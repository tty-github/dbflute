/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.dbflute;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.properties.AdditionalForeignKeyProperties;
import org.seasar.dbflute.properties.BasicProperties;
import org.seasar.dbflute.properties.ClassificationProperties;
import org.seasar.dbflute.properties.DaoDiconProperties;
import org.seasar.dbflute.properties.GeneratedClassPackageProperties;
import org.seasar.dbflute.properties.OptimisticLockProperties;
import org.seasar.dbflute.properties.OtherProperties;
import org.seasar.dbflute.properties.PropertiesHandler;
import org.seasar.dbflute.properties.SelectParamProperties;
import org.seasar.dbflute.properties.Sql2EntityProperties;
import org.seasar.dbflute.util.DfPropertyUtil;
import org.seasar.dbflute.util.DfPropertyUtil.PropertyBooleanFormatException;
import org.seasar.dbflute.util.DfPropertyUtil.PropertyIntegerFormatException;
import org.seasar.dbflute.util.DfPropertyUtil.PropertyNotFoundException;

/**
 * Build properties for Torque.
 * 
 * @author mkubo
 */
public final class TorqueBuildProperties {

    /** Log-instance */
    private static final Log _log = LogFactory.getLog(TorqueBuildProperties.class);

    /** Singleton-instance. */
    private static final TorqueBuildProperties _instance = new TorqueBuildProperties();

    /** TorqueContextProperties */
    private Properties _buildProperties;

    /**
     * Constructor.
     */
    private TorqueBuildProperties() {
    }

    /**
     * Get singleton-instance.
     * 
     * @return Singleton-Instance. (NotNull)
     */
    public synchronized static TorqueBuildProperties getInstance() {
        return _instance;
    }

    /**
     * Set context-properties.
     * 
     * @param value Context-properties.
     */
    final public void setProperties(Properties value) {
        _buildProperties = value;
    }

    /**
     * Get context-properties.
     * 
     * @return Context-properties.
     */
    final public Properties getProperties() {
        return _buildProperties;
    }

    // **********************************************************************************************
    //                                                                                       Delegate
    //                                                                                       ********
    /**
     * Get property as string. {Delegate method}
     * 
     * @param key Property-key.
     * @return Property as string.
     */
    final public String stringProp(String key) {
        try {
            return DfPropertyUtil.stringProp(_buildProperties, key);
        } catch (RuntimeException e) {
            _log.warn("FlPropertyUtil#stringProp() threw the exception with The key[" + key + "]", e);
            throw e;
        }
    }

    /**
     * Get property as string. {Delegate method}
     * 
     * @param key Property-key.
     * @param defaultValue Default value.
     * @return Property as string.
     */
    final public String stringProp(String key, String defaultValue) {
        try {
            return DfPropertyUtil.stringProp(_buildProperties, key);
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        } catch (RuntimeException e) {
            _log.warn("FlPropertyUtil#stringProp() threw the exception with The key[" + key + "]", e);
            throw e;
        }
    }

    /**
     * Get property as boolean. {Delegate method}
     * 
     * @param key Property-key.
     * @return Property as boolean.
     */
    final public boolean booleanProp(String key) {
        try {
            return DfPropertyUtil.booleanProp(_buildProperties, key);
        } catch (RuntimeException e) {
            _log.warn("FlPropertyUtil#booleanProp() threw the exception with The key[" + key + "]", e);
            throw e;
        }
    }

    /**
     * Get property as boolean. {Delegate method}
     * 
     * @param key Property-key.
     * @param defaultValue Default value.
     * @return Property as boolean.
     */
    final public boolean booleanProp(String key, boolean defaultValue) {
        try {
            return DfPropertyUtil.booleanProp(_buildProperties, key);
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        } catch (PropertyBooleanFormatException e) {
            return defaultValue;
        } catch (RuntimeException e) {
            _log.warn("FlPropertyUtil#intProp() threw the exception with The key[" + key + "]", e);
            throw e;
        }
    }

    /**
     * Get property as integer. {Delegate method}
     * 
     * @param key Property-key.
     * @return Property as integer.
     */
    final public int intProp(String key) {
        try {
            return DfPropertyUtil.intProp(_buildProperties, key);
        } catch (RuntimeException e) {
            _log.warn("FlPropertyUtil#intProp() threw the exception with The key[" + key + "]", e);
            throw e;
        }
    }

    /**
     * Get property as integer. {Delegate method}
     * 
     * @param key Property-key.
     * @param defaultValue Default value.
     * @return Property as integer.
     */
    final public int intProp(String key, int defaultValue) {
        try {
            return DfPropertyUtil.intProp(_buildProperties, key);
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        } catch (PropertyIntegerFormatException e) {
            return defaultValue;
        } catch (RuntimeException e) {
            _log.warn("FlPropertyUtil#intProp() threw the exception with The key[" + key + "]", e);
            throw e;
        }
    }

    /**
     * Get property as list. {Delegate method}
     * 
     * @param key Property-key.
     * @return Property as list.
     */
    final public List<Object> listProp(String key) {
        try {
            return DfPropertyUtil.listProp(_buildProperties, key, ";");
        } catch (RuntimeException e) {
            _log.warn("FlPropertyUtil#listProp() threw the exception with The key[" + key + "]", e);
            throw e;
        }
    }

    /**
     * Get property as list. {Delegate method}
     * 
     * @param key Property-key.
     * @param defaultValue Default value.
     * @return Property as list.
     */
    final public List<Object> listProp(String key, List<Object> defaultValue) {
        try {
            final List<Object> result = DfPropertyUtil.listProp(_buildProperties, key, ";");
            if (result.isEmpty()) {
                return defaultValue;
            } else {
                return result;
            }
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        } catch (RuntimeException e) {
            _log.warn("FlPropertyUtil#intProp() threw the exception with The key[" + key + "]", e);
            throw e;
        }
    }

    /**
     * Get property as map. {Delegate method}
     * 
     * @param key Property-key.
     * @return Property as map.
     */
    final public Map<String, Object> mapProp(String key) {
        try {
            return DfPropertyUtil.mapProp(_buildProperties, key, ";");
        } catch (RuntimeException e) {
            _log.warn("FlPropertyUtil#mapProp() threw the exception with The key[" + key + "]", e);
            throw e;
        }
    }

