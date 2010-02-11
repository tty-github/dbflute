package org.seasar.dbflute.properties;

import java.util.Map;
import java.util.Properties;

import org.seasar.dbflute.helper.StringKeyMap;

/**
 * @author jflute
 */
public final class DfMultipleFKPropertyProperties extends DfAbstractHelperProperties {
    // /- - - - - - - - - - - - - - - - - - - 
    // It's closet until it becomes to need!
    // - - - - - - - - - -/

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfMultipleFKPropertyProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                 MultipleFK Property
    //                                                                 ===================
    // map:{
    //     ; [tableName] = map:{
    //         ; [columnName]/[columnName] = map:{
    //             ; columnAliasName = [aliasName]
    //         }
    //     }
    // }
    public static final String KEY_multipleFKPropertyMap = "multipleFKPropertyMap";
    protected Map<String, Map<String, Map<String, String>>> _multipleFKPropertyMap;

    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Map<String, String>>> getMultipleFKPropertyMap() { // It's closet!
        if (_multipleFKPropertyMap == null) {
            final Object obj = mapProp("torque." + KEY_multipleFKPropertyMap, DEFAULT_EMPTY_MAP);
            _multipleFKPropertyMap = (Map<String, Map<String, Map<String, String>>>) obj;
        }
        return _multipleFKPropertyMap;
    }

    public String getMultipleFKPropertyColumnAliasName(String tableName, java.util.List<String> columnNameList) {
        final Map<String, Map<String, String>> foreignKeyMap = asFlexible().get(tableName);
        if (foreignKeyMap == null) {
            return "";
        }
        final String columnKey = createMultipleFKPropertyColumnKey(columnNameList);
        final Map<String, Map<String, String>> foreignKeyFxMap = asFlexible(foreignKeyMap);
        final Map<String, String> foreignPropertyElement = foreignKeyFxMap.get(columnKey);
        if (foreignPropertyElement == null) {
            return "";
        }
        final String columnAliasName = foreignPropertyElement.get("columnAliasName");
        return columnAliasName;
    }

    protected String createMultipleFKPropertyColumnKey(java.util.List<String> columnNameList) {
        final StringBuilder sb = new StringBuilder();
        for (String columnName : columnNameList) {
            sb.append("/").append(columnName);
        }
        sb.delete(0, "/".length());
        return sb.toString();
    }

    protected Map<String, Map<String, Map<String, String>>> asFlexible() {
        return StringKeyMap.createAsCaseInsensitive(getMultipleFKPropertyMap());
    }

    protected Map<String, Map<String, String>> asFlexible(final Map<String, Map<String, String>> foreignKeyMap) {
        return StringKeyMap.createAsCaseInsensitive(foreignKeyMap);
    }
}