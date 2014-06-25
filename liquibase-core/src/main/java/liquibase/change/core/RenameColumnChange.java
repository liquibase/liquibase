package liquibase.change.core;

import liquibase.ExecutionEnvironment;
import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.Statement;
import liquibase.statement.core.RenameColumnStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Renames an existing column.
 */
@DatabaseChange(name="renameColumn", description = "Renames an existing column", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class RenameColumnChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String oldColumnName;
    private String newColumnName;
    private String columnDataType;
    private String remarks;

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table containing that the column to rename")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", exampleValue = "name", description = "Name of the existing column to rename")
    public String getOldColumnName() {
        return oldColumnName;
    }

    public void setOldColumnName(String oldColumnName) {
        this.oldColumnName = oldColumnName;
    }

    @DatabaseChangeProperty(description = "Name to rename the column to", exampleValue = "full_name")
    public String getNewColumnName() {
        return newColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    @DatabaseChangeProperty(description = "Data type of the column")
    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @DatabaseChangeProperty(description = "Remarks of the column")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {
//todo    	if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//        }

    	return new Statement[] { new RenameColumnStatement(
                getCatalogName(),
                getSchemaName(),
    			getTableName(), getOldColumnName(), getNewColumnName(), 
    			getColumnDataType(),getRemarks())
        };
    }

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
        try {
            Database database = env.getTargetDatabase();

            ChangeStatus changeStatus = new ChangeStatus();
            Column newColumn = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getNewColumnName()), database);
            Column oldColumn = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getOldColumnName()), database);

            if (newColumn == null && oldColumn == null) {
                return changeStatus.unknown("Neither column exists");
            }
            if (newColumn != null && oldColumn != null) {
                return changeStatus.unknown("Both columns exist");
            }
            changeStatus.assertComplete(newColumn != null, "New column does not exist");

            return changeStatus;
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    private Statement[] generateStatementsForSQLiteDatabase(ExecutionEnvironment env) {
    	
    	// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...
    
    	List<Statement> statements = new ArrayList<Statement>();
    	
    	// define alter table logic
		AlterTableVisitor rename_alter_visitor = 
		new AlterTableVisitor() {
			@Override
            public ColumnConfig[] getColumnsToAdd() {
				return new ColumnConfig[0];
			}
			@Override
            public boolean copyThisColumn(ColumnConfig column) {
				return true;
			}
			@Override
            public boolean createThisColumn(ColumnConfig column) {
				if (column.getName().equals(getOldColumnName())) {
					column.setName(getNewColumnName());
				}
				return true;
			}
			@Override
            public boolean createThisIndex(Index index) {
				if (index.getColumns().contains(getOldColumnName())) {
					index.getColumns().remove(getOldColumnName());
					index.getColumns().add(getNewColumnName());
				}
				return true;
			}
		};
    		
    	try {
    		// alter table
			statements.addAll(SQLiteDatabase.getAlterTableStatements(
					rename_alter_visitor,
					env,getCatalogName(), getSchemaName(),getTableName()));
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
    	
    	return statements.toArray(new Statement[statements.size()]);
    }

    @Override
    protected Change[] createInverses() {
        RenameColumnChange inverse = new RenameColumnChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setOldColumnName(getNewColumnName());
        inverse.setNewColumnName(getOldColumnName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Column "+tableName+"."+ oldColumnName + " renamed to " + newColumnName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
