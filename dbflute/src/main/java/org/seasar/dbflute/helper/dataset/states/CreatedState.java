package org.seasar.dbflute.helper.dataset.states;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.helper.dataset.DataColumn;
import org.seasar.dbflute.helper.dataset.DataRow;
import org.seasar.dbflute.helper.dataset.DataTable;

/**
 * Row States. {Refer to S2Container}
 * @author jflute
 * @since 0.8.3 (2008/10/28 Tuesday)
 */
public class CreatedState extends AbstractRowState {

    public String toString() {
        return "CREATED";
    }

    protected SqlContext getSqlContext(DataRow row) {
        DataTable table = row.getTable();
        StringBuffer buf = new StringBuffer(100);
        List<Object> argList = new ArrayList<Object>();
        List<Class<?>> argTypeList = new ArrayList<Class<?>>();
        buf.append("insert into ");
        buf.append(table.getTableName());
        buf.append(" (");
        int writableColumnSize = 0;
        for (int i = 0; i < table.getColumnSize(); ++i) {
            DataColumn column = table.getColumn(i);
            if (column.isWritable()) {
                ++writableColumnSize;
                buf.append(column.getColumnName());
                buf.append(", ");
                argList.add(row.getValue(i));
                argTypeList.add(column.getColumnType().getType());
            }
        }
        buf.setLength(buf.length() - 2);
        buf.append(") values (");
        for (int i = 0; i < writableColumnSize; ++i) {
            buf.append("?, ");
        }
        buf.setLength(buf.length() - 2);
        buf.append(")");
        return new SqlContext(buf.toString(), argList.toArray(), argTypeList.toArray(new Class[argTypeList.size()]));
    }
}