/*
 * Copyright 2004-2013 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.properties.propreader;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.infra.dfprop.DfPropFile;

/**
 * @author jflute
 */
public class DfOutsideMapPropReader {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _skipLineSeparator;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfPropFile _dfpropFile = createDfPropFile();

    // ===================================================================================
    //                                                                                Read
    //                                                                                ====
    public Map<String, Object> readMap(String path) {
        return readMap(path, null);
    }

    public Map<String, Object> readMap(String path, String environmentType) {
        return createDfPropFile().readMap(path, environmentType);
    }

    public Map<String, String> readMapAsStringValue(String path) {
        return readMapAsStringValue(path, null);
    }

    public Map<String, String> readMapAsStringValue(String path, String environmentType) {
        return createDfPropFile().readMapAsStringValue(path, environmentType);
    }

    public Map<String, List<String>> readMapAsStringListValue(String path) {
        return readMapAsStringListValue(path, null);
    }

    public Map<String, List<String>> readMapAsStringListValue(String path, String environmentType) {
        return createDfPropFile().readMapAsStringListValue(path, environmentType);
    }

    public Map<String, Map<String, String>> readMapAsStringMapValue(String path) {
        return readMapAsStringMapValue(path, null);
    }

    public Map<String, Map<String, String>> readMapAsStringMapValue(String path, String environmentType) {
        return createDfPropFile().readMapAsStringMapValue(path, environmentType);
    }

    // ===================================================================================
    //                                                                         DfProp File
    //                                                                         ===========
    protected DfPropFile createDfPropFile() {
        final DfPropFile file = newDfPropFile();
        if (_skipLineSeparator) {
            file.skipLineSeparator();
        }
        return file;
    }

    protected DfPropFile newDfPropFile() {
        return new DfPropFile();
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public DfOutsideMapPropReader skipLineSeparator() {
        _skipLineSeparator = true;
        return this;
    }
}