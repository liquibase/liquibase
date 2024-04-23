package liquibase.sqlgenerator;

import liquibase.database.BigqueryDatabase;
import liquibase.database.Database;
import liquibase.sqlgenerator.core.CreateDatabaseChangeLogTableGenerator;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;

public class BigqueryCreateDatabaseChangeLogTableGenerator extends CreateDatabaseChangeLogTableGenerator {

    @Override
    public int getPriority() {
        return BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return database instanceof BigqueryDatabase;
    }

    @Override
    protected String getCharTypeName(Database database) {
        return "string";
    }

}
