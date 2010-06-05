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
package org.seasar.dbflute.cbean.sqlclause;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.util.Srl;

/**
 * The simple filter for where clause.
 * @author jflute
 */
public interface WhereClauseSimpleFilter {

    public static final String BIND_COMMENT_BEGIN_PART = "/*pmb";
    public static final String BIND_COMMENT_END_PART = "*/null";

    public static final String EMBEDDED_COMMENT_BEGIN_PART = "/*$pmb";
    public static final String EMBEDDED_COMMENT_END_PART = "*/null";

    public static final String EMBEDDED_COMMENT_QUOTED_END_PART = "*/'dummy'";

    /**
     * Filter clause element.
     * @param clauseElement Clause element of where. (NotNull and NotEmpty)
     * @return Filtered where clause. (NotNull and NotEmpty)
     */
    String filterClauseElement(String clauseElement);

    /**
     * The simple filter for where clause to embedded. <br />
     * Attention: Searching column is not perfect. This class determines by column name only!
     * So when there are same-name column between tables, both are target!
     * @author jflute
     */
    public static class WhereClauseToEmbeddedSimpleFilter implements WhereClauseSimpleFilter, Serializable {

        /** Serial version UID. (Default) */
        private static final long serialVersionUID = 1L;

        protected Set<ColumnInfo> _filterTargetColumnInfoSet;

        public WhereClauseToEmbeddedSimpleFilter(ColumnInfo filterTargetColumnInfo) {
            this._filterTargetColumnInfoSet = new HashSet<ColumnInfo>();
            this._filterTargetColumnInfoSet.add(filterTargetColumnInfo);
        }

        public WhereClauseToEmbeddedSimpleFilter(Set<ColumnInfo> filterTargetColumnInfoSet) {
            this._filterTargetColumnInfoSet = filterTargetColumnInfoSet;
        }

        /**
         * Filter clause element.
         * @param clauseElement Clause element of where. (NotNull and NotEmpty)
         * @return Filtered where clause. (NotNull and NotEmpty)
         */
        public String filterClauseElement(String clauseElement) {
            if (_filterTargetColumnInfoSet == null || _filterTargetColumnInfoSet.isEmpty()) {
                return toEmbedded(clauseElement);
            }
            for (ColumnInfo columnInfo : _filterTargetColumnInfoSet) {
                if (isTargetClause(clauseElement, columnInfo.getColumnDbName())) {
                    return toEmbedded(clauseElement);
                }
            }
            return clauseElement;
        }

        protected boolean isTargetClause(String clauseElement, final String columnDbName) {
            return clauseElement.indexOf("." + columnDbName + " ") >= 0;
        }

        protected String toEmbedded(String clauseElement) {
            clauseElement = replace(clauseElement, BIND_COMMENT_BEGIN_PART, EMBEDDED_COMMENT_BEGIN_PART);
            clauseElement = replace(clauseElement, BIND_COMMENT_END_PART, EMBEDDED_COMMENT_END_PART);
            return clauseElement;
        }

        protected final String replace(String str, String fromStr, String toStr) {
            return Srl.replace(str, fromStr, toStr);
        }
    }

    /**
     * The simple filter for where clause to embedded and quoted. <br />
     * Attention: Searching column is not perfect. This class determines by column name only!
     * So when there are same-name column between tables, both are target!
     * @author jflute
     */
    public static class WhereClauseToEmbeddedQuotedSimpleFilter extends WhereClauseToEmbeddedSimpleFilter {

        /** Serial version UID. (Default) */
        private static final long serialVersionUID = 1L;

        public WhereClauseToEmbeddedQuotedSimpleFilter(ColumnInfo filterTargetColumnInfo) {
            super(filterTargetColumnInfo);
        }

        public WhereClauseToEmbeddedQuotedSimpleFilter(Set<ColumnInfo> filterTargetColumnInfoSet) {
            super(filterTargetColumnInfoSet);
        }

        @Override
        protected String toEmbedded(String clauseElement) {
            clauseElement = replace(clauseElement, BIND_COMMENT_BEGIN_PART, EMBEDDED_COMMENT_BEGIN_PART);
            clauseElement = replace(clauseElement, BIND_COMMENT_END_PART, EMBEDDED_COMMENT_QUOTED_END_PART);
            return clauseElement;
        }
    }
}
