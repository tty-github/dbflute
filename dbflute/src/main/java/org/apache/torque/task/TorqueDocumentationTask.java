package org.apache.torque.task;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.Database;
import org.apache.velocity.anakia.Escape;
import org.apache.velocity.context.Context;
import org.seasar.dbflute.logic.doc.dataxls.DfDataXlsGenerator;
import org.seasar.dbflute.logic.doc.dataxls.DfDataXlsProcess;
import org.seasar.dbflute.logic.jdbc.schemaxml.DfSchemaXmlReader;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.task.bs.DfAbstractDbMetaTexenTask;

/**
 * The DBFlute task generating documentations, SchemaHTML, HistoryHTML and DataXlsTemplate.
 * @author Modified by jflute
 */
public class TorqueDocumentationTask extends DfAbstractDbMetaTexenTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(TorqueDocumentationTask.class);

    // ===================================================================================
    //                                                                         Data Source
    //                                                                         ===========
    @Override
    protected boolean isUseDataSource() {
        // at old age, this is false, but after all, classification needs a connection 
        return true;
    }

    // ===================================================================================
    //                                                                          Schema XML
    //                                                                          ==========
    @Override
    protected DfSchemaXmlReader createSchemaXmlReader() {
        return createSchemaXmlReaderAsCoreToManage();
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        processSchemaHtml();

        if (isDataXlsTemplateRecordLimitValid()) {
            processDataXlsTemplate();
        }

        // It doesn't refresh because it's heavy.
        // After all the generate task will do it at once after doc task.
        //refreshResources();
    }

    protected void processSchemaHtml() {
        _log.info("");
        _log.info("* * * * * * * * * * *");
        _log.info("*                   *");
        _log.info("*    Schema HTML    *");
        _log.info("*                   *");
        _log.info("* * * * * * * * * * *");
        super.doExecute();
        _log.info("");
    }

    protected void processDataXlsTemplate() {
        _log.info("* * * * * * * * * * *");
        _log.info("*                   *");
        if (isDataXlsTemplateLoadDataReverse()) {
            _log.info("* Load Data Reverse *");
        } else {
            _log.info("* Data Xls Template *");
        }
        _log.info("*                   *");
        _log.info("* * * * * * * * * * *");
        final Database database = _schemaData.getDatabase();
        final String title = isDataXlsTemplateLoadDataReverse() ? "migration-data" : "dataxls";
        _log.info("...Outputting " + title + ": tables=" + database.getTableList().size());
        outputDataXlsTemplate(database);
        _log.info("");
    }

    protected void outputDataXlsTemplate(Database database) {
        final DfDataXlsGenerator handler = new DfDataXlsGenerator(getDataSource());
        handler.setContainsCommonColumn(isDataXlsTemplateContainsCommonColumn());
        handler.setManagedTableOnly(isDataXlsTemplateManagedTableOnly());
        handler.setDelimiterDataOutputDir(getDataDelimiterTemplateDir());
        // changes to TSV for compatibility of copy and paste to excel @since 0.9.8.3
        //handler.setDelimiterDataTypeCsv(true);
        final String templateDir = getDataXlsTemplateDir();
        final String fileTitle = getDataXlsTemplateFileTitle();
        final int limit = getDataXlsTemplateRecordLimit();
        final DfDataXlsProcess generator = new DfDataXlsProcess(handler, templateDir, fileTitle, limit);
        generator.execute(database);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected boolean isDataXlsTemplateRecordLimitValid() {
        return getDocumentProperties().isDataXlsTemplateRecordLimitValid();
    }

    protected int getDataXlsTemplateRecordLimit() {
        return getDocumentProperties().getDataXlsTemplateRecordLimit();
    }

    protected boolean isDataXlsTemplateContainsCommonColumn() {
        return getDocumentProperties().isDataXlsTemplateContainsCommonColumn();
    }

    protected boolean isDataXlsTemplateManagedTableOnly() {
        return getDocumentProperties().isDataXlsTemplateManagedTableOnly();
    }

    protected String getDataXlsTemplateDir() {
        return getDocumentProperties().getDataXlsTemplateDir();
    }

    protected String getDataDelimiterTemplateDir() {
        return getDocumentProperties().getDataDelimiterTemplateDir();
    }

    protected String getDataXlsTemplateFileTitle() {
        return getDocumentProperties().getDataXlsTemplateFileTitle();
    }

    protected boolean isDataXlsTemplateLoadDataReverse() {
        return getDocumentProperties().isDataXlsTemplateLoadDataReverse();
    }

    // ===================================================================================
    //                                                                       Task Override
    //                                                                       =============
    @Override
    public Context initControlContext() throws Exception {
        super.initControlContext();
        _context.put("escape", new Escape());
        return _context;
    }
}
