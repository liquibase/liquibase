package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;

public class CreateDatabaseChangeLogTableGeneratorFirebird extends CreateDatabaseChangeLogTableGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return database instanceof FirebirdDatabase;
    }

    @Override
    protected String getFilenameColumnSize() {
        return "150";
    }

    @Override
    protected String getIdColumnSize() {
        return "60";
    }

    @Override
    protected String getAuthorColumnSize() {
        return "60";
    }
}
