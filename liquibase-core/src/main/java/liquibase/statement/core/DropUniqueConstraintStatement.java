package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.core.Column;

public class DropUniqueConstraintStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintName;
    /**
     * Sybase ASA does drop unique constraint not by name, but using list of the columns in unique clause.
     */
    private ColumnConfig[] uniqueColumns;

    public DropUniqueConstraintStatement(String catalogName, String schemaName, String tableName, String constraintName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
    }

    public DropUniqueConstraintStatement(String catalogName, String schemaName, String tableName, String constraintName, ColumnConfig[] uniqueColumns) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.uniqueColumns = uniqueColumns;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

	public ColumnConfig[] getUniqueColumns() {
		return uniqueColumns;
	}

	public void setUniqueColumns(ColumnConfig[] uniqueColumns) {
		this.uniqueColumns = uniqueColumns;
	}

}
