/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.helper.jdbc.sqlfile;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jflute
 * @since 0.9.4 (2009/03/31 Tuesday)
 */
public class DfSqlFileRunnerResult {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final File _sqlFile;
    protected final List<ErrorContinuedSql> _errorContinuedSqlList = new ArrayList<ErrorContinuedSql>();
    protected int _goodSqlCount = 0;
    protected int _totalSqlCount = 0;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSqlFileRunnerResult(File sqlFile) {
        _sqlFile = sqlFile;
    }

    public static class ErrorContinuedSql {
        protected String _sql;
        protected SQLException _sqlEx;

        public String getSql() {
            return _sql;
        }

        public void setSql(String sql) {
            this._sql = sql;
        }

        public SQLException getSqlEx() {
            return _sqlEx;
        }

        public void setSqlEx(SQLException ex) {
            _sqlEx = ex;
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public File getSqlFile() {
        return _sqlFile;
    }

    public List<ErrorContinuedSql> getErrorContinuedSqlList() {
        return _errorContinuedSqlList;
    }

    public void addErrorContinuedSql(String sql, SQLException sqlEx) {
        final ErrorContinuedSql errorContinuedSql = new ErrorContinuedSql();
        errorContinuedSql.setSql(sql);
        errorContinuedSql.setSqlEx(sqlEx);
        _errorContinuedSqlList.add(errorContinuedSql);
    }

    public int getGoodSqlCount() {
        return _goodSqlCount;
    }

    public void setGoodSqlCount(int goodSqlCount) {
        this._goodSqlCount = goodSqlCount;
    }

    public int getTotalSqlCount() {
        return _totalSqlCount;
    }

    public void setTotalSqlCount(int totalSqlCount) {
        this._totalSqlCount = totalSqlCount;
    }
}
