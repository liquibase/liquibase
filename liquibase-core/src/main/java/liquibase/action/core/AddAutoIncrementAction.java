package liquibase.action.core;

import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.ActionStatus;
import liquibase.snapshot.SnapshotFactory;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;

import java.math.BigInteger;

public class AddAutoIncrementAction extends AbstractAction {

    public ObjectName columnName;
    public String columnDataType;
    public BigInteger startWith;
    public BigInteger incrementBy;

    @Override
    public ActionStatus checkStatus(Scope scope) {
        ActionStatus result = new ActionStatus();
        Column example = new Column(columnName);
        try {
            Column column = scope.getSingleton(SnapshotFactory.class).get(example, scope);
            if (column == null) return result.unknown("Column '"+columnName+"' does not exist");


            result.assertApplied(column.isAutoIncrement(), "Column '"+columnName+"' is not auto-increment");

            if (column.autoIncrementInformation != null) {
                result.assertCorrect(this, column.autoIncrementInformation, "startWith", true);
                result.assertCorrect(this, column.autoIncrementInformation, "incrementBy", true);
            }

            return result;
        } catch (Exception e) {
            return result.unknown(e);

        }
    }

}
