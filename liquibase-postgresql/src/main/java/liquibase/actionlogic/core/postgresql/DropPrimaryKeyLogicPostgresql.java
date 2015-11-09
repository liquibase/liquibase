package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropPrimaryKeyAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
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
    public ValidationErrors validate(DropPrimaryKeyAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("constraintName", action);
    }

    @Override
    public ActionResult execute(DropPrimaryKeyAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();

        String constraintName = action.constraintName;
        if (constraintName == null) {
            return new DelegateResult(
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
                                    + action.tableName.container.name
                                    + "', '"
                                    + action.tableName.name
                                    + "' drop function __liquibase_drop_pk(schemaName text, tableName text")
            );
        } else {
            return super.execute(action, scope);
        }
    }
}
