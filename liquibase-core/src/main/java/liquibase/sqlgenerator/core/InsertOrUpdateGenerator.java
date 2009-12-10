package liquibase.sqlgenerator.core;

import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.InsertOrUpdateStatement;

public abstract class InsertOrUpdateGenerator implements SqlGenerator<InsertOrUpdateStatement> {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

}
