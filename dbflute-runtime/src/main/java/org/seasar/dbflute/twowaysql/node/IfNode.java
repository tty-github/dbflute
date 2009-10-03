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
package org.seasar.dbflute.twowaysql.node;

import org.seasar.dbflute.exception.IfCommentNotBooleanResultException;
import org.seasar.dbflute.exception.IfCommentWrongExpressionException;
import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.util.DfOgnlUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.DfSystemUtil;

/**
 * @author jflute
 */
public class IfNode extends ContainerNode {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _expression;
    protected Object _parsedExpression;
    protected ElseNode _elseNode;
    protected String _specifiedSql;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public IfNode(String expression, String specifiedSql) {
        this._expression = expression;
        this._parsedExpression = parseForOgnl(expression);
        this._specifiedSql = specifiedSql;
    }

    protected Object parseForOgnl(String expression) {
        return DfOgnlUtil.parseExpression(expression);
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void accept(CommandContext ctx) {
        doAcceptByOgnl(ctx);
    }

    protected void doAcceptByEvaluator(CommandContext ctx) {
        final IfCommentEvaluator evaluator = createIfCommentEvaluator(ctx, _expression);
        boolean result = false;
        try {
            result = evaluator.evaluate();
        } catch (IfCommentWrongExpressionException e) {
            final String replaced = replace(_expression, "pmb.", "pmb.parameterMap.");
            final IfCommentEvaluator another = createIfCommentEvaluator(ctx, replaced);
            try {
                result = another.evaluate();
            } catch (IfCommentWrongExpressionException ignored) {
                throw e;
            }
        }
        if (result) {
            super.accept(ctx);
            ctx.setEnabled(true);
        } else if (_elseNode != null) {
            _elseNode.accept(ctx);
            ctx.setEnabled(true);
        }
    }

    protected IfCommentEvaluator createIfCommentEvaluator(final CommandContext ctx, String expression) {
        return new IfCommentEvaluator(new ParameterFinder() {
            public Object find(String name) {
                return ctx.getArg(name);
            }
        }, expression, _specifiedSql);
    }

    protected void doAcceptByOgnl(CommandContext ctx) {
        Object result = null;
        try {
            result = DfOgnlUtil.getValue(_parsedExpression, ctx);
        } catch (RuntimeException e) {
            if (!_expression.contains("pmb.")) {
                throwIfCommentWrongExpressionException(_expression, e, _specifiedSql);
            }
            final String replaced = replace(_expression, "pmb.", "pmb.parameterMap.");
            final Object secondParsedExpression = DfOgnlUtil.parseExpression(replaced);
            try {
                result = DfOgnlUtil.getValue(secondParsedExpression, ctx);
            } catch (RuntimeException ignored) {
                throwIfCommentWrongExpressionException(_expression, e, _specifiedSql);
            }
            if (result == null) {
                throwIfCommentWrongExpressionException(_expression, e, _specifiedSql);
            }
            _parsedExpression = secondParsedExpression; // switch
        }
        if (result != null && result instanceof Boolean) {
            if (((Boolean) result).booleanValue()) {
                super.accept(ctx);
                ctx.setEnabled(true);
            } else if (_elseNode != null) {
                _elseNode.accept(ctx);
                ctx.setEnabled(true);
            }
        } else {
            throwIfCommentNotBooleanResultException(_expression, result, _specifiedSql);
        }
    }

    protected void throwIfCommentWrongExpressionException(String expression, RuntimeException cause, String specifiedSql) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The IF comment of your specified SQL was Wrong!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm the existence of your property on your arguments." + ln();
        msg = msg + "And confirm the IF comment of your specified SQL." + ln();
        msg = msg + "  For example, correct IF comment is as below:" + ln();
        msg = msg + "    /- - - - - - - - - - - - - - - - - - - - - - - - - - " + ln();
        msg = msg + "    /*IF pmb.xxxId != null*/XXX_ID = .../*END*/" + ln();
        msg = msg + "    /*IF pmb.isPaging()*/.../*END*/" + ln();
        msg = msg + "    /*IF pmb.xxxId == null && pmb.xxxName != null*/.../*END*/" + ln();
        msg = msg + "    /*IF pmb.xxxId == null || pmb.xxxName != null*/.../*END*/" + ln();
        msg = msg + "    - - - - - - - - - -/" + ln();
        msg = msg + ln();
        msg = msg + "[IF Comment Expression]" + ln() + expression + ln();
        msg = msg + ln();
        msg = msg + "[Cause Message]" + ln();
        msg = msg + cause.getClass() + ":" + ln();
        msg = msg + "  --> " + cause.getMessage() + ln();
        final Throwable nestedCause = cause.getCause();
        if (nestedCause != null) {
            msg = msg + nestedCause.getClass() + ":" + ln();
            msg = msg + "  --> " + nestedCause.getMessage() + ln();
        }
        msg = msg + ln();
        msg = msg + "[Specified SQL]" + ln() + specifiedSql + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IfCommentWrongExpressionException(msg, cause);
    }

    protected void throwIfCommentNotBooleanResultException(String expression, Object result, String specifiedSql) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The boolean expression on IF comment of your specified SQL was Wrong!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm the grammar of your IF comment. Does it really express boolean?" + ln();
        msg = msg + "And confirm the existence of your property on your arguments if you use parameterMap." + ln();
        msg = msg + ln();
        msg = msg + "[IF Comment Expression]" + ln() + expression + ln();
        msg = msg + ln();
        msg = msg + "[IF Comment Result Value]" + ln() + result + ln();
        msg = msg + ln();
        msg = msg + "[Specified SQL]" + ln() + specifiedSql + ln();
        msg = msg + "* * * * * * * * * */";
        throw new IfCommentNotBooleanResultException(msg);
    }

    protected String replace(String text, String fromText, String toText) {
        return DfStringUtil.replace(text, fromText, toText);
    }

    protected String ln() {
        return DfSystemUtil.getLineSeparator();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getExpression() {
        return _expression;
    }

    public ElseNode getElseNode() {
        return _elseNode;
    }

    public void setElseNode(ElseNode elseNode) {
        this._elseNode = elseNode;
    }
}
