package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropPrimaryKeyAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.actionlogic.core.DropPrimaryKeyLogic;

public class DropPrimaryKeyLogicPostgresql extends DropPrimaryKeyLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(DropPrimaryKeyAction.Attr.constraintName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        String escapedTableName = database.escapeTableName(
                action.get(DropPrimaryKeyAction.Attr.catalogName, String.class),
                action.get(DropPrimaryKeyAction.Attr.schemaName, String.class),
                action.get(DropPrimaryKeyAction.Attr.tableName, String.class)
        );

        String constraintName = action.get(DropPrimaryKeyAction.Attr.constraintName, String.class);
        if (constraintName == null) {
            return new RewriteResult(
                    new ExecuteSqlAction(
                            "create or replace function __liquibase_drop_pk(schemaName text, tableName text) returns void as $$"
                                    + " declare"
                                    + " pkname text;"
                                    + " sql text;"
                                    + " begin"
                                    + " pkname = c.conname"
                                    + " from pg_class r, pg_constraint c, pg_catalog.pg_namespace n"
                                    + " where r.oid = c.conrelid"
                                    + " and contype = 'p'"
                                    + " and n.oid = r.relnamespace"
                                    + " and nspname ilike schemaName"
                                    + " and relname ilike tableName;"
                                    + " sql = 'alter table ' || schemaName || '.' || tableName || ' drop constraint ' || pkname;"
                                    + " execute sql;"
                                    + " end;"
                                    + " $$ language plpgsql;"),
                    new ExecuteSqlAction(
                            " select __liquibase_drop_pk('"
                                    + action.get(DropPrimaryKeyAction.Attr.schemaName, String.class)
                                    + "', '"
                                    + action.get(DropPrimaryKeyAction.Attr.tableName, String.class)
                                    + "' drop function __liquibase_drop_pk(schemaName text, tableName text")
            );
        } else {
            return super.execute(action, scope);
        }
    }
}
