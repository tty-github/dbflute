/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.torque;

import java.util.Iterator;
import java.util.List;

import org.apache.torque.engine.database.model.Column;

public class TorqueColumnListToStringUtil {

    public static String getColumnArgsString(List columnList) {
        validateColumnList(columnList);

        String result = "";
        for (Iterator ite = columnList.iterator(); ite.hasNext();) {
            final Column pk = (Column) ite.next();
            final String javaNative = pk.getJavaNative();
            final String uncapitalisedJavaName = pk.getUncapitalisedJavaName();
            if ("".equals(result)) {
                result = javaNative + " " + uncapitalisedJavaName;
            } else {
                result = result + ", " + javaNative + " " + uncapitalisedJavaName;
            }
        }
        return result;
    }

    public static String getColumnArgsSetupString(String beanName, List columnList) {
        validateColumnList(columnList);
        final String beanPrefix = (beanName != null ? beanName + "." : "");

        String result = "";
        for (Iterator ite = columnList.iterator(); ite.hasNext();) {
            Column pk = (Column) ite.next();
            final String javaName = pk.getJavaName();
            final String uncapitalisedJavaName = pk.getUncapitalisedJavaName();
            final String setterString = beanPrefix + "set" + javaName + "(" + uncapitalisedJavaName + ");";
            if ("".equals(result)) {
                result = setterString;
            } else {
                result = result + setterString;
            }
        }
        return result;
    }
    
    public static String getColumnArgsSetupStringCSharp(String beanName, List columnList) {
        validateColumnList(columnList);
        final String beanPrefix = (beanName != null ? beanName + "." : "");

        String result = "";
        for (Iterator ite = columnList.iterator(); ite.hasNext();) {
            Column pk = (Column) ite.next();
            final String javaName = pk.getJavaName();
            final String uncapitalisedJavaName = pk.getUncapitalisedJavaName();
            final String setterString = beanPrefix + javaName + " = " + uncapitalisedJavaName + ";";
            if ("".equals(result)) {
                result = setterString;
            } else {
                result = result + setterString;
            }
        }
        return result;
    }

    public static String getColumnNameCommaString(List columnList) {
        validateColumnList(columnList);

        String result = "";
        for (Iterator ite = columnList.iterator(); ite.hasNext();) {
            final Column col = (Column) ite.next();
            final String name = col.getName();
            if ("".equals(result)) {
                result = name;
            } else {
                result = result + ", " + name;
            }
        }
        return result;
    }

    public static String getColumnJavaNameCommaString(List columnList) {
        validateColumnList(columnList);

        String result = "";
        for (Iterator ite = columnList.iterator(); ite.hasNext();) {
            final Column col = (Column) ite.next();
            final String name = col.getJavaName();
            if ("".equals(result)) {
                result = name;
            } else {
                result = result + ", " + name;
            }
        }
        return result;
    }

    public static String getColumnUncapitalisedJavaNameCommaString(List columnList) {
        validateColumnList(columnList);

        String result = "";
        for (Iterator ite = columnList.iterator(); ite.hasNext();) {
            final Column col = (Column) ite.next();
            final String name = col.getUncapitalisedJavaName();
            if ("".equals(result)) {
                result = name;
            } else {
                result = result + ", " + name;
            }
        }
        return result;
    }

    public static String getColumnGetterCommaString(List columnList) {
        validateColumnList(columnList);

        String result = "";
        for (Iterator ite = columnList.iterator(); ite.hasNext();) {
            final Column col = (Column) ite.next();
            final String javaName = col.getJavaName();
            final String getterString = "get" + javaName + "()";
            if ("".equals(result)) {
                result = getterString;
            } else {
                result = result + ", " + getterString;
            }
        }
        return result;
    }

    public static String getColumnOrderByString(List columnList, String sortString) {
        validateColumnList(columnList);

        String result = "";
        for (Iterator ite = columnList.iterator(); ite.hasNext();) {
            final Column pk = (Column) ite.next();
            final String name = pk.getName();
            if ("".equals(result)) {
                result = name + " " + sortString;
            } else {
                result = result + ", " + name + " " + sortString;
            }
        }
        return result;
    }

    public static String getColumnDispValueString(List columnList, String getterPrefix) {
        validateColumnList(columnList);

        String result = "";
        for (Iterator ite = columnList.iterator(); ite.hasNext();) {
            Column pk = (Column) ite.next();
            final String javaName = pk.getJavaName();
            final String getterString = getterPrefix + javaName + "()";
            if ("".equals(result)) {
                result = getterString;
            } else {
                result = result + " + \"-\" + " + getterString;
            }
        }
        return result;
    }

    private static void validateColumnList(List columnList) {
        // PrimaryKeyの無いテーブル or Viewの場合はEmptyがあるので、
        // ここではnullチェックのみとする。
        if (columnList == null) {
            String msg = "The columnList is null.";
            throw new IllegalStateException(msg);
        }
    }
}