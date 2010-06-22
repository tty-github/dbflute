/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.bhv;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.SpecifyQuery;
import org.seasar.dbflute.cbean.chelper.HpCalcSpecification;
import org.seasar.dbflute.cbean.chelper.HpCalculator;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.exception.VaryingUpdateCommonColumnSpecificationException;
import org.seasar.dbflute.exception.VaryingUpdateInvalidColumnSpecificationException;
import org.seasar.dbflute.exception.VaryingUpdateNotFoundCalculationException;
import org.seasar.dbflute.exception.VaryingUpdateOptimisticLockSpecificationException;
import org.seasar.dbflute.exception.VaryingUpdatePrimaryKeySpecificationException;
import org.seasar.dbflute.exception.VaryingUpdateUnsupportedColumnTypeException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * The option of update for varying-update.
 * @author jflute
 * @since 0.9.7.2 (2010/06/18 Friday)
 * @param <CB> The type of condition-bean for specification.
 */
public class UpdateOption<CB extends ConditionBean> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<HpCalcSpecification<CB>> _selfSpecificationList = DfCollectionUtil.newArrayList();
    protected final Map<String, HpCalcSpecification<CB>> _selfSpecificationMap = StringKeyMap.createAsFlexibleOrdered();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * <pre>
     * Purchase purchase = new Purchase();
     * purchase.setPK...(value); <span style="color: #3F7E5E">// required</span>
     * purchase.setOther...(value); <span style="color: #3F7E5E">// you should set only modified columns</span>
     * UpdateOption&lt;PurchaseCB&gt; option = <span style="color: #FD4747">new UpdateOption&lt;PurchaseCB&gt;()</span>;
     * option.<span style="color: #FD4747">self</span>(new SpecifyQuery&lt;PurchaseCB&gt;() {
     *     public void specify(PurchaseCB cb) {
     *         cb.specify().<span style="color: #FD4747">columnPurchaseCount()</span>;
     *     }
     * }).<span style="color: #FD4747">plus</span>(1); <span style="color: #3F7E5E">// PURCHASE_COUNT = PURCHASE_COUNT + 1</span>
     * purchaseBhv.<span style="color: #FD4747">varyingUpdateNonstrict</span>(purchase, option);
     * </pre>
     */
    public UpdateOption() {
    }

    // ===================================================================================
    //                                                                    Self Calculation
    //                                                                    ================
    /**
     * Specify a self calculation as update value. <br />
     * You can specify a column except PK column, common column and optimistic-lock column.
     * And you can specify only one column that is a number type.
     * <pre>
     * Purchase purchase = new Purchase();
     * purchase.setPK...(value); <span style="color: #3F7E5E">// required</span>
     * purchase.setOther...(value); <span style="color: #3F7E5E">// you should set only modified columns</span>
     * UpdateOption&lt;PurchaseCB&gt; option = new UpdateOption&lt;PurchaseCB&gt;();
     * option.<span style="color: #FD4747">self</span>(new SpecifyQuery&lt;PurchaseCB&gt;() {
     *     public void specify(PurchaseCB cb) {
     *         cb.specify().<span style="color: #FD4747">columnPurchaseCount()</span>;
     *     }
     * }).<span style="color: #FD4747">plus</span>(1); <span style="color: #3F7E5E">// PURCHASE_COUNT = PURCHASE_COUNT + 1</span>
     * purchaseBhv.<span style="color: #FD4747">varyingUpdateNonstrict</span>(purchase, option);
     * </pre>
     * @param specifyQuery The query for specification that specifies only one column. (NotNull)
     * @return The calculation of specification for the specified column. (NotNull)
     */
    public HpCalculator self(SpecifyQuery<CB> specifyQuery) {
        if (specifyQuery == null) {
            String msg = "The argument 'specifyQuery' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        final HpCalcSpecification<CB> specification = new HpCalcSpecification<CB>(specifyQuery);
        _selfSpecificationList.add(specification);
        return specification;
    }

    // ===================================================================================
    //                                                               Resolve Specification
    //                                                               =====================
    public void resolveSpecification(CB cb) {
        for (HpCalcSpecification<CB> specification : _selfSpecificationList) {
            final SpecifyQuery<CB> specifyQuery = specification.getSpecifyQuery();
            specifyQuery.specify(cb);
            final String columnDbName = getSpecifiedColumnDbNameAsOne(cb);
            assertSpecifiedColumn(cb, columnDbName);
            _selfSpecificationMap.put(columnDbName, specification);
        }
    }

    protected void assertSpecifiedColumn(CB cb, String columnDbName) {
        if (columnDbName == null) {
            throwVaryingUpdateInvalidColumnSpecificationException(cb);
        }
        final ColumnInfo columnInfo = cb.getDBMeta().findColumnInfo(columnDbName);
        if (columnInfo.isPrimary()) {
            throwVaryingUpdatePrimaryKeySpecificationException(columnInfo);
        }
        if (columnInfo.isCommonColumn()) {
            throwVaryingUpdateCommonColumnSpecificationException(columnInfo);
        }
        if (columnInfo.isOptimisticLock()) {
            throwVaryingUpdateOptimisticLockSpecificationException(columnInfo);
        }
        if (!Number.class.isAssignableFrom(columnInfo.getPropertyType())) {
            // *simple message because other types may be supported at the future
            String msg = "Not number column specified: " + columnInfo;
            throw new VaryingUpdateUnsupportedColumnTypeException(msg);
        }
    }

    protected void throwVaryingUpdateInvalidColumnSpecificationException(CB cb) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The specified column for varying-update was invalid.");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("For example:");
        br.addElement("");
        br.addElement("  (x):");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            // *no, empty");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (x):");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            cb.specify().columnPurchaseCount();");
        br.addElement("            cb.specify().columnPurchasePrice(); // *no, duplicated");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (o)");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            cb.specify().columnPurchaseCount(); // OK");
        br.addElement("        }");
        br.addElement("    });");
        br.addItem("Target Table");
        br.addElement(cb.getTableDbName());
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdateInvalidColumnSpecificationException(msg);
    }

    protected void throwVaryingUpdatePrimaryKeySpecificationException(ColumnInfo columnInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The primary key column was specified.");
        br.addItem("Advice");
        br.addElement("Varying-update is not allowed to specify a PK column.");
        br.addItem("Target Table");
        br.addElement(columnInfo.getDBMeta().getTableDbName());
        br.addItem("Specified Column");
        br.addElement(columnInfo);
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdatePrimaryKeySpecificationException(msg);
    }

    protected void throwVaryingUpdateCommonColumnSpecificationException(ColumnInfo columnInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column for optimistic lock was specified.");
        br.addItem("Advice");
        br.addElement("Varying-update is not allowed to specify a optimistic-lock column.");
        br.addItem("Target Table");
        br.addElement(columnInfo.getDBMeta().getTableDbName());
        br.addItem("Specified Column");
        br.addElement(columnInfo);
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdateCommonColumnSpecificationException(msg);
    }

    protected void throwVaryingUpdateOptimisticLockSpecificationException(ColumnInfo columnInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column for optimistic lock was specified.");
        br.addItem("Advice");
        br.addElement("Varying-update is not allowed to specify a optimistic-lock column.");
        br.addItem("Target Table");
        br.addElement(columnInfo.getDBMeta().getTableDbName());
        br.addItem("Specified Column");
        br.addElement(columnInfo);
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdateOptimisticLockSpecificationException(msg);
    }

    protected String getSpecifiedColumnDbNameAsOne(CB cb) {
        return cb.getSqlClause().getSpecifiedColumnDbNameAsOne(); // it's column DB name
    }

    // ===================================================================================
    //                                                                     Build Statement
    //                                                                     ===============
    public boolean hasStatement(String columnDbName) {
        return findSpecification(columnDbName) != null;
    }

    public String buildStatement(String columnDbName, ColumnSqlName columnSqlName) {
        final HpCalcSpecification<CB> statement = findSpecification(columnDbName);
        if (statement == null) {
            return null;
        }
        final String exp = statement.buildStatement(columnSqlName);
        if (exp == null) { // means non-calculation
            throwVaryingUpdateNotFoundCalculationException(columnDbName);
        }
        return exp;
    }

    protected HpCalcSpecification<CB> findSpecification(String columnDbName) {
        // only "self" supported
        return _selfSpecificationMap.get(columnDbName);
    }

    protected void throwVaryingUpdateNotFoundCalculationException(String columnDbName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("A calculation of specified column for varying-update was not found.");
        br.addItem("Advice");
        br.addElement("You should call plus()/minus()/... methods after specification.");
        br.addElement("For example:");
        br.addElement("");
        br.addElement("  (x):");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            cb.specify().columnPurchaseCount();");
        br.addElement("        }");
        br.addElement("    }); // *no!");
        br.addElement("  (o):");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            cb.specify().columnPurchaseCount();");
        br.addElement("        }");
        br.addElement("    }).plus(1); // OK");
        br.addItem("Specified Column");
        br.addElement(columnDbName);
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdateNotFoundCalculationException(msg);
    }
}