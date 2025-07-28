package liquibase.statement.core.snowflake;

import liquibase.statement.AbstractSqlStatement;

public class DropSchemaStatement extends AbstractSqlStatement {
    
    private String databaseName;
    private String schemaName;
    private Boolean ifExists;
    private Boolean cascade;

    public DropSchemaStatement(String databaseName, String schemaName, Boolean ifExists, Boolean cascade) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.ifExists = ifExists;
        this.cascade = cascade;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public Boolean getIfExists() {
        return ifExists;
    }

    public Boolean getCascade() {
        return cascade;
    }
}