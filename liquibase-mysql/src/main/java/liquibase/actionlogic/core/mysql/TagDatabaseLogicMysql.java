package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.core.TagDatabaseAction;
import liquibase.actionlogic.core.TagDatabaseLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.structure.ObjectReference;
import liquibase.util.StringClauses;

public class TagDatabaseLogicMysql extends TagDatabaseLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateWhereClause(TagDatabaseAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses().append("DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM (SELECT DATEEXECUTED FROM " + database.escapeObjectName(new ObjectReference(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())) + ") AS X)");
    }
}