    /**
     * Get property as map. {Delegate method}
     * 
     * @param key Property-key.
     * @param defaultValue Default value.
     * @return Property as map.
     */
    final public Map<String, Object> mapProp(String key, Map<String, Object> defaultValue) {
        try {
            final Map<String, Object> result = DfPropertyUtil.mapProp(_buildProperties, key, ";");
            if (result.isEmpty()) {
                return defaultValue;
            } else {
                return result;
            }
        } catch (PropertyNotFoundException e) {
            return defaultValue;
        } catch (RuntimeException e) {
            _log.warn("FlPropertyUtil#intProp() threw the exception with The key[" + key + "]", e);
            throw e;
        }
    }

    // **********************************************************************************************
    //                                                                                        Default
    //                                                                                        *******
    public static final Map<String, Object> DEFAULT_EMPTY_MAP = new LinkedHashMap<String, Object>();
    public static final List<Object> DEFAULT_EMPTY_LIST = new ArrayList<Object>();
    public static final String DEFAULT_EMPTY_MAP_STRING = "map:{}";
    public static final String DEFAULT_EMPTY_LIST_STRING = "list:{}";

    // **********************************************************************************************
    //                                                                                        Handler
    //                                                                                        *******
    public PropertiesHandler getHandler() {
        return PropertiesHandler.getInstance();
    }

    // **********************************************************************************************
    //                                                                                       Property
    //                                                                                       ********
    // ===============================================================================
    //                                                              Properties - Basic
    //                                                              ==================
    public BasicProperties getBasicProperties() {
        return getHandler().getBasicProperties(getProperties());
    }

    // ===============================================================================
    //                                                           Properties - DaoDicon
    //                                                           =====================
    public DaoDiconProperties getDaoDiconProperties() {
        return getHandler().getDaoDiconProperties(getProperties());
    }

    // ===============================================================================
    //                                            Properties - Generated Class Package
    //                                            ====================================
    public GeneratedClassPackageProperties getGeneratedClassPackageProperties() {
        return getHandler().getGeneratedClassPackageProperties(getProperties());
    }

    // ===============================================================================
    //                                              Properties - Sequence and Identity
    //                                              ==================================
    public static final String KEY_sequenceDefinitionMap = "sequenceDefinitionMap";
    protected Map<String, Object> _sequenceDefinitionMap;

    public Map<String, Object> getSequenceDefinitionMap() {
        if (_sequenceDefinitionMap == null) {
            _sequenceDefinitionMap = mapProp("torque." + KEY_sequenceDefinitionMap, DEFAULT_EMPTY_MAP);
        }
        return _sequenceDefinitionMap;
    }

    public String getSequenceDefinitionMapAsStringRemovedLineSeparator() {
        final String property = stringProp("torque." + KEY_sequenceDefinitionMap, DEFAULT_EMPTY_MAP_STRING);
        return removeNewLine(property);
    }

    public static final String KEY_identityDefinitionMap = "identityDefinitionMap";
    protected Map<String, Object> _identityDefinitionMap;

    public Map<String, Object> getIdentityDefinitionMap() {
        if (_identityDefinitionMap == null) {
            _identityDefinitionMap = mapProp("torque." + KEY_identityDefinitionMap, DEFAULT_EMPTY_MAP);
        }
        return _identityDefinitionMap;
    }

    // ===============================================================================
    //                                                    Properties - Optimistic Lock
    //                                                    ============================
    public OptimisticLockProperties getOptimisticLockProperties() {
        return getHandler().getOptimisticLockProperties(getProperties());
    }

    // ===============================================================================
    //                                                      Properties - Common-Column
    //                                                      ==========================
    public static final String KEY_commonColumnMap = "commonColumnMap";
    protected Map<String, Object> _commonColumnMap;

    public Map<String, Object> getCommonColumnMap() {
        if (_commonColumnMap == null) {
            _commonColumnMap = mapProp("torque." + KEY_commonColumnMap, DEFAULT_EMPTY_MAP);
        }
        return _commonColumnMap;
    }

    protected List<String> _commonColumnNameList;

    public List<String> getCommonColumnNameList() {
        if (_commonColumnNameList == null) {
            final Map<String, Object> commonColumnMap = getCommonColumnMap();
            _commonColumnNameList = new ArrayList<String>(commonColumnMap.keySet());
        }
        return _commonColumnNameList;
    }

    // --------------------------------------
    //                                 insert
    //                                 ------
    public static final String KEY_commonColumnSetupBeforeInsertInterceptorLogicMap = "commonColumnSetupBeforeInsertInterceptorLogicMap";
    protected Map<String, Object> _commonColumnSetupBeforeInsertInterceptorLogicMap;

    public Map<String, Object> getCommonColumnSetupBeforeInsertInterceptorLogicMap() {
        if (_commonColumnSetupBeforeInsertInterceptorLogicMap == null) {
            final String key = "torque." + KEY_commonColumnSetupBeforeInsertInterceptorLogicMap;
            _commonColumnSetupBeforeInsertInterceptorLogicMap = mapProp(key, DEFAULT_EMPTY_MAP);
        }
        return _commonColumnSetupBeforeInsertInterceptorLogicMap;
    }

