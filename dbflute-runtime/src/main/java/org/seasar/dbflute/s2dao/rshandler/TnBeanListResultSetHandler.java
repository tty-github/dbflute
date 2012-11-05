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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ConditionBeanContext;
import org.seasar.dbflute.outsidesql.OutsideSqlContext;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.extension.TnRelationRowCreatorExtension;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyMapping;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyType;
import org.seasar.dbflute.s2dao.rowcreator.TnRelationRowCreator;
import org.seasar.dbflute.s2dao.rowcreator.TnRowCreator;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnBeanListResultSetHandler extends TnAbstractBeanResultSetHandler {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param beanMetaData Bean meta data. (NotNull)
     * @param rowCreator Row creator. (NotNull)
     * @param relationRowCreator Relation row creator. (NotNul)
     */
    public TnBeanListResultSetHandler(TnBeanMetaData beanMetaData, TnRowCreator rowCreator,
            TnRelationRowCreator relationRowCreator) {
        super(beanMetaData, rowCreator, relationRowCreator);
    }

    // ===================================================================================
    //                                                                              Handle
    //                                                                              ======
    public Object handle(ResultSet rs) throws SQLException {
        final List<Object> list = new ArrayList<Object>();
        mappingBean(rs, new BeanRowHandler() {
            public void handle(Object row) throws SQLException {
                list.add(row);
            }
        });
        return list;
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    protected static interface BeanRowHandler {
        void handle(Object row) throws SQLException;
    }

    protected void mappingBean(ResultSet rs, BeanRowHandler handler) throws SQLException {
        // lazy initialization because if the result is zero, the resources are unused
        Map<String, String> selectColumnMap = null;
        Map<String, TnPropertyMapping> propertyCache = null;
        Map<String, Map<String, TnPropertyMapping>> relPropCache = null; // key is relationNoSuffix, columnName
        TnRelationRowCache relRowCache = null;

        final TnBeanMetaData basePointBmd = getBeanMetaData();
        final int relSize = basePointBmd.getRelationPropertyTypeSize();
        final boolean hasCB = hasConditionBean();
        final boolean skipRelationLoop;
        {
            final boolean emptyRelation = isSelectedRelationEmpty();
            final boolean hasOSC = hasOutsideSqlContext();
            final boolean specifiedOutsideSql = isSpecifiedOutsideSql();

            // if it has condition-bean that has no relation to get
            // or it has outside SQL context that is specified-outside-sql,
            // they are unnecessary to do relation loop
            skipRelationLoop = (hasCB && emptyRelation) || (hasOSC && specifiedOutsideSql);
        }
        final boolean canRowCache = hasCB && canRelationMappingCache();
        final Map<String, Integer> selectIndexMap = ResourceContext.getSelectIndexMap();

        while (rs.next()) {
            if (selectColumnMap == null) {
                selectColumnMap = createSelectColumnMap(rs);
            }
            if (propertyCache == null) {
                propertyCache = createPropertyCache(selectColumnMap);
            }

            // create row instance of base table by row property cache
            final Object row = createRow(rs, selectIndexMap, propertyCache);

            if (skipRelationLoop) {
                adjustCreatedRow(row, basePointBmd);
                handler.handle(row);
                continue;
            }

            if (relPropCache == null) {
                relPropCache = createRelationPropertyCache(selectColumnMap, selectIndexMap);
            }
            if (relRowCache == null) {
                relRowCache = createRelationRowCache(relSize, canRowCache);
            }
            for (int i = 0; i < relSize; ++i) {
                final TnRelationPropertyType rpt = basePointBmd.getRelationPropertyType(i);
                if (rpt == null) {
                    continue;
                }
                // do only selected foreign property for performance if condition-bean exists
                if (hasCB && !hasSelectedRelation(rpt.getRelationNoSuffixPart())) {
                    continue;
                }
                mappingFirstRelation(rs, row, rpt, selectColumnMap, selectIndexMap, relPropCache, relRowCache);
            }
            adjustCreatedRow(row, basePointBmd);
            handler.handle(row);
        }
    }

    /**
     * Create the cache of relation row.
     * @param canRowCache Can the relation row cache?
     * @param relSize The size of relation.
     * @return The cache of relation row. (NotNull)
     */
    protected TnRelationRowCache createRelationRowCache(int relSize, boolean canRowCache) {
        return new TnRelationRowCache(relSize, canRowCache);
    }

    /**
     * Do mapping first relation row. <br />
     * This logic is similar to next relation mapping in {@link TnRelationRowCreatorExtension}. <br />
     * So you should check it when this logic has modification.
     * @param rs The result set of JDBC, connecting to database here. (NotNull)
     * @param row The base point row. (NotNull)
     * @param rpt The property type of the relation. (NotNull)
     * @param selectColumnMap The map of select column. (NotNull)
     * @param selectIndexMap The map of select index. (NullAllowed)
     * @param relPropCache The map of relation property cache. (NotNull) 
     * @param relRowCache The cache of relation row. (NotNull)
     * @throws SQLException
     */
    protected void mappingFirstRelation(ResultSet rs, Object row, TnRelationPropertyType rpt,
            Map<String, String> selectColumnMap, Map<String, Integer> selectIndexMap,
            Map<String, Map<String, TnPropertyMapping>> relPropCache, TnRelationRowCache relRowCache)
            throws SQLException {
        final String relationNoSuffix = getFirstLevelRelationPath(rpt);
        final TnRelationKey relKey = relRowCache.createRelationKey(rs, rpt // basic resource
                , selectColumnMap, selectIndexMap // select resource
                , relationNoSuffix); // indicates relation location
        if (relKey == null) {
            return; // treated as no data if the relation key has no data
        }
        Object relationRow = relRowCache.getRelationRow(relationNoSuffix, relKey);
        if (relationRow == null) { // when no cache
            relationRow = createRelationRow(rs, rpt // basic resource
                    , selectColumnMap, selectIndexMap // select resource
                    , relKey.getRelKeyValues(), relPropCache, relRowCache); // relation resource
            if (relationRow != null) { // is new created relation row
                adjustCreatedRow(relationRow, rpt.getYourBeanMetaData());
                relRowCache.addRelationRow(relationNoSuffix, relKey, relationRow);
            }
        }
        if (relationRow != null) {
            rpt.getPropertyDesc().setValue(row, relationRow);
        }
    }

    protected String getFirstLevelRelationPath(TnRelationPropertyType rpt) {
        // here is on base so this suffix becomes relation path directly
        return rpt.getRelationNoSuffixPart();
    }

    // ===================================================================================
    //                                                                       ConditionBean
    //                                                                       =============
    protected boolean hasConditionBean() {
        return ConditionBeanContext.isExistConditionBeanOnThread();
    }

    protected boolean isSelectedRelationEmpty() {
        if (!hasConditionBean()) {
            return true;
        }
        final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
        return cb.getSqlClause().isSelectedRelationEmpty();
    }

    /**
     * Does it have the relation as selected?
     * You should call hasConditionBean() before calling this!
     * @param relationNoSuffix The suffix of relation NO. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean hasSelectedRelation(String relationNoSuffix) {
        final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
        return cb.getSqlClause().hasSelectedRelation(relationNoSuffix);
    }

    /**
     * Can the relation mapping (entity instance) cache?
     * @return The determination, true or false.
     */
    protected boolean canRelationMappingCache() {
        final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
        return cb.canRelationMappingCache();
    }

    // ===================================================================================
    //                                                                          OutsideSql
    //                                                                          ==========
    protected boolean hasOutsideSqlContext() {
        return OutsideSqlContext.isExistOutsideSqlContextOnThread();
    }

    protected boolean isSpecifiedOutsideSql() {
        if (!hasOutsideSqlContext()) {
            return false;
        }
        final OutsideSqlContext context = OutsideSqlContext.getOutsideSqlContextOnThread();
        return context.isSpecifiedOutsideSql();
    }
}
