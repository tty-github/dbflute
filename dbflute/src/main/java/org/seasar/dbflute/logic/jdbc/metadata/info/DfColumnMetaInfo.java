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
package org.seasar.dbflute.logic.jdbc.metadata.info;

import java.util.Map;

import org.seasar.dbflute.logic.jdbc.metadata.comment.DfDbCommentExtractor.UserColComments;

/**
 * @author jflute
 */
public class DfColumnMetaInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _columnName;
    protected int _jdbcDefValue;
    protected String _dbTypeName;
    protected int _columnSize;
    protected int _decimalDigits;
    protected boolean _required;
    protected String _columnComment;
    protected String _defaultValue;

    // only when Sql2Entity task
    protected String _sql2entityRelatedTableName;
    protected String _sql2entityRelatedColumnName;
    protected String _sql2entityForcedJavaNative;
    protected boolean _procedureParameter;

    // basically only when procedure parameter
    protected String arrayTypeName;
    protected String structTypeName;

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasColumnComment() {
        return _columnComment != null && _columnComment.trim().length() > 0;
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void acceptColumnComment(Map<String, UserColComments> columnCommentMap) {
        if (columnCommentMap == null) {
            return;
        }
        final UserColComments userColComments = columnCommentMap.get(_columnName);
        if (userColComments == null) {
            return;
        }
        final String comment = userColComments.getComments();
        if (comment != null && comment.trim().length() > 0) {
            _columnComment = comment;
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _columnName + ", " + _dbTypeName + "(" + _columnSize + "," + _decimalDigits + "), "
                + _jdbcDefValue + ", " + _required + ", " + _columnComment + ", " + _defaultValue + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getColumnName() {
        return _columnName;
    }

    public void setColumnName(String columnName) {
        this._columnName = columnName;
    }

    public int getColumnSize() {
        return _columnSize;
    }

    public void setColumnSize(int columnSize) {
        this._columnSize = columnSize;
    }

    public int getDecimalDigits() {
        return _decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this._decimalDigits = decimalDigits;
    }

    public String getDefaultValue() {
        return _defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this._defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return _required;
    }

    public void setRequired(boolean required) {
        this._required = required;
    }

    public int getJdbcDefValue() {
        return _jdbcDefValue;
    }

    public void setJdbcDefValue(int jdbcDefValue) {
        this._jdbcDefValue = jdbcDefValue;
    }

    public String getDbTypeName() {
        return _dbTypeName;
    }

    public void setDbTypeName(String dbTypeName) {
        this._dbTypeName = dbTypeName;
    }

    public String getColumnComment() {
        return _columnComment;
    }

    public void setColumnComment(String columnComment) {
        this._columnComment = columnComment;
    }

    public String getSql2EntityRelatedTableName() {
        return _sql2entityRelatedTableName;
    }

    public void setSql2EntityRelatedTableName(String sql2entityRelatedTableName) {
        this._sql2entityRelatedTableName = sql2entityRelatedTableName;
    }

    public String getSql2EntityRelatedColumnName() {
        return _sql2entityRelatedColumnName;
    }

    public void setSql2EntityRelatedColumnName(String sql2entityRelatedColumnName) {
        this._sql2entityRelatedColumnName = sql2entityRelatedColumnName;
    }

    public String getSql2EntityForcedJavaNative() {
        return _sql2entityForcedJavaNative;
    }

    public void setSql2EntityForcedJavaNative(String sql2entityForcedJavaNative) {
        this._sql2entityForcedJavaNative = sql2entityForcedJavaNative;
    }

    public boolean isProcedureParameter() {
        return _procedureParameter;
    }

    public void setProcedureParameter(boolean procedureParameter) {
        this._procedureParameter = procedureParameter;
    }

    public String getArrayTypeName() {
        return arrayTypeName;
    }

    public void setArrayTypeName(String arrayTypeName) {
        this.arrayTypeName = arrayTypeName;
    }

    public String getStructTypeName() {
        return structTypeName;
    }

    public void setStructTypeName(String structTypeName) {
        this.structTypeName = structTypeName;
    }
}
