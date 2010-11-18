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
package org.seasar.dbflute.s2dao.valuetype.basic;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.seasar.dbflute.s2dao.valuetype.TnAbstractValueType;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * {Refers to Seasar and Extends its class}
 * @author jflute
 */
public class BooleanType extends TnAbstractValueType {

    public BooleanType() {
        super(Types.BOOLEAN);
    }

    public Object getValue(ResultSet rs, int index) throws SQLException {
        return DfTypeUtil.toBoolean(rs.getObject(index));
    }

    public Object getValue(ResultSet rs, String columnName) throws SQLException {
        return DfTypeUtil.toBoolean(rs.getObject(columnName));
    }

    public Object getValue(CallableStatement cs, int index) throws SQLException {
        return DfTypeUtil.toBoolean(cs.getObject(index));
    }

    public Object getValue(CallableStatement cs, String parameterName) throws SQLException {
        return DfTypeUtil.toBoolean(cs.getObject(parameterName));
    }

    public void bindValue(Connection conn, PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            setNull(ps, index);
        } else {
            ps.setBoolean(index, DfTypeUtil.toPrimitiveBoolean(value));
        }
    }

    public void bindValue(Connection conn, CallableStatement cs, String parameterName, Object value)
            throws SQLException {
        if (value == null) {
            setNull(cs, parameterName);
        } else {
            cs.setBoolean(parameterName, DfTypeUtil.toPrimitiveBoolean(value));
        }
    }
}