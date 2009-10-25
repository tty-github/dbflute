/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.dbflute.logic.bqp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.language.grammar.DfGrammarInfo;
import org.seasar.dbflute.logic.outsidesql.DfSql2EntityMarkAnalyzer;
import org.seasar.dbflute.logic.pathhandling.DfPackagePathHandler;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.util.DfStringUtil;

/**
 * @author jflute
 */
public class DfBehaviorQueryPathSetupper {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfBehaviorQueryPathSetupper.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfBuildProperties _buildProperties;
    protected String _flatDirectoryPackage;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfBehaviorQueryPathSetupper(DfBuildProperties buildProperties) {
        _buildProperties = buildProperties;
    }

    // ===================================================================================
    //                                                                              Set up 
    //                                                                              ======
    /**
     * @param sqlFileList The list of SQL file. (NotNull)
     */
    public void setupBehaviorQueryPath(List<File> sqlFileList) {
        if (getOutsideSqlProperties().isSuppressBehaviorQueryPath()) {
            _log.info("*Behavior Query Path is suppressed!");
            return;
        }
        reflectBehaviorQueryPath(createBehaviorQueryPathMap(sqlFileList));
    }

    // ===================================================================================
    //                                                                             Extract
    //                                                                             =======
    /**
     * Extract the case insensitive map of table behavior query path.
     * <pre>
     * map:{
     *     [tablePropertyName] = map:{
     *         [behaviorQueryPath] = map:{
     *             ; path = [value]
     *             ; behaviorName = [value]
     *             ; entityName = [value]
     *             ; subDirectoryPath = [value]
     *             ; behaviorQueryPath = [value]
     *         }
     *     } 
     * }
     * </pre>
     * @param sqlFileList The list of SQL file. (NotNull)
     * @return The case insensitive map of table behavior query path. (NotNull)
     */
    public Map<String, Map<String, Map<String, String>>> extractTableBqpMap(List<File> sqlFileList) {
        final Map<String, Map<String, Map<String, String>>> resultMap = StringKeyMap.createAsCaseInsensitiveOrder();
        final Map<String, Map<String, String>> bqpMap = createBehaviorQueryPathMap(sqlFileList);
        final Map<File, Map<String, Map<String, String>>> resourceMap = createReflectResourceMap(bqpMap);
        final Set<Entry<File, Map<String, Map<String, String>>>> entrySet = resourceMap.entrySet();
        for (Entry<File, Map<String, Map<String, String>>> entry : entrySet) {
            final File bsbhvFile = entry.getKey();
            String className = bsbhvFile.getName();
            final int extIndex = className.lastIndexOf(".");
            if (extIndex >= 0) {
                className = className.substring(0, extIndex);
            }
            if (className.endsWith("Bhv")) {
                className = className.substring(0, className.length() - "Bhv".length());
            }
            final DfBasicProperties basicProperties = _buildProperties.getBasicProperties();
            final String projectPrefix = basicProperties.getProjectPrefix();
            if (DfStringUtil.isNotNullAndNotTrimmedEmpty(projectPrefix) && className.startsWith(projectPrefix)) {
                className = className.substring(projectPrefix.length(), className.length());
            }
            final String basePrefix = basicProperties.getBasePrefix();
            if (DfStringUtil.isNotNullAndNotTrimmedEmpty(basePrefix) && className.startsWith(basePrefix)) {
                className = className.substring(basePrefix.length(), className.length());
            }
            resultMap.put(className, entry.getValue());

            // additional element
            final Map<String, Map<String, String>> behaviorQueryPathMap = entry.getValue();
            final Set<Entry<String, Map<String, String>>> bqpSet = behaviorQueryPathMap.entrySet();
            for (Entry<String, Map<String, String>> elementEntry : bqpSet) {
                final Map<String, String> elementMap = elementEntry.getValue();
                final String path = elementMap.get("path");
                final File sqlFile = new File(path);
                final DfSql2EntityMarkAnalyzer analyzer = new DfSql2EntityMarkAnalyzer();
                final BufferedReader reader = new BufferedReader(newInputStreamReader(sqlFile));
                final StringBuilder sb = new StringBuilder();
                try {
                    while (true) {
                        String line;
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        sb.append(line).append(ln());
                    }
                } catch (IOException e) {
                    String msg = "Failed to read the SQL: " + sqlFile;
                    throw new IllegalStateException(msg, e);
                }
                final String sql = sb.toString();
                final String customizeEntity = analyzer.getCustomizeEntityName(sql);
                final String parameterBean = analyzer.getParameterBeanName(sql);
                elementMap.put("customizeEntity", customizeEntity);
                elementMap.put("parameterBean", parameterBean);
            }
        }
        return resultMap;
    }

