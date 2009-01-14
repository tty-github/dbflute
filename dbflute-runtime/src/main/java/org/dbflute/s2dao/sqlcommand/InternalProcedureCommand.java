package org.dbflute.s2dao.sqlcommand;

import javax.sql.DataSource;

import org.dbflute.bhv.core.SqlExecution;
import org.dbflute.jdbc.StatementFactory;
import org.dbflute.outsidesql.OutsideSqlContext;
import org.dbflute.s2dao.procedure.TnProcedureMetaData;
import org.dbflute.s2dao.sqlhandler.TnProcedureHandler;
import org.dbflute.s2dao.jdbc.TnResultSetHandler;


/**
 * @author DBFlute(AutoGenerator)
 */
public class InternalProcedureCommand implements TnSqlCommand, SqlExecution {

	// ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DataSource dataSource;
    protected TnResultSetHandler resultSetHandler;
    protected StatementFactory statementFactory;
    protected TnProcedureMetaData procedureMetaData;

	// ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public InternalProcedureCommand(DataSource dataSource, TnResultSetHandler resultSetHandler,
            StatementFactory statementFactory, TnProcedureMetaData procedureMetaData) {
        this.dataSource = dataSource;
        this.resultSetHandler = resultSetHandler;
        this.statementFactory = statementFactory;
        this.procedureMetaData = procedureMetaData;
    }

	// ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public Object execute(final Object[] args) {
        final TnProcedureHandler handler = newArgumentDtoProcedureHandler();
        final OutsideSqlContext outsideSqlContext = OutsideSqlContext.getOutsideSqlContextOnThread();
        final Object pmb = outsideSqlContext.getParameterBean();
        // The logging message SQL of procedure is unnecessary.
        // handler.setLoggingMessageSqlArgs(...);
        return handler.execute(new Object[]{pmb});
    }
    protected TnProcedureHandler newArgumentDtoProcedureHandler() {
        return new TnProcedureHandler(dataSource, createSql(procedureMetaData), resultSetHandler,
                statementFactory, procedureMetaData);
    }
    protected String createSql(final TnProcedureMetaData procedureMetaData) {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        int size = procedureMetaData.parameterTypes().size();
        if (procedureMetaData.hasReturnParameterType()) {
            sb.append("? = ");
            size--;
        }
        sb.append("call ").append(procedureMetaData.getProcedureName()).append("(");
        for (int i = 0; i < size; i++) {
            sb.append("?, ");
        }
        if (size > 0) {
            sb.setLength(sb.length() - 2);
        }
        sb.append(")}");
        return sb.toString();
    }
}
