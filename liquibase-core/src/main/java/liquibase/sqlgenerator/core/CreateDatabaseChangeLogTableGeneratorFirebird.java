package liquibase.sqlgenerator.core;

import liquibase.database.core.FirebirdDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;

public class CreateDatabaseChangeLogTableGeneratorFirebird extends CreateDatabaseChangeLogTableGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof FirebirdDatabase;
    }
}
