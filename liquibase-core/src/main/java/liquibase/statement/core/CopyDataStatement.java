package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import java.util.List;

/**
 * Copies data from one table to another.
 */
public class CopyDataStatement extends AbstractStatement {

    public static final String SOURCE_TABLE_CATALOG_NAME = "sourceTableCatalogName";
    public static final String SOURCE_TABLE_SCHEMA_NAME = "sourceTableSchemaName";
	public static final String SOURCE_TABLE_NAME = "sourceTableName";

    public static final String TARGET_TABLE_CATALOG_NAME = "targetTableCatalogName";
    public static final String TARGET_TABLE_SCHEMA_NAME = "targetTableSchemaName";
    public static final String TARGET_TABLE_NAME = "targetTableName";

	public static final String SOURCE_COLUMNS = "sourceColumns";
	
	
	public CopyDataStatement() {
	}

    public String getSourceTableCatalogName() {
        return getAttribute(SOURCE_TABLE_CATALOG_NAME, String.class);
    }

    public CopyDataStatement setSourceTableCatalogName(String tableCatalogName) {
        return (CopyDataStatement) setAttribute(SOURCE_TABLE_CATALOG_NAME, tableCatalogName);
    }

    public String getSourceTableSchemaName() {
        return getAttribute(SOURCE_TABLE_SCHEMA_NAME, String.class);
    }

    public CopyDataStatement setSourceTableSchemaName(String tableSchemaName) {
        return (CopyDataStatement) setAttribute(SOURCE_TABLE_SCHEMA_NAME, tableSchemaName);
    }

    public String getSourceTableName() {
		return getAttribute(SOURCE_TABLE_NAME, String.class);
	}

    public CopyDataStatement setSourceTableName(String tableName) {
        return (CopyDataStatement) setAttribute(SOURCE_TABLE_NAME, tableName);
    }

    public String getTargetTableCatalogName() {
        return getAttribute(TARGET_TABLE_CATALOG_NAME, String.class);
    }

    public CopyDataStatement setTargetTableCatalogName(String tableCatalogName) {
        return (CopyDataStatement) setAttribute(TARGET_TABLE_CATALOG_NAME, tableCatalogName);
    }

    public String getTargetTableSchemaName() {
        return getAttribute(TARGET_TABLE_SCHEMA_NAME, String.class);
    }

    public CopyDataStatement setTargetTableSchemaName(String tableSchemaName) {
        return (CopyDataStatement) setAttribute(TARGET_TABLE_SCHEMA_NAME, tableSchemaName);
    }

    public String getTargetTableName() {
        return getAttribute(TARGET_TABLE_NAME, String.class);
    }

    public CopyDataStatement setTargetTableName(String tableName) {
        return (CopyDataStatement) setAttribute(TARGET_TABLE_NAME, tableName);
    }

    public List<ColumnConfig> getSourceColumns() {
		return (List<ColumnConfig>) getAttribute(SOURCE_COLUMNS, List.class);
	}

    public CopyDataStatement setSourceColumns(List<ColumnConfig> columns) {
        return (CopyDataStatement) setAttribute(SOURCE_COLUMNS, columns);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
                new Table().setName(getTargetTableName()).setSchema(getTargetTableCatalogName(), getTargetTableSchemaName())
        };
    }
}
