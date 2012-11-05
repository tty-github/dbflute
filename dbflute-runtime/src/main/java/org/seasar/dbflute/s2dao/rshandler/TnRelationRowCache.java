/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.s2dao.rshandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyType;

/**
 * This is not thread safe.
 * @author modified by jflute (originated in S2Dao)
 */
public class TnRelationRowCache {

    /** The list of row map. map:{relationPath = map:{relationKey = row}} (NotNull: if canRowCache is true) */
    private final Map<String, Map<TnRelationKey, Object>> _rowMap;

    /**
     * @param size The size of relation.
     * @param canRowCache Can the relation row cache?
     */
    public TnRelationRowCache(int size, boolean canRowCache) {
        _rowMap = canRowCache ? new HashMap<String, Map<TnRelationKey, Object>>() : null;
    }

    /**
     * Get relation row from cache by relation key.
     * @param relationNoSuffix The relation No suffix that indicates the location of the relation.
     * @param relKey The key of relation. (NotNull)
     * @return The relation row. (NullAllowed)
     */
    public Object getRelationRow(String relationNoSuffix, TnRelationKey relKey) {
        if (_rowMap == null) {
            return null;
        }
        final Map<TnRelationKey, Object> elementMap = _rowMap.get(relationNoSuffix);
        if (elementMap == null) {
            return null;
        }
        return elementMap.get(relKey);
    }

    /**
     * Add relation row to cache.
     * @param relationNoSuffix The relation No suffix that indicates the location of the relation.
     * @param relKey The key of relation. (NotNull)
     * @param relationRow The relation row. (NullAllowed)
     */
    public void addRelationRow(String relationNoSuffix, TnRelationKey relKey, Object relationRow) {
        if (_rowMap == null) {
            return;
        }
        Map<TnRelationKey, Object> elementMap = _rowMap.get(relationNoSuffix);
        if (elementMap == null) {
            elementMap = new HashMap<TnRelationKey, Object>();
            _rowMap.put(relationNoSuffix, elementMap);
        }
        elementMap.put(relKey, relationRow);
    }

    /**
     * Create the key of relation.
     * @param rs The result set. (NotNull)
     * @param rpt The property type of relation. (NotNull)
     * @param selectColumnMap The name map of select column. {flexible-name = column-DB-name} (NotNull)
     * @param selectIndexMap The map of select index. (NullAllowed: If it's null, it doesn't use select index.)
     * @param relationNoSuffix The suffix of relation No. (NotNull)
     * @return The key of relation. (NotNull)
     * @throws SQLException
     */
    public TnRelationKey createRelationKey(ResultSet rs, TnRelationPropertyType rpt,
            Map<String, String> selectColumnMap, Map<String, Integer> selectIndexMap, String relationNoSuffix)
            throws SQLException {
        final DBMeta dbmeta = rpt.getYourBeanMetaData().getDBMeta();
        final Map<String, Object> relKeyValues;
        if (dbmeta != null && dbmeta.hasPrimaryKey()) {
            final List<ColumnInfo> uniqueColumnList = dbmeta.getPrimaryUniqueInfo().getUniqueColumnList();
            relKeyValues = new LinkedHashMap<String, Object>(uniqueColumnList.size());
            for (ColumnInfo columnInfo : uniqueColumnList) {
                final String keyColumn = columnInfo.getColumnDbName();
                setupKeyElement(rs, rpt, selectColumnMap, selectIndexMap, relationNoSuffix, relKeyValues, keyColumn);
            }
        } else {
            final int keySize = rpt.getKeySize();
            relKeyValues = new LinkedHashMap<String, Object>(keySize);
            for (int i = 0; i < keySize; ++i) {
                final String keyColumn = rpt.getYourKey(i);
                setupKeyElement(rs, rpt, selectColumnMap, selectIndexMap, relationNoSuffix, relKeyValues, keyColumn);
            }
        }
        return !relKeyValues.isEmpty() ? new TnRelationKey(relKeyValues) : null;
    }

    protected void setupKeyElement(ResultSet rs, TnRelationPropertyType rpt, Map<String, String> selectColumnMap,
            Map<String, Integer> selectIndexMap, String relationNoSuffix, final Map<String, Object> relKeyValues,
            String keyColumn) throws SQLException {
        final TnPropertyType pt = rpt.getYourBeanMetaData().getPropertyTypeByColumnName(keyColumn);
        final String columnLabel = pt.getColumnDbName() + relationNoSuffix;
        final ValueType valueType;
        if (selectColumnMap.containsKey(columnLabel)) {
            valueType = pt.getValueType();
        } else {
            // basically unreachable
            // because the referred column (basically PK or FK) must exist
            // if the relation's select clause is specified
            return;
        }
        final Object value;
        if (selectIndexMap != null) {
            value = ResourceContext.getValue(rs, columnLabel, valueType, selectIndexMap);
        } else {
            value = valueType.getValue(rs, columnLabel);
        }
        if (value == null) {
            // reachable when the referred column data is null
            // (treated as no relation data)
            return;
        }
        relKeyValues.put(columnLabel, value);
    }

    /**
     * Build the string of relation No suffix.
     * @param rpt The property type of relation. (NotNull)
     * @return The string of relation No suffix. (NotNull)
     */
    protected String buildRelationNoSuffix(TnRelationPropertyType rpt) {
        return "_" + rpt.getRelationNo();
    }
}
