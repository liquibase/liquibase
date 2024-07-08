package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.statement.core.SetTableRemarksStatement;

public class SetTableRemarksGeneratorSnowflake extends SetTableRemarksGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SetTableRemarksStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
}
