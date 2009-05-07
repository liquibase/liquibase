package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.structure.Index;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.statement.ReorganizeTableStatement;
import liquibase.statement.SetNullableStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.UpdateStatement;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds a not-null constraint to an existing column.
 */
public class AddNotNullConstraintChange extends AbstractChange {
    private String schemaName;
    private String tableName;
    private String columnName;
    private String defaultNullValue;
    private String columnDataType;


    public AddNotNullConstraintChange() {
        super("addNotNullConstraint", "Add Not-Null Constraint");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDefaultNullValue() {
        return defaultNullValue;
    }

    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    public SqlStatement[] generateStatements(Database database) {

////        if (database instanceof SQLiteDatabase) {
//    		// return special statements for SQLite databases
//    		return generateStatementsForSQLiteDatabase(database);
//        }

    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
    	String schemaName = getSchemaName() == null?database.getDefaultSchemaName():getSchemaName();
    	
        if (defaultNullValue != null) {
            statements.add(new UpdateStatement(schemaName, getTableName())
                    .addNewColumnValue(getColumnName(), getDefaultNullValue())
                    .setWhereClause(getColumnName() + " IS NULL"));
        }
        
    	statements.add(new SetNullableStatement(schemaName, getTableName(), getColumnName(), getColumnDataType(), false));
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(schemaName, getTableName()));
        }           
        
        return statements.toArray(new SqlStatement[statements.size()]);
    }
    
    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
    	
    	// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...
    	
    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
    	
    	String schemaName = getSchemaName() == null?database.getDefaultSchemaName():getSchemaName();
        if (defaultNullValue != null) {
            statements.add(new UpdateStatement(schemaName, getTableName())
                    .addNewColumnValue(getColumnName(), getDefaultNullValue())
                    .setWhereClause(getColumnName() + " IS NULL"));
        }
        
//		// ... test if column contains NULL values
//		if (defaultNullValue == null) {
//			List<Map> null_rows = null;
//			try {
//				null_rows = database.getJdbcTemplate().
//					queryForList(new RawSqlStatement(
//						"SELECT * FROM `"+
//						database.escapeTableName(getSchemaName(), getTableName())+
//						"` WHERE `"+getColumnName()+"` IS NULL;"));
//			} catch (JDBCException e) {
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
			public ColumnConfig[] getColumnsToAdd() {
				return new ColumnConfig[0];
			}
			public boolean copyThisColumn(ColumnConfig column) {
				return true;
			}
			public boolean createThisColumn(ColumnConfig column) {
				if (column.getName().equals(getColumnName())) {
					column.getConstraints().setNullable(false);
				}
				return true;
			}
			public boolean createThisIndex(Index index) {
				return true;
			}
		};
    		
		try {
    		// alter table
			statements.addAll(SQLiteDatabase.getAlterTableStatements(rename_alter_visitor, database,getSchemaName(),getTableName()));
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return statements.toArray(new SqlStatement[statements.size()]);
    }

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

    public String getConfirmationMessage() {
        return "Null constraint has been added to " + getTableName() + "." + getColumnName();
    }
}
