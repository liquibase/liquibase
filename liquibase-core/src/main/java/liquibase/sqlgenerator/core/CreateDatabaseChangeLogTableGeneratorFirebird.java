package liquibase.sqlgenerator.core;

import liquibase.database.core.FirebirdDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;

public class CreateDatabaseChangeLogTableGeneratorFirebird extends CreateDatabaseChangeLogTableGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof FirebirdDatabase;
    }
}
