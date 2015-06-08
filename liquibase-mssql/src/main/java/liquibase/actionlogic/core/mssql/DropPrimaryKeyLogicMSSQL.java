package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropPrimaryKeyAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.actionlogic.core.DropPrimaryKeyLogic;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;

public class DropPrimaryKeyLogicMSSQL extends DropPrimaryKeyLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(DropPrimaryKeyAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("constraintName", action);
    }

    @Override
    public ActionResult execute(DropPrimaryKeyAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();

        String constraintName = action.constraintName;
        if (constraintName == null) {
            return new DelegateResult(new ExecuteSqlAction(
                    "DECLARE @pkname nvarchar(255)"
            +"\n"
            +"DECLARE @sql nvarchar(2048)"
            +"\n"
            +"select @pkname=i.name from sysindexes i"
            +" join sysobjects o ON i.id = o.id"
            +" join sysobjects pk ON i.name = pk.name AND pk.parent_obj = i.id AND pk.xtype = 'PK'"
            +" join sysindexkeys ik on i.id = ik.id AND i.indid = ik.indid"
            +" join syscolumns c ON ik.id = c.id AND ik.colid = c.colid"
            +" where o.name = '"+action.tableName.name+"'"
            +"\n"
            +"set @sql='alter table "+database.escapeObjectName(action.tableName, Table.class)+" drop constraint ' + @pkname"
            +"\n"
            +"exec(@sql)"
            +"\n"));
        } else {
            return super.execute(action, scope);
        }
    }
}
