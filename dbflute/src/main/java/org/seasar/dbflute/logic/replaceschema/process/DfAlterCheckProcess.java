package org.seasar.dbflute.logic.replaceschema.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfAlterCheckAlterNGMarkFoundException;
import org.seasar.dbflute.exception.DfAlterCheckAlterScriptSQLException;
import org.seasar.dbflute.exception.DfAlterCheckAlterSqlFailureException;
import org.seasar.dbflute.exception.DfAlterCheckCreateNGMarkFoundException;
import org.seasar.dbflute.exception.DfAlterCheckDataSourceNotFoundException;
import org.seasar.dbflute.exception.DfAlterCheckDifferenceFoundException;
import org.seasar.dbflute.exception.DfAlterCheckReplaceSchemaFailureException;
import org.seasar.dbflute.exception.DfAlterCheckRollbackSchemaFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireMan;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireResult;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunner;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerResult;
import org.seasar.dbflute.helper.process.ProcessResult;
import org.seasar.dbflute.helper.process.SystemScript;
import org.seasar.dbflute.logic.jdbc.schemadiff.DfNextPreviousDiff;
import org.seasar.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.seasar.dbflute.logic.jdbc.schemaxml.DfSchemaXmlSerializer;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfAlterSchemaFinalInfo;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfAlterCheckProcess extends DfAbstractReplaceSchemaProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfAlterCheckProcess.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final DataSource _dataSource;
    protected final UnifiedSchema _mainSchema;
    protected final CoreProcessPlayer _coreProcessPlayer;

    // -----------------------------------------------------
    //                                          Renamed File
    //                                          ------------
    protected final Map<File, File> _backupPreviousFileMap = new LinkedHashMap<File, File>();
    protected final Map<File, File> _deployedNextFileMap = new LinkedHashMap<File, File>();

    // -----------------------------------------------------
    //                                                Status
    //                                                ------
    protected boolean _rolledBack;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfAlterCheckProcess(DataSource dataSource, UnifiedSchema mainSchema, CoreProcessPlayer coreProcessPlayer) {
        if (dataSource == null) { // for example, ReplaceSchema may have lazy connection
            throwAlterCheckDataSourceNotFoundException();
        }
        _dataSource = dataSource;
        _mainSchema = mainSchema;
        _coreProcessPlayer = coreProcessPlayer;
    }

    protected void throwAlterCheckDataSourceNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the data source for AlterCheck (or ChangeOutput).");
        br.addItem("Advice");
        br.addElement("Make sure your database process works");
        br.addElement("or your connection settings are correct.");
        String msg = br.buildExceptionMessage();
        throw new DfAlterCheckDataSourceNotFoundException(msg);
    }

    public static DfAlterCheckProcess createAsMain(DataSource dataSource, CoreProcessPlayer coreProcessPlayer) {
        final UnifiedSchema mainSchema = getDatabaseProperties().getDatabaseSchema();
        return new DfAlterCheckProcess(dataSource, mainSchema, coreProcessPlayer);
    }

    public static interface CoreProcessPlayer {
        void play();

        void rollback();
    }

    // ===================================================================================
    //                                                                             Process
    //                                                                             =======
    public DfAlterSchemaFinalInfo checkAlter() {
        processReady();

        final DfAlterSchemaFinalInfo finalInfo = alterSchema();
        if (finalInfo.isFailure()) {
            return finalInfo;
        }
        replaceSchema(finalInfo);
        if (finalInfo.isFailure()) {
            return finalInfo;
        }

        deleteAlterCheckResultDiff(); // to replace the result file
        final DfSchemaDiff schemaDiff = schemaDiff();
        if (schemaDiff.hasDiff()) {
            processDifference(finalInfo, schemaDiff);
        } else {
            processSuccess();
        }

        // not allowed to be executed when processes before abort
        // for unanticipated accidents (not to delete previous files)
        deleteTemporaryResource();
        return finalInfo;
    }

    public DfAlterSchemaFinalInfo outputChange() { // sub-process
        processReady();

        final DfAlterSchemaFinalInfo finalInfo = new DfAlterSchemaFinalInfo();
        finalInfo.setResultMessage("{Change Output}");

        deleteChangeOutputResultDiff(); // to replace the result file
        final String resultDiff = getMigrationChangeOutputResultDiff();
        final DfSchemaXmlSerializer previousSerializer = createSchemaXmlSerializer(resultDiff);
        previousSerializer.serialize();
        replaceSchema(finalInfo);
        if (finalInfo.isFailure()) {
            return finalInfo;
        }

        // success here
        deleteCreateNGMark();
        try {
            final DfSchemaXmlSerializer replacedSerializer = createSchemaXmlSerializer(resultDiff);
            replacedSerializer.serialize();
            finalInfo.addDetailMessage("o (success)");
        } finally {
            rollbackSchema();
        }

        deleteTemporaryResource();
        return finalInfo;
    }

    // ===================================================================================
    //                                                                               Ready
    //                                                                               =====
    protected void processReady() {
        verifyPremise();

        // resources may remain if previous execution aborts
        deleteTemporaryResource();
    }

    protected void verifyPremise() {
        // *because NG marks are only for notice (not for restriction)
        //if (hasMigrationAlterNGMark()) {
        //    throwAlterCheckAlterNGMarkFoundException();
        //}
        //if (hasMigrationCreateNGMark()) {
        //    throwAlterCheckCreateNGMarkFoundException();
        //}

        deleteAllNGMark(); // instead of
    }

    protected void throwAlterCheckAlterNGMarkFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the alter-NG mark of AlterCheck.");
        br.addItem("Advice");
        setupFixedAlterAdviceMessage(br);
        String msg = br.buildExceptionMessage();
        throw new DfAlterCheckAlterNGMarkFoundException(msg);
    }

    protected void throwAlterCheckCreateNGMarkFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the create-NG mark of AlterCheck (or ChangeOutput).");
        br.addItem("Advice");
        setupFixedCreateAdviceMessage(br);
        String msg = br.buildExceptionMessage();
        throw new DfAlterCheckCreateNGMarkFoundException(msg);
    }

    // ===================================================================================
    //                                                                         AlterSchema
    //                                                                         ===========
    protected DfAlterSchemaFinalInfo alterSchema() {
        _log.info("");
        _log.info("* * * * * * * * *");
        _log.info("*               *");
        _log.info("* Alter Schema  *");
        _log.info("*               *");
        _log.info("* * * * * * * * *");
        final DfAlterSchemaFinalInfo finalInfo = executeAlterSql();
        if (finalInfo.isFailure()) {
            markAlterNG(getAlterCheckAlterSqlFailureNotice());
            rollbackSchema();
            setupAlterCheckAlterSqlFailureException(finalInfo);
        } else {
            serializeAlteredSchema();
        }
        return finalInfo;
    }

    protected DfAlterSchemaFinalInfo executeAlterSql() {
        final List<File> alterSqlFileList = getMigrationAlterSqlFileList();
        final DfRunnerInformation runInfo = createRunnerInformation();
        final DfSqlFileFireMan fireMan = createSqlFileFireMan();
        fireMan.setExecutorName("Alter Schema");
        final DfSqlFileRunner runner = new DfSqlFileRunnerExecute(runInfo, _dataSource);
        final DfSqlFileFireResult result = fireMan.fire(runner, alterSqlFileList);
        return createFinalInfo(result);
    }

    protected DfSqlFileFireMan createSqlFileFireMan() {
        final String[] scriptExtAry = SystemScript.getSupportedExtList().toArray(new String[] {});
        final SystemScript script = new SystemScript();
        return new DfSqlFileFireMan() {
            @Override
            protected DfSqlFileRunnerResult processSqlFile(DfSqlFileRunner runner, File sqlFile) {
                final String path = sqlFile.getPath();
                if (!Srl.endsWith(path, scriptExtAry)) { // SQL file
                    return super.processSqlFile(runner, sqlFile);
                }
                // script file
                final String resolvedPath = Srl.replace(path, "\\", "/");
                final String baseDir = Srl.substringLastFront(resolvedPath, "/");
                final String scriptName = Srl.substringLastRear(resolvedPath, "/");
                final ProcessResult processResult = script.execute(new File(baseDir), scriptName);
                if (processResult.isSystemMismatch()) {
                    _log.info("...Skipping the script for system mismatch: " + scriptName);
                    return null;
                }
                final String console = processResult.getConsole();
                if (Srl.is_NotNull_and_NotTrimmedEmpty(console)) {
                    _log.info("...Reading console for " + scriptName + ":" + ln() + console);
                }
                final DfSqlFileRunnerResult runnerResult = new DfSqlFileRunnerResult(sqlFile);
                runnerResult.setTotalSqlCount(1);
                final int exitCode = processResult.getExitCode();
                if (exitCode != 0) {
                    final String msg = "The script failed: " + scriptName + " exitCode=" + exitCode;
                    final SQLException sqlEx = new DfAlterCheckAlterScriptSQLException(msg);
                    final String sqlExp = "(commands on the script)";
                    runnerResult.addErrorContinuedSql(sqlExp, sqlEx);
                    return runnerResult;
                } else {
                    runnerResult.setGoodSqlCount(1);
                    return runnerResult;
                }
            }
        };
    }

    protected void serializeAlteredSchema() {
        final DfSchemaXmlSerializer serializer = createSchemaXmlSerializer();
        serializer.serialize();
    }

    protected DfSchemaXmlSerializer createSchemaXmlSerializer() {
        final String schemaXml = getMigrationSchemaXml();
        final String diffFile = getMigrationAlterCheckResultDiff();
        return DfSchemaXmlSerializer.createAsManage(_dataSource, _mainSchema, schemaXml, diffFile);
    }

    protected DfSchemaXmlSerializer createSchemaXmlSerializer(String diffFile) {
        final String schemaXml = getMigrationSchemaXml();
        return DfSchemaXmlSerializer.createAsManage(_dataSource, _mainSchema, schemaXml, diffFile);
    }

    protected void setupAlterCheckAlterSqlFailureException(DfAlterSchemaFinalInfo finalInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(getAlterCheckAlterSqlFailureNotice());
        br.addItem("Advice");
        setupFixedAlterAdviceMessage(br);
        br.addElement("Look at the final info in the log for DBFlute task.");
        br.addItem("Message");
        br.addElement(finalInfo.getResultMessage());
        String msg = br.buildExceptionMessage();
        finalInfo.setAlterSqlFailureEx(new DfAlterCheckAlterSqlFailureException(msg));
        finalInfo.setFailure(true);
    }

    protected String getAlterCheckAlterSqlFailureNotice() {
        return "Failed to execute the alter SQL statements.";
    }

    // ===================================================================================
    //                                                                       ReplaceSchema
    //                                                                       =============
    protected void replaceSchema(DfAlterSchemaFinalInfo finalInfo) {
        _log.info("");
        _log.info("* * * * * * * * * *");
        _log.info("*                 *");
        _log.info("* Replace Schema  *");
        _log.info("*                 *");
        _log.info("* * * * * * * * * *");
        try {
            backupPreviousResource();
            deployNextResource();
            _coreProcessPlayer.play();
        } catch (RuntimeException threwLater) {
            markCreateNG(getAlterCheckReplaceSchemaFailureNotice());
            rollbackSchema();
            setupAlterCheckReplaceSchemaFailureException(finalInfo, threwLater);
        }
    }

    protected void backupPreviousResource() {
        doBackupPreviousResource(getMigrationReplaceSchemaSqlFileMap(), getReplaceSchemaSqlFileMap());
        doBackupPreviousResource(getMigrationTakeFinallySqlFileMap(), getTakeFinallySqlFileMap());
        doBackupPreviousResource(getMigrationSchemaDataAllMap(), getSchemaDataAllMap());
    }

    protected void doBackupPreviousResource(Map<String, File> migrationFileMap, Map<String, File> previousFileMap) {
        final String previousDir = getMigrationTemporaryPreviousDirectory();
        final String playSqlDirSymbol = getPlaySqlDirSymbol();
        for (Entry<String, File> entry : migrationFileMap.entrySet()) {
            final String uniqueKey = entry.getKey();
            final File previousFile = previousFileMap.get(uniqueKey);
            if (previousFile != null) { // found overridden previous SQL file
                final String backupPath = buildMoveToPath(previousFile, previousDir, playSqlDirSymbol);
                mkdirsFileIfNotExists(backupPath);
                final File backupTo = new File(backupPath);
                _log.info("...Moving the previous file to backup: " + backupPath);
                if (previousFile.renameTo(backupTo)) {
                    _backupPreviousFileMap.put(previousFile, backupTo);
                } else {
                    String msg = "Failed to rename (for backup) to " + backupTo;
                    throw new IllegalStateException(msg);
                }
            }
        }
    }

    protected void deployNextResource() {
        doDeployNextResource(getMigrationReplaceSchemaSqlFileMap());
        doDeployNextResource(getMigrationTakeFinallySqlFileMap());
        doDeployNextResource(getMigrationSchemaDataAllMap());
    }

    protected void doDeployNextResource(Map<String, File> migrationFileMap) {
        final String playSqlDir = getPlaySqlDirectory();
        final String createDirSymbol = getMigrationCreateDirSymbol(); // migration/create
        for (File migrationFile : migrationFileMap.values()) {
            final String deployPath = buildMoveToPath(migrationFile, playSqlDir, createDirSymbol);
            mkdirsFileIfNotExists(deployPath);
            final File deployTo = new File(deployPath);
            _log.info("...Moving the next file to deployment: " + deployPath);
            if (migrationFile.renameTo(deployTo)) {
                _deployedNextFileMap.put(migrationFile, deployTo);
            } else {
                String msg = "Failed to rename (for deployment) to " + deployPath;
                throw new IllegalStateException(msg);
            }
        }
    }

    protected String buildMoveToPath(File sourceFile, String destBaseDir, String pointDirSymbol) {
        // e.g. when next resource
        // /Users/.../dbflute_exampledb/playsql/migration/create/replace-schema.sql
        // /Users/.../dbflute_exampledb/playsql/migration/create/data/common/xls/10-master.xls
        final String absolutePath = Srl.replace(sourceFile.getAbsolutePath(), "\\", "/");

        // e.g. when next resource
        // replace-schema.sql
        // data/common/xls/10-master.xls
        final String relativePath = Srl.substringLastRear(absolutePath, "/" + pointDirSymbol + "/");

        // e.g. when next resource
        // playsql/replace-schema.sql
        // playsql/data/common/xls/10-master.xls
        return destBaseDir + "/" + relativePath;
    }

    protected void markCreateNG(String notice) {
        final String ngMark = getMigrationCreateNGMark();
        try {
            final File markFile = new File(ngMark);
            if (!markFile.exists()) {
                _log.info("...Marking create-NG: " + ngMark);
                markFile.createNewFile();
                writeNotice(markFile, notice);
            }
        } catch (IOException e) {
            String msg = "Failed to create a file for create-NG mark: " + ngMark;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void setupAlterCheckReplaceSchemaFailureException(DfAlterSchemaFinalInfo finalInfo, RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(getAlterCheckReplaceSchemaFailureNotice());
        br.addItem("Advice");
        br.addElement("Make sure your create SQL or data files are correct,");
        br.addElement("and after that, execute ReplaceSchema again.");
        String msg = br.buildExceptionMessage();
        finalInfo.setReplaceSchemaFailureEx(new DfAlterCheckReplaceSchemaFailureException(msg, e));
        finalInfo.setFailure(true);
        finalInfo.addDetailMessage("x (create failure)");
    }

    protected String getAlterCheckReplaceSchemaFailureNotice() {
        return "Failed to replace the schema using create SQL.";
    }

    // ===================================================================================
    //                                                                          SchemaDiff
    //                                                                          ==========
    protected DfSchemaDiff schemaDiff() {
        _log.info("");
        _log.info("* * * * * * * *");
        _log.info("*             *");
        _log.info("* Schema Diff *");
        _log.info("*             *");
        _log.info("* * * * * * * *");
        final DfSchemaXmlSerializer serializer = createSchemaXmlSerializer();
        serializer.serialize();
        return serializer.getSchemaDiff();
    }

    // ===================================================================================
    //                                                                     Different Story
    //                                                                     ===============
    protected void processDifference(DfAlterSchemaFinalInfo finalInfo, DfSchemaDiff schemaDiff) {
        _log.info("");
        _log.info("* * * * * * * * * *");
        _log.info("*                 *");
        _log.info("* Different Story *");
        _log.info("*                 *");
        _log.info("* * * * * * * * * *");
        markAlterNG(getAlterDiffNotice());
        rollbackSchema();
        handleAlterDiff(finalInfo, schemaDiff);
    }

    protected void markAlterNG(String notice) {
        final String ngMark = getMigrationAlterNGMark();
        try {
            final File markFile = new File(ngMark);
            if (!markFile.exists()) {
                _log.info("...Marking alter-NG: " + ngMark);
                markFile.createNewFile();
                writeNotice(markFile, notice);
            }
        } catch (IOException e) {
            String msg = "Failed to create a file for alter-NG mark: " + ngMark;
            throw new IllegalStateException(msg, e);
        }
    }

    // -----------------------------------------------------
    //                                      Roll-back Schema
    //                                      ----------------
    protected void rollbackSchema() {
        if (_rolledBack) { // duplication check
            String msg = "Already rolled-back!";
            throw new IllegalStateException(msg);
        }
        _rolledBack = true;
        _log.info("");
        _log.info("* * * * * * * * * * *");
        _log.info("*                   *");
        _log.info("* Roll-back Schema  *");
        _log.info("*                   *");
        _log.info("* * * * * * * * * * *");
        try {
            revertToPreviousResource();
            _coreProcessPlayer.rollback();
        } catch (RuntimeException e) {
            deleteCreateNGMark(); // create SQL failure may be caused by previous resources
            markPreviousNG(getAlterCheckRollbackSchemaFailureNotice());
            throwAlterCheckRollbackSchemaFailureException(e);
        }
    }

    protected void revertToPreviousResource() {
        for (Entry<File, File> entry : _deployedNextFileMap.entrySet()) {
            final File migration = entry.getKey();
            final File deployment = entry.getValue();
            final String pathDisp = Srl.replace(migration.getPath(), "\\", "/");
            _log.info("...Moving the next file back to migration: " + pathDisp);
            if (!deployment.renameTo(migration)) {
                String msg = "Failed to rename (for reversion) to " + pathDisp;
                throw new IllegalStateException(msg);
            }
        }
        for (Entry<File, File> entry : _backupPreviousFileMap.entrySet()) {
            final File deployment = entry.getKey();
            final File backup = entry.getValue();
            final String pathDisp = Srl.replace(deployment.getPath(), "\\", "/");
            _log.info("...Moving the previous file back to deployment: " + pathDisp);
            if (!backup.renameTo(deployment)) {
                String msg = "Failed to rename (for reversion) to " + pathDisp;
                throw new IllegalStateException(msg);
            }
        }
    }

    protected void markPreviousNG(String notice) {
        final String ngMark = getMigrationPreviousNGMark();
        try {
            final File markFile = new File(ngMark);
            if (!markFile.exists()) {
                _log.info("...Marking previous-NG: " + ngMark);
                markFile.createNewFile();
                writeNotice(markFile, notice);
            }
        } catch (IOException e) {
            String msg = "Failed to create a file for previous-NG mark: " + ngMark;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void throwAlterCheckRollbackSchemaFailureException(RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(getAlterCheckRollbackSchemaFailureNotice());
        br.addItem("Advice");
        br.addElement("The function AlterCheck requires that previous schema is valid.");
        br.addElement("So you should fix the mistakes of SQL statements for the previous schema");
        br.addElement("and replace the schema to previous state by ReplaceSchema.");
        br.addElement("");
        br.addElement("The previous-NG mark file, which supresses AlterCheck process,");
        br.addElement("will be deleted if the execution succeeds.");
        br.addElement("In doing so, you can execute AlterCheck again.");
        final String msg = br.buildExceptionMessage();
        throw new DfAlterCheckRollbackSchemaFailureException(msg, e);
    }

    protected String getAlterCheckRollbackSchemaFailureNotice() {
        return "Failed to rollback the schema to previous state.";
    }

    // -----------------------------------------------------
    //                                    AlterDiff Handling
    //                                    ------------------
    protected void handleAlterDiff(DfAlterSchemaFinalInfo finalInfo, DfSchemaDiff schemaDiff) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(getAlterDiffNotice());
        br.addItem("Advice");
        setupFixedAlterAdviceMessage(br);
        br.addElement("");
        br.addElement("You can confirm the details at");
        br.addElement(" '" + getMigrationAlterCheckResultDiff() + "',");
        br.addItem("Diff Date");
        br.addElement(schemaDiff.getDiffDate());
        final DfNextPreviousDiff tableCountDiff = schemaDiff.getTableCount();
        if (tableCountDiff != null && tableCountDiff.hasDiff()) {
            br.addItem("Table Count");
            br.addElement(tableCountDiff.getPrevious() + " to " + tableCountDiff.getNext());
        }
        final String msg = br.buildExceptionMessage();
        finalInfo.setDiffFoundEx(new DfAlterCheckDifferenceFoundException(msg));
        finalInfo.setFailure(true);
        finalInfo.addDetailMessage("x (found diff)");
    }

    protected String getAlterDiffNotice() {
        return "Found the differences between alter SQL and create SQL.";
    }

    protected void setupFixedAlterAdviceMessage(ExceptionMessageBuilder br) {
        br.addElement("Make sure your alter SQL are correct,");
        br.addElement("and after that, execute ReplaceSchema again.");
    }

    protected void setupFixedCreateAdviceMessage(ExceptionMessageBuilder br) {
        br.addElement("Make sure your create SQL are correct");
        br.addElement("and after that, execute ReplaceSchema again.");
    }

    // ===================================================================================
    //                                                                       Success Story
    //                                                                       =============
    protected void processSuccess() {
        _log.info("");
        _log.info("* * * * * * * * *");
        _log.info("*               *");
        _log.info("* Success Story *");
        _log.info("*               *");
        _log.info("* * * * * * * * *");
        deleteAllNGMark();
        saveHistory();
        deleteDiffResult();
    }

    protected void deleteAllNGMark() {
        deleteAlterNGMark();
        deleteCreateNGMark();
    }

    protected void deleteAlterNGMark() {
        final String alterNGMark = getMigrationAlterNGMark();
        deleteFile(new File(alterNGMark), "...Deleting the alter-NG mark");
    }

    protected void deleteCreateNGMark() {
        final String createNGMark = getMigrationCreateNGMark();
        deleteFile(new File(createNGMark), "...Deleting the create-NG mark");
    }

    protected void saveHistory() {
        final String currentDir = getHistoryCurrentDir();
        final List<File> alterSqlFileList = getMigrationAlterSqlFileList();
        _log.info("...Saving history to " + currentDir);
        for (File sqlFile : alterSqlFileList) {
            final File historyTo = new File(currentDir + "/" + sqlFile.getName());
            _log.info(" " + historyTo.getName());
            sqlFile.renameTo(historyTo); // no check here
        }
    }

    protected String getHistoryCurrentDir() {
        final String historyDir = getMigrationHistoryDirectory();
        final Date currentDate = new Date();
        final String middleDir = DfTypeUtil.toString(currentDate, "yyyyMM");
        mkdirsDirIfNotExists(historyDir + "/" + middleDir);
        // e.g. history/201104/20110429_2247
        final String yyyyMMddHHmm = DfTypeUtil.toString(currentDate, "yyyyMMdd_HHmm");
        final String currentDir = historyDir + "/" + middleDir + "/" + yyyyMMddHHmm;
        mkdirsDirIfNotExists(currentDir);
        return currentDir;
    }

    protected void mkdirsDirIfNotExists(String dirPath) {
        final File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    protected void mkdirsFileIfNotExists(String filePath) {
        final File dir = new File(Srl.substringLastFront(filePath, "/"));
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    protected void deleteDiffResult() {
        deleteAlterCheckResultDiff();
        deleteChangeOutputResultDiff();
    }

    protected void deleteAlterCheckResultDiff() {
        final String diff = getMigrationAlterCheckResultDiff();
        deleteFile(new File(diff), "...Deleting the AlterCheck result diff");
    }

    protected void deleteChangeOutputResultDiff() {
        final String diff = getMigrationChangeOutputResultDiff();
        deleteFile(new File(diff), "...Deleting the ChangeOutput result diff");
    }

    // ===================================================================================
    //                                                                             Closing
    //                                                                             =======
    protected void deleteTemporaryResource() {
        deleteSchemaXml();
        deleteTmpDir();
    }

    protected void deleteSchemaXml() {
        final String schemaXml = getMigrationSchemaXml();
        deleteFile(new File(schemaXml), "...Deleting the SchemaXml file");
    }

    protected void deleteTmpDir() {
        final String dir = getMigrationTemporaryDirectory();
        _log.info("...Deleting the temporary directory: " + dir);
        deleteFileHierarchically(new File(dir));
    }

    // ===================================================================================
    //                                                                          Final Info
    //                                                                          ==========
    protected DfAlterSchemaFinalInfo createFinalInfo(DfSqlFileFireResult fireResult) {
        final DfAlterSchemaFinalInfo finalInfo = new DfAlterSchemaFinalInfo();
        finalInfo.setResultMessage(fireResult.getResultMessage());
        final List<String> detailMessageList = extractDetailMessageList(fireResult);
        for (String detailMessage : detailMessageList) {
            finalInfo.addDetailMessage(detailMessage);
        }
        finalInfo.setFailure(fireResult.existsError());
        return finalInfo;
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected void deleteFile(File file, String msg) {
        if (file.exists()) {
            _log.info(msg + ": " + file.getPath());
            file.delete();
        }
    }

    protected void deleteFileHierarchically(File file) {
        if (file.exists()) {
            // one level only deleted
            final File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File nested : listFiles) {
                    deleteFileHierarchically(nested);
                }
            }
            file.delete();
        }
    }

    protected void writeNotice(File file, String notice) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            bw.write(notice + ln() + "Look at the log for detail.");
            bw.flush();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    // -----------------------------------------------------
    //                                         ReplaceSchema
    //                                         -------------
    // without Application's
    public String getPlaySqlDirectory() {
        return getReplaceSchemaProperties().getPlaySqlDirectory();
    }

    public String getPlaySqlDirSymbol() {
        return getReplaceSchemaProperties().getPlaySqlDirSymbol();
    }

    public Map<String, File> getReplaceSchemaSqlFileMap() {
        return getReplaceSchemaProperties().getReplaceSchemaSqlFileMap();
    }

    public Map<String, File> getTakeFinallySqlFileMap() {
        return getReplaceSchemaProperties().getTakeFinallySqlFileMap();
    }

    protected Map<String, File> getSchemaDataAllMap() {
        return getReplaceSchemaProperties().getSchemaDataAllMap();
    }

    // -----------------------------------------------------
    //                                     Migration NG Mark
    //                                     -----------------
    public String getMigrationAlterNGMark() {
        return getReplaceSchemaProperties().getMigrationAlterNGMark();
    }

    public boolean hasMigrationAlterNGMark() {
        return getReplaceSchemaProperties().hasMigrationAlterNGMark();
    }

    public String getMigrationCreateNGMark() {
        return getReplaceSchemaProperties().getMigrationCreateNGMark();
    }

    public boolean hasMigrationCreateNGMark() {
        return getReplaceSchemaProperties().hasMigrationCreateNGMark();
    }

    public String getMigrationPreviousNGMark() {
        return getReplaceSchemaProperties().getMigrationPreviousNGMark();
    }

    // -----------------------------------------------------
    //                                      Migration Detail
    //                                      ----------------
    public List<File> getMigrationAlterSqlFileList() {
        return getReplaceSchemaProperties().getMigrationAlterSqlFileList();
    }

    public String getMigrationCreateDirSymbol() {
        return getReplaceSchemaProperties().getMigrationCreateDirSymbol();
    }

    public Map<String, File> getMigrationReplaceSchemaSqlFileMap() {
        return getReplaceSchemaProperties().getMigrationReplaceSchemaSqlFileMap();
    }

    public Map<String, File> getMigrationTakeFinallySqlFileMap() {
        return getReplaceSchemaProperties().getMigrationTakeFinallySqlFileMap();
    }

    protected Map<String, File> getMigrationSchemaDataAllMap() {
        return getReplaceSchemaProperties().getMigrationSchemaDataAllMap();
    }

    protected String getMigrationSchemaXml() {
        return getReplaceSchemaProperties().getMigrationSchemaXml();
    }

    protected String getMigrationAlterCheckResultDiff() {
        return getReplaceSchemaProperties().getMigrationAlterCheckResultDiff();
    }

    protected String getMigrationChangeOutputResultDiff() {
        return getReplaceSchemaProperties().getMigrationChangeOutputResultDiff();
    }

    protected String getMigrationHistoryDirectory() {
        return getReplaceSchemaProperties().getMigrationHistoryDirectory();
    }

    public String getMigrationTemporaryDirectory() {
        return getReplaceSchemaProperties().getMigrationTemporaryDirectory();
    }

    public String getMigrationTemporaryPreviousDirectory() {
        return getReplaceSchemaProperties().getMigrationTemporaryPreviousDirectory();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replaceString(String text, String fromText, String toText) {
        return DfStringUtil.replace(text, fromText, toText);
    }
}
