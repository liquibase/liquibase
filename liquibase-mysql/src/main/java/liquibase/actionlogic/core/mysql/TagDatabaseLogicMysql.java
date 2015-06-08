package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.action.core.TagDatabaseAction;
import liquibase.actionlogic.core.TagDatabaseLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;

public class TagDatabaseLogicMysql extends TagDatabaseLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateWhereClause(TagDatabaseAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses("DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM (SELECT DATEEXECUTED FROM " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()) + ") AS X)");
    }
}
