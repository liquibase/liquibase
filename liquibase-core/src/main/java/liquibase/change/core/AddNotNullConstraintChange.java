package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;
import liquibase.statement.core.ReindexStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.statement.core.UpdateDataStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a not-null constraint to an existing column.
 */
@DatabaseChange(name="addNotNullConstraint",
        description = "Adds a not-null constraint to an existing table. If a defaultNullValue attribute is passed, all null values for the column will be updated to the passed value before the constraint is applied.",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class AddNotNullConstraintChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String defaultNullValue;
    private String columnDataType;

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

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Adds a not-null constraint to an existing table. If a defaultNullValue attribute is passed, all null values for the column will be updated to the passed value before the constraint is applied.")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.column", description = "Name of the column to add the constraint to")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Value to set all currently null values to. If not set, change will fail if null values exist")
    public String getDefaultNullValue() {
        return defaultNullValue;
    }

    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }

    @DatabaseChangeProperty(description = "Current data type of the column")
    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {

////        if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//        }

        Database database = env.getTargetDatabase();

    	List<Statement> statements = new ArrayList<Statement>();

        if (defaultNullValue != null) {
            statements.add(new UpdateDataStatement(getCatalogName(), getSchemaName(), getTableName())
                    .addNewColumnValue(getColumnName(), defaultNullValue)
                    .setWhere(database.escapeObjectName(getColumnName(), Column.class) + " IS NULL"));
        }
        
    	statements.add(new SetNullableStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), false));
        if (database instanceof DB2Database) {
            statements.add(new ReindexStatement(getCatalogName(), getSchemaName(), getTableName()));
        }           
        
        return statements.toArray(new Statement[statements.size()]);
    }

    private Statement[] generateStatementsForSQLiteDatabase(ExecutionEnvironment env) {
    	
    	// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...
    	
    	List<Statement> statements = new ArrayList<Statement>();
    	
        if (defaultNullValue != null) {
            statements.add(new UpdateDataStatement(getCatalogName(), getSchemaName(), getTableName())
                    .addNewColumnValue(getColumnName(), getDefaultNullValue())
                    .setWhere(getColumnName() + " IS NULL"));
        }

//		// ... test if column contains NULL values
//		if (defaultNullValue == null) {
//			List<Map> null_rows = null;
//			try {
//				null_rows = database.getExecutor().
//					queryForList(new RawSqlStatement(
//						"SELECT * FROM `"+
//						database.escapeTableName(getSchemaName(), getTableName())+
//						"` WHERE `"+getColumnName()+"` IS NULL;"));
//			} catch (DatabaseException e) {
//				e.printStackTrace();
//			}
//    		if (null_rows.size()>0) {
//    			throw new UnsupportedChangeException(
//    					"Failed to add a Not-Null-Constraint because " +
//    					"some values are null. Use the " +
//    					"defaultNullValue attribute to define default " +
//    					"values for the existing null values.");
//    		}
//    	}
		
		// define alter table logic
		AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
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
				if (column.getName().equals(getColumnName())) {
					column.getConstraints().setNullable(false);
				}
				return true;
			}
			@Override
            public boolean createThisIndex(Index index) {
				return true;
			}
		};
    		
		try {
    		// alter table
			statements.addAll(SQLiteDatabase.getAlterTableStatements(rename_alter_visitor, env,getCatalogName(), getSchemaName(),getTableName()));
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return statements.toArray(new Statement[statements.size()]);
    }

    @Override
    protected Change[] createInverses() {
        DropNotNullConstraintChange inverse = new DropNotNullConstraintChange();
        inverse.setColumnName(getColumnName());
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Null constraint has been added to " + getTableName() + "." + getColumnName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}