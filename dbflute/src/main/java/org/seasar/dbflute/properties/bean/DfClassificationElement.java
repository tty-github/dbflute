package org.seasar.dbflute.properties.bean;

/**
 * @author jflute
 * @since 0.8.2 (2008/10/22 Wednesday)
 */
public class DfClassificationElement {

    protected String _code;
    protected String _name;
    protected String _alias;
    protected String _comment;

    @Override
    public String toString() {
        return "{" + _code + ", " + _name + ", " + _alias + ", " + _comment + "}";
    }

    public String getCode() {
        return _code;
    }

    public void setCode(String code) {
        this._code = code;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getAlias() {
        return _alias;
    }

    public void setAlias(String alias) {
        this._alias = alias;
    }

    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        this._comment = comment;
    }
}
