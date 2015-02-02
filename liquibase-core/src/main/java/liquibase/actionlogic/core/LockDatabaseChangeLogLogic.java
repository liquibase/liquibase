package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.LockDatabaseChangeLogAction;
import liquibase.action.core.UpdateDataAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ActionPerformException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.core.Column;
import liquibase.util.NetUtil;

import java.sql.Timestamp;

public class LockDatabaseChangeLogLogic extends AbstractActionLogic {

    protected static final String hostname;
    protected static final String hostaddress;
    protected static final String hostDescription = System.getProperty("liquibase.hostDescription") == null ? "" : "#" + System.getProperty("liquibase.hostDescription");

    static {
        try {
            hostname = NetUtil.getLocalHostName();
            hostaddress = NetUtil.getLocalHostAddress();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }


    @Override
    protected Class<? extends Action> getSupportedAction() {
        return LockDatabaseChangeLogAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        String liquibaseSchema = database.getLiquibaseSchemaName();
        String liquibaseCatalog = database.getLiquibaseCatalogName();

        return new DelegateResult((UpdateDataAction) new UpdateDataAction(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogLockTableName())
                .addNewColumnValue("LOCKED", true)
                .addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()))
                .addNewColumnValue("LOCKEDBY", hostname + hostDescription + " (" + hostaddress + ")")
                .set(UpdateDataAction.Attr.whereClause,
                        database.escapeObjectName("ID", Column.class)
                                + " = 1 AND "
                                + database.escapeObjectName("LOCKED", Column.class)
                                + " = "
                                + DataTypeFactory.getInstance().fromDescription("boolean", database).objectToSql(false, database)));
    }
}