package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class SelectSequencesStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;

    public SelectSequencesStatement(String catalogName, String schemaName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }
}