    protected InputStreamReader newInputStreamReader(File sqlFile) {
        final String encoding = getProperties().getOutsideSqlProperties().getSqlFileEncoding();
        try {
            return new InputStreamReader(new FileInputStream(sqlFile), encoding);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("The file does not exist: " + sqlFile, e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("The encoding is unsupported: " + encoding, e);
        }
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    /**
     * @param sqlFileList The list of SQL file. (NotNull)
     * @return The map of behavior query path. (NotNull)
     */
    protected Map<String, Map<String, String>> createBehaviorQueryPathMap(List<File> sqlFileList) {
        final String exbhvName;
        {
            String exbhvPackage = getBasicProperties().getExtendedBehaviorPackage();
            if (exbhvPackage.contains(".")) {
                exbhvPackage = exbhvPackage.substring(exbhvPackage.lastIndexOf(".") + ".".length());
            }
            exbhvName = exbhvPackage;
        }
        Map<String, Map<String, String>> behaviorQueryPathMap = new LinkedHashMap<String, Map<String, String>>();
        gatherBehaviorQueryPathInfo(behaviorQueryPathMap, sqlFileList, exbhvName);
        return behaviorQueryPathMap;
    }

    /**
     * @param behaviorQueryPathMap The empty map of behavior query path. (NotNull)
     * @param sqlFileList The list of SQL file. (NotNull)
     * @param exbhvName The name of extended behavior. (NotNull)
     */
    protected void gatherBehaviorQueryPathInfo(Map<String, Map<String, String>> behaviorQueryPathMap,
            List<File> sqlFileList, String exbhvName) {
        final String exbhvMark = "/" + exbhvName + "/";
        final Pattern behaviorQueryPathPattern = Pattern.compile(".+" + exbhvMark + ".+Bhv_.+.sql$");
        for (File sqlFile : sqlFileList) {
            final String path = getSlashPath(sqlFile);
            final Matcher matcher = behaviorQueryPathPattern.matcher(path);
            if (!matcher.matches()) {
                continue;
            }
            String simpleFileName = path.substring(path.lastIndexOf(exbhvMark) + exbhvMark.length());
            String subDirectoryPath = null;
            if (simpleFileName.contains("/")) {
                subDirectoryPath = simpleFileName.substring(0, simpleFileName.lastIndexOf("/"));
                simpleFileName = simpleFileName.substring(simpleFileName.lastIndexOf("/") + "/".length());
            }
            final int behaviorNameMarkIndex = simpleFileName.indexOf("Bhv_");
            final int behaviorNameEndIndex = behaviorNameMarkIndex + "Bhv".length();
            final int behaviorQueryPathStartIndex = behaviorNameMarkIndex + "Bhv_".length();
            final int behaviorQueryPathEndIndex = simpleFileName.lastIndexOf(".sql");
            final String entityName = simpleFileName.substring(0, behaviorNameMarkIndex);
            final String behaviorName = simpleFileName.substring(0, behaviorNameEndIndex);
            final String behaviorQueryPath = simpleFileName.substring(behaviorQueryPathStartIndex,
                    behaviorQueryPathEndIndex);
            final Map<String, String> behaviorQueryElement = new LinkedHashMap<String, String>();
            behaviorQueryElement.put("path", path);
            behaviorQueryElement.put("subDirectoryPath", subDirectoryPath);
            behaviorQueryElement.put("entityName", entityName);
            behaviorQueryElement.put("behaviorName", behaviorName);
            behaviorQueryElement.put("behaviorQueryPath", behaviorQueryPath);
            behaviorQueryPathMap.put(path, behaviorQueryElement);
        }
    }

    /**
     * @param behaviorQueryPathMap The map of behavior query path. (NotNull)
     */
    protected void reflectBehaviorQueryPath(Map<String, Map<String, String>> behaviorQueryPathMap) {
        Map<File, Map<String, Map<String, String>>> reflectResourceMap = createReflectResourceMap(behaviorQueryPathMap);
        if (reflectResourceMap.isEmpty()) {
            return;
        }
        handleReflectResource(reflectResourceMap);
    }

    protected Map<File, Map<String, Map<String, String>>> createReflectResourceMap(
            Map<String, Map<String, String>> behaviorQueryPathMap) {
        if (behaviorQueryPathMap.isEmpty()) {
            return new HashMap<File, Map<String, Map<String, String>>>();
        }
        String outputDir = getBasicProperties().getGenerateOutputDirectory();
        if (outputDir.endsWith("/")) {
            outputDir = outputDir.substring(0, outputDir.length() - "/".length());
        }
        final String classFileExtension = getBasicProperties().getLanguageDependencyInfo().getGrammarInfo()
                .getClassFileExtension();
        final String projectPrefix = getBasicProperties().getProjectPrefix();
        final String basePrefix = getBasicProperties().getBasePrefix();
        final String bsbhvPackage = getBasicProperties().getBaseBehaviorPackage();

        final DfPackagePathHandler packagePathHandler = new DfPackagePathHandler(getBasicProperties());
        packagePathHandler.setFileSeparatorSlash(true);
        final String bsbhvPathBase = outputDir + "/" + packagePathHandler.getPackageAsPath(bsbhvPackage);

        final File bsbhvDir = new File(bsbhvPathBase);
        final FileFilter filefilter = new FileFilter() {
            public boolean accept(File file) {
                final String path = file.getPath();
                return path.endsWith("Bhv." + classFileExtension);
            }
        };
        if (!bsbhvDir.exists()) {
            _log.warn("The base behavior directory was not found: bsbhvDir=" + bsbhvDir);
            return new HashMap<File, Map<String, Map<String, String>>>();
        }

        final List<File> bsbhvFileList = Arrays.asList(bsbhvDir.listFiles(filefilter));
        final Map<String, File> bsbhvFileMap = new HashMap<String, File>();
        for (File bsbhvFile : bsbhvFileList) {
            String path = getSlashPath(bsbhvFile);
            path = path.substring(0, path.lastIndexOf("." + classFileExtension));
            final String bsbhvSimpleName;
            if (path.contains("/")) {
                bsbhvSimpleName = path.substring(path.lastIndexOf("/") + "/".length());
            } else {
                bsbhvSimpleName = path;
            }
            final String behaviorName = removeBasePrefix(bsbhvSimpleName, projectPrefix, basePrefix);
            bsbhvFileMap.put(behaviorName, bsbhvFile);
        }

        final Map<File, Map<String, Map<String, String>>> reflectResourceMap = new HashMap<File, Map<String, Map<String, String>>>();
        final Set<String> keySet = behaviorQueryPathMap.keySet();
        for (String key : keySet) {
            final Map<String, String> behaviorQueryElementMap = behaviorQueryPathMap.get(key);
            final String behaviorName = behaviorQueryElementMap.get("behaviorName");
            final String behaviorQueryPath = behaviorQueryElementMap.get("behaviorQueryPath");
            final File bsbhvFile = bsbhvFileMap.get(behaviorName);
            if (bsbhvFile == null) {
                throwBehaviorNotFoundException(bsbhvFileMap, behaviorQueryElementMap, bsbhvPathBase);
            }
            Map<String, Map<String, String>> resourceElementMap = reflectResourceMap.get(bsbhvFile);
            if (resourceElementMap == null) {
                resourceElementMap = new LinkedHashMap<String, Map<String, String>>();
                reflectResourceMap.put(bsbhvFile, resourceElementMap);
            }
            if (!resourceElementMap.containsKey(behaviorQueryPath)) {
                resourceElementMap.put(behaviorQueryPath, behaviorQueryElementMap);
            }
        }
        return reflectResourceMap;
    }

    protected void throwBehaviorNotFoundException(Map<String, File> bsbhvFileMap,
            Map<String, String> behaviorQueryElementMap, String bsbhvPathBase) {
        final String path = behaviorQueryElementMap.get("path");
        final String behaviorName = behaviorQueryElementMap.get("behaviorName");
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The behavior was Not Found!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm the existence of the behavior." + ln();
        msg = msg + "And confirm your SQL file name." + ln();
        msg = msg + ln();
        msg = msg + "[Your SQL File]" + ln() + path + ln();
        msg = msg + ln();
        msg = msg + "[Not Found Behavior]" + ln() + behaviorName + ln();
        msg = msg + ln();
        msg = msg + "[Behavior Directory]" + ln() + bsbhvPathBase + ln();
        msg = msg + ln();
        msg = msg + "[Behavior List]" + ln() + bsbhvFileMap.keySet() + ln();
        msg = msg + "* * * * * * * * * */" + ln();
        throw new BehaviorNotFoundException(msg);
    }

    protected static class BehaviorNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public BehaviorNotFoundException(String msg) {
            super(msg);
        }
    }

    /**
     * @param reflectResourceMap The map of reflect resource. (NotNull)
     */
    protected void handleReflectResource(Map<File, Map<String, Map<String, String>>> reflectResourceMap) {
        _log.info(" ");
        _log.info("[Behavior Query Path]");
        final Set<File> fileKeySet = reflectResourceMap.keySet();
        for (File bsbhvFile : fileKeySet) {
            final Map<String, Map<String, String>> resourceElementMap = reflectResourceMap.get(bsbhvFile);
            writeBehaviorQueryPath(bsbhvFile, resourceElementMap);
        }
        _log.info(" ");
    }

    /**
     * @param bsbhvFile The file of base behavior. (NotNull)
     * @param resourceElementMap The map of resource element. (NotNull) 
     */
    protected void writeBehaviorQueryPath(File bsbhvFile, Map<String, Map<String, String>> resourceElementMap) {
        final String encoding = getBasicProperties().getTemplateFileEncoding();
        final BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(bsbhvFile), encoding));
        } catch (UnsupportedEncodingException e) {
            String msg = "The encoding is unsupported: encoding=" + encoding;
            throw new IllegalStateException(msg, e);
        } catch (FileNotFoundException e) {
            String msg = "The file of base behavior was Not Found: bsbhvFile=" + bsbhvFile;
            throw new IllegalStateException(msg, e);
        }
        StringBuilder logSb = new StringBuilder();
        final String path = getSlashPath(bsbhvFile);
        if (path.contains("/")) {
            logSb.append(path.substring(path.lastIndexOf("/") + "/".length()));
        } else {
            logSb.append(path);
        }
        final DfGrammarInfo grammarInfo = getBasicProperties().getLanguageDependencyInfo().getGrammarInfo();
        final String behaviorQueryPathBeginMark = getBasicProperties().getBehaviorQueryPathBeginMark();
        final String behaviorQueryPathEndMark = getBasicProperties().getBehaviorQueryPathEndMark();
        String lineString = null;
        final StringBuilder sb = new StringBuilder();
        try {
            boolean targetArea = false;
            boolean done = false;
            while (true) {
                lineString = bufferedReader.readLine();
                if (lineString == null) {
                    if (targetArea) {
                        String msg = "The end mark of behavior query path was NOT found: bsbhvFile=" + bsbhvFile;
                        throw new IllegalStateException(msg);
                    }
                    break;
                }
                if (targetArea) {
                    if (lineString.contains(behaviorQueryPathEndMark)) {
                        targetArea = false;
                    } else {
                        continue;
                    }
                }
                sb.append(lineString).append("\n");
                if (!done && lineString.contains(behaviorQueryPathBeginMark)) {
                    targetArea = true;
                    final Set<String> behaviorQueryPathSet = resourceElementMap.keySet();
                    for (String behaviorQueryPath : behaviorQueryPathSet) {
                        final Map<String, String> behaviorQueryElementMap = resourceElementMap.get(behaviorQueryPath);
                        final StringBuilder definitionLineSb = new StringBuilder();
                        definitionLineSb
                                .append(lineString.substring(0, lineString.indexOf(behaviorQueryPathBeginMark)));
                        definitionLineSb.append(grammarInfo.getPublicStaticDefinition());
                        final String subDirectoryPath = behaviorQueryElementMap.get("subDirectoryPath");
                        if (DfStringUtil.isNotNullAndNotTrimmedEmpty(subDirectoryPath)) {
                            final String subDirectoryName = DfStringUtil.replace(subDirectoryPath, "/", "_");
                            final String subDirectoryValue = DfStringUtil.replace(subDirectoryPath, "/", ":");
                            definitionLineSb.append(" String PATH_");
                            definitionLineSb.append(subDirectoryName).append("_").append(behaviorQueryPath);
                            definitionLineSb.append(" = \"");
                            definitionLineSb.append(subDirectoryValue).append(":").append(behaviorQueryPath);
                            definitionLineSb.append("\";");
                        } else {
                            definitionLineSb.append(" String PATH_").append(behaviorQueryPath);
                            definitionLineSb.append(" = \"").append(behaviorQueryPath).append("\";");
                        }

                        String tmp4log = definitionLineSb.toString();
                        logSb.append("\n").append(tmp4log.substring(tmp4log.indexOf(" PATH_")));

                        definitionLineSb.append("\n");
                        sb.append(definitionLineSb);
                    }
                    done = true;
                }
            }
            _log.info(logSb.toString());
            if (!done) {
                _log.info("  --> The mark of behavior query path was Not Found!");
            }
        } catch (IOException e) {
            String msg = "bufferedReader.readLine() threw the exception: current line=" + lineString;
            throw new IllegalStateException(msg, e);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException ignored) {
                _log.warn(ignored.getMessage());
            }
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bsbhvFile), encoding));
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();
        } catch (UnsupportedEncodingException e) {
            String msg = "The encoding is unsupported: encoding=" + encoding;
            throw new IllegalStateException(msg, e);
        } catch (FileNotFoundException e) {
            String msg = "The file of base behavior was not found: bsbhvFile=" + bsbhvFile;
            throw new IllegalStateException(msg, e);
        } catch (IOException e) {
            String msg = "bufferedWriter.write() threw the exception: bsbhvFile=" + bsbhvFile;
            throw new IllegalStateException(msg, e);
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException ignored) {
                    _log.warn(ignored.getMessage());
                }
            }
        }
    }

    protected String removeBasePrefix(String simpleClassName, String projectPrefix, String basePrefix) {
        final String prefix = projectPrefix + basePrefix;
        if (!simpleClassName.startsWith(prefix)) {
            return simpleClassName;
        }
        final int prefixLength = prefix.length();
        if (!Character.isUpperCase(simpleClassName.substring(prefixLength).charAt(0))) {
            return simpleClassName;
        }
        if (simpleClassName.length() <= prefixLength) {
            return simpleClassName;
        }
        return projectPrefix + simpleClassName.substring(prefixLength);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    public String replaceString(String text, String fromText, String toText) {
        return DfStringUtil.replace(text, fromText, toText);
    }

    public String getSlashPath(File file) {
        return replaceString(file.getPath(), getFileSeparator(), "/");
    }

    public String getFileSeparator() {
        return File.separator;
    }

    public String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected DfBuildProperties getProperties() {
        return _buildProperties;
    }

    protected DfBasicProperties getBasicProperties() {
        return _buildProperties.getBasicProperties();
    }

    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return _buildProperties.getOutsideSqlProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    public String getFlatDirectoryPackage() {
        return _flatDirectoryPackage;
    }

    public void setFlatDirectoryPackage(String flatDirectoryPackage) {
        this._flatDirectoryPackage = flatDirectoryPackage;
    }
}