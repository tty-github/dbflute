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
package org.seasar.dbflute.cbean.sqlclause.query;

import java.util.List;

/**
 * @author jflute
 */
public class OrScopeQueryClauseGroup {

    protected List<QueryClause> _orClauseList;

    @Override
    public String toString() {
        return "{orClauseList=" + (_orClauseList != null ? _orClauseList.size() : "null") + "}";
    }

    public List<QueryClause> getOrClauseList() {
        return _orClauseList;
    }

    public void setOrClauseList(List<QueryClause> orClauseList) {
        this._orClauseList = orClauseList;
    }
}