    public boolean containsValidColumnNameKeyCommonColumnSetupBeforeInsertInterceptorLogicMap(String columnName) {
        final Map map = getCommonColumnSetupBeforeInsertInterceptorLogicMap();
        final String logic = (String) map.get(columnName);
        if (logic != null && logic.trim().length() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getCommonColumnSetupBeforeInsertInterceptorLogicByColumnName(String columnName) {
        final Map map = getCommonColumnSetupBeforeInsertInterceptorLogicMap();
        return (String) map.get(columnName);
    }

    // --------------------------------------
    //                                 update
    //                                 ------
    public static final String KEY_commonColumnSetupBeforeUpdateInterceptorLogicMap = "commonColumnSetupBeforeUpdateInterceptorLogicMap";
    protected Map<String, Object> _commonColumnSetupBeforeUpdateInterceptorLogicMap;

    public Map<String, Object> getCommonColumnSetupBeforeUpdateInterceptorLogicMap() {
        if (_commonColumnSetupBeforeUpdateInterceptorLogicMap == null) {
            final String key = "torque." + KEY_commonColumnSetupBeforeUpdateInterceptorLogicMap;
            _commonColumnSetupBeforeUpdateInterceptorLogicMap = mapProp(key, DEFAULT_EMPTY_MAP);
        }
        return _commonColumnSetupBeforeUpdateInterceptorLogicMap;
    }

    public boolean containsValidColumnNameKeyCommonColumnSetupBeforeUpdateInterceptorLogicMap(String columnName) {
        final Map map = getCommonColumnSetupBeforeUpdateInterceptorLogicMap();
        final String logic = (String) map.get(columnName);
        if (logic != null && logic.trim().length() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getCommonColumnSetupBeforeUpdateInterceptorLogicByColumnName(String columnName) {
        final Map map = getCommonColumnSetupBeforeUpdateInterceptorLogicMap();
        return (String) map.get(columnName);
    }

    // --------------------------------------
    //                                 delete
    //                                 ------
    public static final String KEY_commonColumnSetupBeforeDeleteInterceptorLogicMap = "commonColumnSetupBeforeDeleteInterceptorLogicMap";
    protected Map<String, Object> _commonColumnSetupBeforeDeleteInterceptorLogicMap;

    public Map<String, Object> getCommonColumnSetupBeforeDeleteInterceptorLogicMap() {
        if (_commonColumnSetupBeforeDeleteInterceptorLogicMap == null) {
            final String key = "torque." + KEY_commonColumnSetupBeforeDeleteInterceptorLogicMap;
            _commonColumnSetupBeforeDeleteInterceptorLogicMap = mapProp(key, DEFAULT_EMPTY_MAP);
        }
        return _commonColumnSetupBeforeDeleteInterceptorLogicMap;
    }

    public boolean containsValidColumnNameKeyCommonColumnSetupBeforeDeleteInterceptorLogicMap(String columnName) {
        final Map map = getCommonColumnSetupBeforeDeleteInterceptorLogicMap();
        final String logic = (String) map.get(columnName);
        if (logic != null && logic.trim().length() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getCommonColumnSetupBeforeDeleteInterceptorLogicByColumnName(String columnName) {
        final Map map = getCommonColumnSetupBeforeDeleteInterceptorLogicMap();
        return (String) map.get(columnName);
    }

    // ===============================================================================
    //                                                     Properties - Logical-Delete
    //                                                     ===========================
    public static final String KEY_logicalDeleteColumnValueMap = "logicalDeleteColumnValueMap";
    protected Map<String, Object> _logicalDeleteColumnValueMap;

    public Map<String, Object> getLogicalDeleteColumnValueMap() {
        if (_logicalDeleteColumnValueMap == null) {
            final String key = "torque." + KEY_logicalDeleteColumnValueMap;
            _logicalDeleteColumnValueMap = mapProp(key, DEFAULT_EMPTY_MAP);
        }
        return _logicalDeleteColumnValueMap;
    }

    protected List<String> _logicalDeleteColumnNameList;

    public List<String> getLogicalDeleteColumnNameList() {
        if (_logicalDeleteColumnNameList == null) {
            _logicalDeleteColumnNameList = new ArrayList<String>(getLogicalDeleteColumnValueMap().keySet());
        }
        return _logicalDeleteColumnNameList;
    }

    // ===============================================================================
    //                                        Properties - Revival from Logical-Delete
    //                                        ========================================
    public static final String KEY_revivalFromLogicalDeleteColumnValueMap = "revivalFromLogicalDeleteColumnValueMap";
    protected Map<String, Object> _revivalFromLogicalDeleteColumnValueMap;

    public Map<String, Object> getRevivalFromLogicalDeleteColumnValueMap() {
        if (_revivalFromLogicalDeleteColumnValueMap == null) {
            final String key = "torque." + KEY_revivalFromLogicalDeleteColumnValueMap;
            _revivalFromLogicalDeleteColumnValueMap = mapProp(key, DEFAULT_EMPTY_MAP);
        }
        return _revivalFromLogicalDeleteColumnValueMap;
    }

    protected List<String> _revivalFromLogicalDeleteColumnNameList;

    public List<String> getRevivalFromLogicalDeleteColumnNameList() {
        if (_revivalFromLogicalDeleteColumnNameList == null) {
            _revivalFromLogicalDeleteColumnNameList = new ArrayList<String>(getLogicalDeleteColumnValueMap().keySet());
        }
        return _revivalFromLogicalDeleteColumnNameList;
    }

    // ===============================================================================
    //                                                     Properties - Classification
    //                                                     ===========================
    // --------------------------------------
    //                             Definition
    //                             ----------
    protected ClassificationProperties _classificationProperties;

    protected ClassificationProperties getClassificationProperties() {
        if (_classificationProperties == null) {
            _classificationProperties = new ClassificationProperties(_buildProperties);
        }
        return _classificationProperties;
    }

    public boolean hasClassificationDefinitionMap() {
        return getClassificationProperties().hasClassificationDefinitionMap();
    }

    public Map<String, List<Map<String, String>>> getClassificationDefinitionMap() {
        return getClassificationProperties().getClassificationDefinitionMap();
    }

    public List<String> getClassificationNameList() {
        return getClassificationProperties().getClassificationNameList();
    }

    public List<String> getClassificationNameListValidNameOnly() {
        return getClassificationProperties().getClassificationNameListValidNameOnly();
    }

    public List<String> getClassificationNameListValidAliasOnly() {
        return getClassificationProperties().getClassificationNameListValidAliasOnly();
    }

    public String getClassificationDefinitionMapAsStringRemovedLineSeparatorFilteredQuotation() {
        return getClassificationProperties()
                .getClassificationDefinitionMapAsStringRemovedLineSeparatorFilteredQuotation();
    }

    public List<java.util.Map<String, String>> getClassificationMapList(String classificationName) {
        return getClassificationProperties().getClassificationMapList(classificationName);
    }

    // --------------------------------------
    //                             Deployment
    //                             ----------
    public Map<String, Map<String, String>> getClassificationDeploymentMap() {
        return getClassificationProperties().getClassificationDeploymentMap();
    }

    public void initializeClassificationDeploymentMap(List<Table> tableList) {
        getClassificationProperties().initializeClassificationDeploymentMap(tableList);
    }

    public String getClassificationDeploymentMapAsStringRemovedLineSeparatorFilteredQuotation() {
        return getClassificationProperties()
                .getClassificationDeploymentMapAsStringRemovedLineSeparatorFilteredQuotation();
    }

    public boolean hasClassification(String tableName, String columnName) {
        return getClassificationProperties().hasClassification(tableName, columnName);
    }

    public String getClassificationName(String tableName, String columnName) {
        return getClassificationProperties().getClassificationName(tableName, columnName);
    }

    public boolean hasClassificationName(String tableName, String columnName) {
        return getClassificationProperties().hasClassificationName(tableName, columnName);
    }

    public boolean hasClassificationAlias(String tableName, String columnName) {
        return getClassificationProperties().hasClassificationAlias(tableName, columnName);
    }

    public Map<String, String> getAllColumnClassificationMap() {
        return getClassificationProperties().getAllColumnClassificationMap();
    }

    public boolean isAllClassificationColumn(String columnName) {
        return getClassificationProperties().isAllClassificationColumn(columnName);
    }

    public String getAllClassificationName(String columnName) {
        return getClassificationProperties().getAllClassificationName(columnName);
    }

    // ===============================================================================
    //                                                       Properties - Select Param
    //                                                       =========================
    protected SelectParamProperties getSelectParamProperties() {
        return getHandler().getSelectParamProperties(getProperties());
    }

    public String getSelectQueryTimeout() {
        return getSelectParamProperties().getSelectQueryTimeout();
    }

    public boolean isSelectQueryTimeoutValid() {
        return getSelectParamProperties().isSelectQueryTimeoutValid();
    }

    public String getStatementResultSetType() {
        return getSelectParamProperties().getStatementResultSetType();
    }

    public String getStatementResultSetConcurrency() {
        return getSelectParamProperties().getStatementResultSetConcurrency();
    }

    public boolean isStatementResultSetTypeValid() {
        return getSelectParamProperties().isStatementResultSetTypeValid();
    }

    // ===============================================================================
    //                                                       Properties - CustomizeDao
    //                                                       =========================
    public static final String KEY_customizeDaoDefinitionMap = "customizeDaoDefinitionMap";
    protected Map<String, Map<String, Map<String, String>>> _customizeDaoDefinitionMap;

    public Map<String, Map<String, Map<String, String>>> getCustomizeDaoDifinitionMap() {
        if (_customizeDaoDefinitionMap == null) {
            _customizeDaoDefinitionMap = new LinkedHashMap<String, Map<String, Map<String, String>>>();
            final Map<String, Object> generatedMap = mapProp("torque." + KEY_customizeDaoDefinitionMap,
                    DEFAULT_EMPTY_MAP);
            final Set fisrtKeySet = generatedMap.keySet();
            for (Object tableName : fisrtKeySet) {
                final Object firstValue = generatedMap.get(tableName);
                if (!(firstValue instanceof Map)) {
                    String msg = "The value type should be Map: tableName=" + tableName + " property=CustomizeDao";
                    msg = msg + " actualType=" + firstValue.getClass() + " actualValue=" + firstValue;
                    throw new IllegalStateException(msg);
                }
                final Map tableDefinitionMap = (Map) firstValue;
                Set secondKeySet = tableDefinitionMap.keySet();
                final Map<String, Map<String, String>> genericTableDefinitiontMap = new LinkedHashMap<String, Map<String, String>>();
                for (Object componentName : secondKeySet) {
                    final Object secondValue = tableDefinitionMap.get(componentName);
                    if (secondValue == null) {
                        continue;
                    }
                    if (!(componentName instanceof String)) {
                        String msg = "The key type should be String: tableName=" + tableName + " property=CustomizeDao";
                        msg = msg + " actualType=" + componentName.getClass() + " actualKey=" + componentName;
                        throw new IllegalStateException(msg);
                    }
                    if (!(secondValue instanceof Map)) {
                        String msg = "The value type should be Map: tableName=" + tableName + " property=CustomizeDao";
                        msg = msg + " actualType=" + secondValue.getClass() + " actualValue=" + secondValue;
                        throw new IllegalStateException(msg);
                    }

                    final Map componentMap = (Map) secondValue;
                    Set thirdKeySet = componentMap.keySet();
                    final Map<String, String> genericComponentMap = new LinkedHashMap<String, String>();
                    for (Object componentKey : thirdKeySet) {
                        final Object componentValue = componentMap.get(componentKey);
                        if (!(componentKey instanceof String)) {
                            String msg = "The key type should be String: tableName=" + tableName
                                    + " property=CustomizeDao";
                            msg = msg + " actualType=" + componentKey.getClass() + " actualKey=" + componentKey;
                            throw new IllegalStateException(msg);
                        }
                        if (!(componentValue instanceof String)) {
                            String msg = "The value type should be String: tableName=" + tableName
                                    + " property=CustomizeDao";
                            msg = msg + " actualType=" + componentValue.getClass() + " actualValue=" + componentValue;
                            throw new IllegalStateException(msg);
                        }
                        genericComponentMap.put((String) componentKey, (String) componentValue);
                    }
                    genericTableDefinitiontMap.put((String) componentName, genericComponentMap);
                }
                _customizeDaoDefinitionMap.put((String) tableName, genericTableDefinitiontMap);
            }
        }
        return _customizeDaoDefinitionMap;
    }

    public Map<String, String> getCustomizeDaoComponentColumnMap(String tableName) {
        final Map<String, Map<String, String>> componentDefinitionMap = getCustomizeDaoDifinitionMap().get(tableName);
        final Map<String, String> columnMap = componentDefinitionMap.get("columnMap");
        if (columnMap == null) {
            String msg = "The table did not have 'columnMap': tableName=" + tableName;
            msg = msg + " componentDefinitionMap=" + componentDefinitionMap;
            msg = msg + " " + KEY_customizeDaoDefinitionMap + "=" + getCustomizeDaoDifinitionMap();
            throw new IllegalStateException(msg);
        }
        return columnMap;
    }

    public Map<String, String> getCustomizeDaoComponentMethodMap(String tableName) {
        final Map<String, Map<String, String>> componentDefinitionMap = getCustomizeDaoDifinitionMap().get(tableName);
        final Map<String, String> methodMap = componentDefinitionMap.get("methodMap");
        if (methodMap == null) {
            String msg = "The table did not have 'methodMap': tableName=" + tableName;
            msg = msg + " componentDefinitionMap=" + componentDefinitionMap;
            msg = msg + " " + KEY_customizeDaoDefinitionMap + "=" + getCustomizeDaoDifinitionMap();
            throw new IllegalStateException(msg);
        }
        return methodMap;
    }

    public String getCustomizeDaoComponentMethodArgumentVariableCommaString(String tableName, String methodName) {
        final Map<String, String> methodMap = getCustomizeDaoComponentMethodMap(tableName);
        final StringBuffer sb = new StringBuffer();
        final String argumentString = methodMap.get(methodName);
        final StringTokenizer st = new StringTokenizer(argumentString, ",");
        while (st.hasMoreTokens()) {
            final String trimmedToken = st.nextToken().trim();
            if (trimmedToken.indexOf(" ") == -1) {
                String msg = "The trimmedToken should have one blank: trimmedToken" + trimmedToken + " methodMap="
                        + methodMap;
                throw new IllegalStateException(msg);
            }
            sb.append(", ").append(trimmedToken.substring(trimmedToken.indexOf(" ") + 1));
        }
        sb.delete(0, ", ".length());
        return sb.toString();
    }

    public Map<String, String> getCustomizeDaoComponentImportMap(String tableName) {
        final Map<String, Map<String, String>> componentDefinitionMap = getCustomizeDaoDifinitionMap().get(tableName);
        return componentDefinitionMap.get("importMap");
    }

    public Map<String, String> getCustomizeDaoComponentRelationMap(String tableName) {
        final Map<String, Map<String, String>> componentDefinitionMap = getCustomizeDaoDifinitionMap().get(tableName);
        return componentDefinitionMap.get("relationMap");
    }

    public boolean isAvailableCustomizeDaoGeneration() {
        return !getCustomizeDaoDifinitionMap().isEmpty();
    }

    // ===============================================================================
    //                                               Properties - AdditionalForeignKey
    //                                               =================================
    protected AdditionalForeignKeyProperties getAdditionalForeignKeyProperties() {
        return getHandler().getAdditionalForeignKeyProperties(getProperties());
    }

    public Map<String, Map<String, String>> getAdditionalForeignKeyMap() {
        return getAdditionalForeignKeyProperties().getAdditionalForeignKeyMap();
    }

    public String getAdditionalForeignKeyComponentLocalTableName(String foreignName) {
        return getAdditionalForeignKeyProperties().getAdditionalForeignKeyComponentLocalTableName(foreignName);
    }

    public String getAdditionalForeignKeyComponentForeignTableName(String foreignName) {
        return getAdditionalForeignKeyProperties().getAdditionalForeignKeyComponentForeignTableName(foreignName);
    }

    public List<String> getAdditionalForeignKeyComponentLocalColumnNameList(String foreignName) {
        return getAdditionalForeignKeyProperties().getAdditionalForeignKeyComponentLocalColumnNameList(foreignName);
    }

    public List<String> getAdditionalForeignKeyComponentForeignColumnNameList(String foreignName) {
        return getAdditionalForeignKeyProperties().getAdditionalForeignKeyComponentForeignColumnNameList(foreignName);
    }

    // ===============================================================================
    //                                                   Properties - SqlParameterBean
    //                                                   =============================
    public static final String KEY_sqlParameterBeanDefinitionMap = "sqlParameterBeanDefinitionMap";
    protected Map<String, Object> _sqlParameterBeanDefinitionMap;

    public String getSqlParameterBeanPackage() {
        return stringProp("torque.sqlParameterBeanPackage", "");
    }

    public Map<String, Object> getSqlParameterBeanDefinitionMap() {
        if (_sqlParameterBeanDefinitionMap == null) {
            _sqlParameterBeanDefinitionMap = mapProp("torque." + KEY_sqlParameterBeanDefinitionMap, DEFAULT_EMPTY_MAP);
        }
        return _sqlParameterBeanDefinitionMap;
    }

    public List<String> getSqlParameterBeanClassNameList() {
        return new ArrayList<String>(getSqlParameterBeanDefinitionMap().keySet());
    }

    public Map<String, String> getSqlParameterBeanClassDefinitionMap(String className) {
        final Map<String, String> map = (Map<String, String>) getSqlParameterBeanDefinitionMap().get(className);
        if (map == null) {
            String msg = "getSqlParameterBeanDefinitionMap().get(className) returned null: " + className;
            throw new IllegalArgumentException(msg);
        }
        return map;
    }

    public String getSqlParameterBeanPropertyType(String className, String property) {
        final String str = (String) getSqlParameterBeanClassDefinitionMap(className).get(property);
        if (str == null) {
            String msg = "getSqlParameterBeanClassDefinitionMap(className).get(property) returned null";
            msg = msg + ": className=" + className + " property=" + property;
            throw new IllegalArgumentException(msg);
        }
        if (str.indexOf("-") == -1) {
            return str;
        }
        return str.substring(0, str.indexOf("-"));
    }

    public boolean isSqlParameterBeanPropertyDefaultValueEffective(String className, String property) {
        return getSqlParameterBeanPropertyDefaultValue(className, property).trim().length() != 0;
    }

    public String getSqlParameterBeanPropertyDefaultValue(String className, String property) {
        final String str = (String) getSqlParameterBeanClassDefinitionMap(className).get(property);
        if (str == null) {
            String msg = "getSqlParameterBeanClassDefinitionMap(className).get(property) returned null";
            msg = msg + ": className=" + className + " property=" + property;
            throw new IllegalArgumentException(msg);
        }
        if (str.indexOf("-") == -1) {
            return "";
        }
        return str.substring(str.indexOf("-") + 1);
    }

    public boolean isAvailableSqlParameterBeanGeneration() {
        return !getSqlParameterBeanDefinitionMap().isEmpty();
    }

    public boolean isSqlParameterBeanHaveTheProperty(String className, String property) {
        final String str = (String) getSqlParameterBeanClassDefinitionMap(className).get(property);
        return str != null;
    }

    // ===============================================================================
    //                                                       Properties - ArguemntBean
    //                                                       =========================
    public static final String KEY_argumentBeanDefinitionMap = "argumentBeanDefinitionMap";
    protected Map<String, Object> _argumentBeanDefinitionMap;

    public String getArgumentBeanPackage() {
        return stringProp("torque.argumentBeanPackage", "");
    }

    public Map<String, Object> getArgumentBeanDefinitionMap() {
        if (_argumentBeanDefinitionMap == null) {
            _argumentBeanDefinitionMap = mapProp("torque." + KEY_argumentBeanDefinitionMap, DEFAULT_EMPTY_MAP);
        }
        return _argumentBeanDefinitionMap;
    }

    public List<String> getArgumentBeanClassNameList() {
        return new ArrayList<String>(getArgumentBeanDefinitionMap().keySet());
    }

    public Map<String, String> getArgumentBeanClassDefinitionMap(String className) {
        final Map<String, String> map = (Map<String, String>) getArgumentBeanDefinitionMap().get(className);
        if (map == null) {
            String msg = "getArgumentBeanDifinitionMap().get(className) returned null: " + className;
            throw new IllegalArgumentException(msg);
        }
        return map;
    }

    public String getArgumentBeanPropertyType(String className, String property) {
        final String str = (String) getArgumentBeanClassDefinitionMap(className).get(property);
        if (str == null) {
            String msg = "getArgumentBeanClassDefinitionMap(className).get(property) returned null";
            msg = msg + ": className=" + className + " property=" + property;
            throw new IllegalArgumentException(msg);
        }
        if (str.indexOf("-") == -1) {
            return str;
        }
        return str.substring(0, str.indexOf("-"));
    }

    public boolean isArgumentBeanPropertyDefaultValueEffective(String className, String property) {
        return getArgumentBeanPropertyDefaultValue(className, property).trim().length() != 0;
    }

    public String getArgumentBeanPropertyDefaultValue(String className, String property) {
        final String str = (String) getArgumentBeanClassDefinitionMap(className).get(property);
        if (str == null) {
            String msg = "getArgumentBeanClassDefinitionMap(className).get(property) returned null";
            msg = msg + ": className=" + className + " property=" + property;
            throw new IllegalArgumentException(msg);
        }
        if (str.indexOf("-") == -1) {
            return "";
        }
        return str.substring(str.indexOf("-") + 1);
    }

    public boolean isAvailableArgumentBeanGeneration() {
        return !getArgumentBeanDefinitionMap().isEmpty();
    }

    public static final String KEY_argumentBeanRelatedSqlParameterMap = "argumentBeanRelatedSqlParameterMap";
    protected Map<String, Object> _argumentBeanRelatedSqlParameterMap;

    public Map<String, Object> getArgumentBeanRelatedSqlParameterMap() {
        if (_argumentBeanRelatedSqlParameterMap == null) {
            _argumentBeanRelatedSqlParameterMap = mapProp("torque." + KEY_argumentBeanRelatedSqlParameterMap,
                    DEFAULT_EMPTY_MAP);
        }
        return _argumentBeanRelatedSqlParameterMap;
    }

    public List<String> getArgumentBeanRelatedSqlParameterSqlParameterNameList(String argumentBeanName) {
        return (List<String>) getArgumentBeanRelatedSqlParameterMap().get(argumentBeanName);
    }

    // ===============================================================================
    //                                             Properties - OriginalBehaviorAspect
    //                                             ===================================
    public static final String KEY_originalBehaviorAspectMap = "originalBehaviorAspectMap";
    protected Map<String, Map<String, String>> _originalBehaviorAspectMap;

    public Map<String, Map<String, String>> getOriginalBehaviorAspectMap() {
        if (_originalBehaviorAspectMap == null) {
            _originalBehaviorAspectMap = new LinkedHashMap<String, Map<String, String>>();

            final Map<String, Object> generatedMap = mapProp("torque." + KEY_originalBehaviorAspectMap,
                    DEFAULT_EMPTY_MAP);
            final Set<String> keySet = generatedMap.keySet();
            for (String key : keySet) {
                final Map<String, String> aspectDefinition = (Map<String, String>) generatedMap.get(key);
                _originalBehaviorAspectMap.put(key, aspectDefinition);
            }
        }
        return _originalBehaviorAspectMap;
    }

    public List<String> getOriginalBehaviorAspectComponentNameList() {
        return new ArrayList<String>(getOriginalBehaviorAspectMap().keySet());
    }

    public String getOriginalBehaviorAspectClassName(String componentName) {
        final Map<String, String> aspectDefinition = getOriginalBehaviorAspectMap().get(componentName);
        return aspectDefinition.get("className");
    }

    public String getOriginalBehaviorAspectPointcut(String componentName) {
        final Map<String, String> aspectDefinition = getOriginalBehaviorAspectMap().get(componentName);
        return aspectDefinition.get("pointcut");
    }

    // ===============================================================================
    //                                                      Properties - ExtractAccept
    //                                                      ==========================

    public String getExtractAcceptStartBrace() {
        return stringProp("torque.extractAcceptStartBrace", "@{");
    }

    public String getExtractAcceptEndBrace() {
        return stringProp("torque.extractAcceptEndBrace", "@}");
    }

    public String getExtractAcceptDelimiter() {
        return stringProp("torque.extractAcceptDelimiter", "@;");
    }

    public String getExtractAcceptEqual() {
        return stringProp("torque.extractAcceptEqual", "@=");
    }
    
    // ===============================================================================
    //                                                              Properties - Other
    //                                                              ==================
    public OtherProperties getOtherProperties() {
        return getHandler().getOtherProperties(getProperties());
    }

    // ===============================================================================
    //                                                      Properties - Database Info
    //                                                      ==========================
    public String getDatabaseDriver() {
        return stringProp("torque.database.driver");
    }

    public String getDatabaseUri() {
        return stringProp("torque.database.url");
    }

    public String getDatabaseUser() {
        return stringProp("torque.database.user");
    }

    public String getDatabasePassword() {
        return stringProp("torque.database.password");
    }

    public Connection getConnection() {
        try {
            Class.forName(getDatabaseDriver());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            return DriverManager.getConnection(getDatabaseUri(), getDatabaseUser(), getDatabasePassword());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ===============================================================================
    //                                                        Properties - TableExcept
    //                                                        ========================
    protected List<String> _tableExceptList;

    public List<String> getTableExceptList() {
        if (_tableExceptList == null) {
            final List<Object> tmpLs = listProp("torque.table.except.list", DEFAULT_EMPTY_LIST);
            _tableExceptList = new ArrayList<String>();
            for (Object object : tmpLs) {
                _tableExceptList.add((String) object);
            }
            _tableExceptList.addAll(getTableExceptInformation().getTableExceptList());
        }
        return _tableExceptList;
    }

    protected TableExceptInformation _tableExceptInformation;

    public TableExceptInformation getTableExceptInformation() {
        if (_tableExceptInformation == null) {
            if ("mssql".equals(getBasicProperties().getDatabaseName())) {
                _tableExceptInformation = new TableExceptSQLServer();
            } else {
                _tableExceptInformation = new TableExceptDefault();
            }
        }
        return _tableExceptInformation;
    }

    public static interface TableExceptInformation {
        public List<String> getTableExceptList();
    }

    public static class TableExceptSQLServer implements TableExceptInformation {
        public List<String> getTableExceptList() {
            return Arrays.asList(new String[] { "sysconstraints", "syssegments", "dtproperties" });
        }
    }

    public static class TableExceptDefault implements TableExceptInformation {
        public List<String> getTableExceptList() {
            return Arrays.asList(new String[] {});
        }
    }

    // ===============================================================================
    //                                        Properties - jdbcToJavaNative (Internal)
    //                                        ========================================
    public String getJdbcToJavaNativeAsStringRemovedLineSeparator() {
        final String property = stringProp("torque.jdbcToJavaNativeMap", DEFAULT_EMPTY_MAP_STRING);
        return removeNewLine(property);
    }

    protected Map<String, Object> _jdbcToJavaNativeMap;

    public Map<String, Object> getJdbcToJavaNative() {
        if (_jdbcToJavaNativeMap == null) {
            _jdbcToJavaNativeMap = mapProp("torque.jdbcToJavaNativeMap", getLanguageMetaData().getJdbcToJavaNativeMap());
        }
        return _jdbcToJavaNativeMap;
    }

    protected List<Object> _javaNativeStringList;

    public List<Object> getJavaNativeStringList() {
        if (_javaNativeStringList == null) {
            _javaNativeStringList = listProp("torque.javaNativeStringList", getLanguageMetaData().getStringList());
        }
        return _javaNativeStringList;
    }

    protected List<Object> _javaNativeBooleanList;

    public List<Object> getJavaNativeBooleanList() {
        if (_javaNativeBooleanList == null) {
            _javaNativeBooleanList = listProp("torque.javaNativeBooleanList", getLanguageMetaData().getBooleanList());
        }
        return _javaNativeBooleanList;
    }

    protected List<Object> _javaNativeNumberList;

    public List<Object> getJavaNativeNumberList() {
        if (_javaNativeNumberList == null) {
            _javaNativeNumberList = listProp("torque.javaNativeNumberList", getLanguageMetaData().getNumberList());
        }
        return _javaNativeNumberList;
    }

    protected List<Object> _javaNativeDateList;

    public List<Object> getJavaNativeDateList() {
        if (_javaNativeDateList == null) {
            _javaNativeDateList = listProp("torque.javaNativeDateList", getLanguageMetaData().getDateList());
        }
        return _javaNativeDateList;
    }

    protected List<Object> _javaNativeBinaryList;

    public List<Object> getJavaNativeBinaryList() {
        if (_javaNativeBinaryList == null) {
            _javaNativeBinaryList = listProp("torque.javaNativeBinaryList", getLanguageMetaData().getBinaryList());
        }
        return _javaNativeBinaryList;

    }

    protected LanguageMetaData _languageMetaData;

    protected LanguageMetaData getLanguageMetaData() {
        if (getBasicProperties().isTargetLanguageJava()) {
            if (_languageMetaData == null) {
                _languageMetaData = new JavaMetaData();
            }
        } else if (getBasicProperties().isTargetLanguageCSharp()) {
            if (_languageMetaData == null) {
                _languageMetaData = new CSharpMetaData();
            }
        } else {
            String msg = "The language is unsupported: " + getBasicProperties().getTargetLanguage();
            throw new IllegalStateException(msg);
        }
        return _languageMetaData;
    }

    public static interface LanguageMetaData {

        public Map<String, Object> getJdbcToJavaNativeMap();

        public List<Object> getStringList();

        public List<Object> getBooleanList();

        public List<Object> getNumberList();

        public List<Object> getDateList();

        public List<Object> getBinaryList();
    }

    public static class JavaMetaData implements LanguageMetaData {
        public Map<String, Object> getJdbcToJavaNativeMap() {
            return DEFAULT_EMPTY_MAP;
        }

        public List<Object> getStringList() {
            return Arrays.asList(new Object[] { "String" });
        }

        public List<Object> getBooleanList() {
            return Arrays.asList(new Object[] { "Boolean" });
        }

        public List<Object> getNumberList() {
            return Arrays.asList(new Object[] { "Byte", "Short", "Integer", "Long", "Float", "Double", "BigDecimal",
                    "BigInteger" });
        }

        public List<Object> getDateList() {
            return Arrays.asList(new Object[] { "Date", "Time", "Timestamp" });
        }

        public List<Object> getBinaryList() {
            return Arrays.asList(new Object[] { "byte[]" });
        }
    }

    public static class CSharpMetaData implements LanguageMetaData {
        public Map<String, Object> getJdbcToJavaNativeMap() {
            final Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("CHAR", "String");
            map.put("VARCHAR", "String");
            map.put("LONGVARCHAR", "String");
            map.put("NUMERIC", "Nullables.NullableDecimal");
            map.put("DECIMAL", "Nullables.NullableDecimal");
            map.put("BIT", "Nullables.NullableBoolean");
            map.put("TINYINT", "Nullables.NullableDecimal");
            map.put("SMALLINT", "Nullables.NullableDecimal");
            map.put("INTEGER", "Nullables.NullableDecimal");
            map.put("BIGINT", "Nullables.NullableDecimal");
            map.put("REAL", "Nullables.NullableDecimal");
            map.put("FLOAT", "Nullables.NullableDecimal");
            map.put("DOUBLE", "Nullables.NullableDecimal");
            map.put("DATE", "Nullables.NullableDateTime");
            map.put("TIME", "Nullables.NullableDateTime");
            map.put("TIMESTAMP", "Nullables.NullableDateTime");
            return map;
        }

        public List<Object> getStringList() {
            return Arrays.asList(new Object[] { "String" });
        }

        public List<Object> getBooleanList() {
            return Arrays.asList(new Object[] { "Nullables.NullableBoolean" });
        }

        public List<Object> getNumberList() {
            return Arrays.asList(new Object[] { "Nullables.NullableDecimal" });
        }

        public List<Object> getDateList() {
            return Arrays.asList(new Object[] { "Nullables.NullableDateTime" });
        }

        public List<Object> getBinaryList() {
            return Arrays.asList(new Object[] { "byte[]" });
        }
    }

    // ===============================================================================
    //                      Properties - ToLowerInGeneratorUnderscoreMethod (Internal)
    //                      ==========================================================
    public boolean isAvailableToLowerInGeneratorUnderscoreMethod() {
        return booleanProp("torque.isAvailableToLowerInGeneratorUnderscoreMethod", true);
    }

    // ===============================================================================
    //                                   Properties - invokeReplaceSchemaDefinitionMap
    //                                   =============================================
    public static final String KEY_invokeReplaceSchemaDefinitionMap = "invokeReplaceSchemaDefinitionMap";
    protected Map<String, Object> _invokeReplaceSchemaDefinitionMap;

    public Map<String, Object> getInvokeReplaceSchemaDefinitionMap() {
        if (_invokeReplaceSchemaDefinitionMap == null) {
            _invokeReplaceSchemaDefinitionMap = mapProp("torque." + KEY_invokeReplaceSchemaDefinitionMap,
                    DEFAULT_EMPTY_MAP);
        }
        return _invokeReplaceSchemaDefinitionMap;
    }

    public String getInvokeReplaceSchemaSqlFile() {
        return (String) getInvokeReplaceSchemaDefinitionMap().get("sqlFile");
    }

    public boolean isInvokeReplaceSchemaAutoCommit() {
        final String isAutoCommitString = (String) getInvokeReplaceSchemaDefinitionMap().get("isAutoCommit");
        if (isAutoCommitString != null && isAutoCommitString.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isInvokeReplaceSchemaRollbackOnly() {
        final String isRollbackOnlyString = (String) getInvokeReplaceSchemaDefinitionMap().get("isRollbackOnly");
        if (isRollbackOnlyString != null && isRollbackOnlyString.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isInvokeReplaceSchemaErrorContinue() {
        final String isErrorContinueString = (String) getInvokeReplaceSchemaDefinitionMap().get("isErrorContinue");
        if (isErrorContinueString != null && isErrorContinueString.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    // ===============================================================================
    //                                    Properties - invokeSqlDirectoryDefinitionMap
    //                                    ============================================
    public static final String KEY_invokeSqlDirectoryDefinitionMap = "invokeSqlDirectoryDefinitionMap";
    protected Map<String, Object> _invokeSqlDirectoryDefinitionMap;

    public Map<String, Object> getInvokeSqlDirectoryDefinitionMap() {
        if (_invokeSqlDirectoryDefinitionMap == null) {
            _invokeSqlDirectoryDefinitionMap = mapProp("torque." + KEY_invokeSqlDirectoryDefinitionMap,
                    DEFAULT_EMPTY_MAP);
        }
        return _invokeSqlDirectoryDefinitionMap;
    }

    public String getInvokeSqlDirectorySqlDirectory() {
        return (String) getInvokeSqlDirectoryDefinitionMap().get("sqlDirectory");
    }

    public boolean isInvokeSqlDirectoryAutoCommit() {
        final String isAutoCommitString = (String) getInvokeSqlDirectoryDefinitionMap().get("isAutoCommit");
        if (isAutoCommitString != null && isAutoCommitString.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isInvokeSqlDirectoryRollbackOnly() {
        final String isRollbackOnlyString = (String) getInvokeSqlDirectoryDefinitionMap().get("isRollbackOnly");
        if (isRollbackOnlyString != null && isRollbackOnlyString.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isInvokeSqlDirectoryErrorContinue() {
        final String isErrorContinueString = (String) getInvokeSqlDirectoryDefinitionMap().get("isErrorContinue");
        if (isErrorContinueString != null && isErrorContinueString.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    // ===============================================================================
    //                                            Properties - sql2EntityDefinitionMap
    //                                            ====================================
    public Sql2EntityProperties getSql2EntityProperties() {
        return getHandler().getSql2EntityProperties(getProperties());
    }

    // **********************************************************************************************
    //                                                                                         Helper
    //                                                                                         ******

    // ===============================================================================
    //                                                                          String
    //                                                                          ======
    public String filterDoubleQuotation(String str) {
        return DfPropertyUtil.convertAll(str, "\"", "'");
    }

    public String removeNewLine(String str) {
        return DfPropertyUtil.removeAll(str, System.getProperty("line.separator"));
    }

}