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
package org.seasar.dbflute.s2dao.metadata;

import org.seasar.dbflute.jdbc.ValueType;

/**
 * {Refers to Seasar and Extends its class}
 * @author jflute
 */
public class TnProcedureParameterType {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final TnProcedureParameterAccessor _parameterAccessor;
    private final String _parameterName;
    private final Class<?> _parameterType;
    private final Class<?> _elementType;
    private Integer _parameterIndex;
    private ValueType _valueType;
    private boolean _inType;
    private boolean _outType;
    private boolean _returnType;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnProcedureParameterType(TnProcedureParameterAccessor parameterAccessor, String parameterName,
            Class<?> parameterType, Class<?> elementType) {
        this._parameterAccessor = parameterAccessor;
        this._parameterName = parameterName;
        this._parameterType = parameterType;
        this._elementType = elementType;
    }

    public static interface TnProcedureParameterAccessor {
        Object getValue(Object target);

        void setValue(Object target, Object value);
    }

    // ===================================================================================
    //                                                                         Field Value
    //                                                                         ===========
    public Object getValue(Object target) {
        return _parameterAccessor.getValue(target);
    }

    public void setValue(Object target, Object value) {
        _parameterAccessor.setValue(target, value);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getParameterName() {
        return _parameterName;
    }

    public Class<?> getParameterType() {
        return _parameterType;
    }

    public Class<?> getElementType() {
        return _elementType;
    }

    public Integer getParameterIndex() {
        return _parameterIndex;
    }

    public void setParameterIndex(Integer parameterIndex) {
        this._parameterIndex = parameterIndex;
    }

    public ValueType getValueType() {
        return _valueType;
    }

    public void setValueType(final ValueType valueType) {
        this._valueType = valueType;
    }

    public boolean isInType() {
        return _inType;
    }

    public void setInType(final boolean inType) {
        this._inType = inType;
    }

    public boolean isOutType() {
        return _outType;
    }

    public void setOutType(final boolean outType) {
        this._outType = outType;
    }

    public boolean isReturnType() {
        return _returnType;
    }

    public void setReturnType(final boolean returnType) {
        this._returnType = returnType;
    }
}