/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.logic.generate.language.typemapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfLanguageTypeMappingInfoPhp implements DfLanguageTypeMappingInfo {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final Map<String, Object> _jdbcToJavaNativeMap;
    static {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("CHAR", "string");
        map.put("VARCHAR", "string");
        map.put("LONGVARCHAR", "string");
        map.put("NUMERIC", "integer");
        map.put("DECIMAL", "double");
        map.put("BIT", "integer");
        map.put("TINYINT", "integer");
        map.put("SMALLINT", "integer");
        map.put("INTEGER", "integer");
        map.put("BIGINT", "integer");
        map.put("REAL", "double");
        map.put("FLOAT", "double");
        map.put("DOUBLE", "double");
        map.put("DATE", "string");
        map.put("TIME", "string");
        map.put("TIMESTAMP", "string");
        _jdbcToJavaNativeMap = map;
    }
    protected static final List<String> _stringList = DfCollectionUtil.newArrayList("string");
    protected static final List<String> _numberList = DfCollectionUtil.newArrayList("integer");
    protected static final List<String> _dateList = DfCollectionUtil.newArrayList("string");
    protected static final List<String> _booleanList = DfCollectionUtil.newArrayList("bool?");
    protected static final List<String> _binaryList = DfCollectionUtil.newArrayList("byte[]");

    // ===================================================================================
    //                                                                        Type Mapping
    //                                                                        ============
    public Map<String, Object> getJdbcToJavaNativeMap() {
        return _jdbcToJavaNativeMap;
    }

    // ===================================================================================
    //                                                                  Native Suffix List
    //                                                                  ==================
    public List<String> getStringList() {
        return _stringList;
    }

    public List<String> getNumberList() {
        return _numberList;
    }

    public List<String> getDateList() {
        return _dateList;
    }

    public List<String> getBooleanList() {
        return _booleanList;
    }

    public List<String> getBinaryList() {
        return _binaryList;
    }

    // ===================================================================================
    //                                                                JDBC Type Adjustment
    //                                                                ====================
    public String getSequenceType() {
        return "integer";
    }

    public String getJdbcTypeOfUUID() {
        return null; // means no mapping
    }
}
