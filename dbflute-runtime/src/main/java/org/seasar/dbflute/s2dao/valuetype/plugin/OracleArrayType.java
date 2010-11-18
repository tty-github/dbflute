/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.s2dao.valuetype.plugin;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

import org.seasar.dbflute.s2dao.valuetype.TnAbstractValueType;

/**
 * @author jflute
 */
public abstract class OracleArrayType extends TnAbstractValueType {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _arrayTypeName;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OracleArrayType(String arrayTypeName) {
        super(Types.ARRAY);
        _arrayTypeName = arrayTypeName;
    }

    // ===================================================================================
    //                                                                           Get Value
    //                                                                           =========
    public Object getValue(ResultSet rs, int index) throws SQLException {
        return rs.getArray(index);
    }

    public Object getValue(ResultSet rs, String columnName) throws SQLException {
        return rs.getArray(columnName);
    }

    public Object getValue(CallableStatement cs, int index) throws SQLException {
        return cs.getArray(index);
    }

    public Object getValue(CallableStatement cs, String parameterName) throws SQLException {
        return cs.getArray(parameterName);
    }

    // ===================================================================================
    //                                                                          Bind Value
    //                                                                          ==========
    public void bindValue(Connection conn, PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) { // basically for insert and update
            setNull(ps, index);
        } else {
            ps.setObject(index, toOracleArray(conn, _arrayTypeName, toArray(value)));
        }
    }

    public void bindValue(Connection conn, CallableStatement cs, String parameterName, Object value)
            throws SQLException {
        if (value == null) {
            setNull(cs, parameterName);
        } else {
            cs.setObject(parameterName, toOracleArray(conn, _arrayTypeName, toArray(value)));
        }
    }

    protected Object toArray(Object value) {
        if (value instanceof Collection<?>) {
            return ((Collection<?>) value).toArray();
        }
        return value;
    }

    // ===================================================================================
    //                                                                       Oracle's Type
    //                                                                       =============
    /**
     * Convert the array value to Oracle's array.
     * @param conn The connection for the database. (NotNull)
     * @param arrayTypeName The name of array type for Oracle. (NotNull)
     * @param arrayValue The value of array. (NotNull) 
     * @return The instance of oracle.sql.ARRAY for the array argument. (NotNull)
     */
    protected abstract Object toOracleArray(Connection conn, String arrayTypeName, Object arrayValue);
}