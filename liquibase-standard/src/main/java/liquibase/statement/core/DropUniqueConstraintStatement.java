package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;

public class DropUniqueConstraintStatement extends AbstractSqlStatement {

    private final String constraintName;
    /**
     * Sybase ASA does drop unique constraint not by name, but using list of the columns in unique clause.
     */
    private ColumnConfig[] uniqueColumns;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public DropUniqueConstraintStatement(String catalogName, String schemaName, String tableName, String constraintName) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.constraintName = constraintName;
    }

    public DropUniqueConstraintStatement(String catalogName, String schemaName, String tableName, String constraintName, ColumnConfig[] uniqueColumns) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.constraintName = constraintName;
        this.uniqueColumns = uniqueColumns;
    }

    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }

    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }

    public String getTableName() {
        return databaseTableIdentifier.getTableName();
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
