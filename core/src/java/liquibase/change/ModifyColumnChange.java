package liquibase.change;

import liquibase.database.*;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.structure.Index;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.statement.RawSqlStatement;
import liquibase.statement.ReorganizeTableStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Modifies the data type of an existing column.
 */
public class ModifyColumnChange extends AbstractChange implements ChangeWithColumns {

    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;

    public ModifyColumnChange() {
        super("modifyColumn", "Modify Column");
        columns = new ArrayList<ColumnConfig>();
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

    public List<ColumnConfig> getColumns() {
    		return columns;
    }

    public void addColumn(ColumnConfig column) {
      	columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
      	columns.remove(column);
    }

    public SqlStatement[] generateStatements(Database database) {
    	
    	if (database instanceof SQLiteDatabase) {
    		// return special statements for SQLite databases
    		return generateStatementsForSQLiteDatabase(database);
        }
    	
    	List<SqlStatement> sql = new ArrayList<SqlStatement>();
    	
      for (ColumnConfig aColumn : columns) {

          String schemaName = getSchemaName() == null?database.getDefaultSchemaName():getSchemaName();
          if(database instanceof SybaseASADatabase || database instanceof SybaseDatabase) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " MODIFY " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof MSSQLDatabase) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " ALTER COLUMN " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof MySQLDatabase) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " MODIFY COLUMN " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof OracleDatabase || database instanceof MaxDBDatabase || database instanceof InformixDatabase) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " MODIFY (" + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false) + ")"));
        } else if (database instanceof DerbyDatabase) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " ALTER COLUMN "+aColumn.getName()+" SET DATA TYPE " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof HsqlDatabase) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " ALTER COLUMN "+aColumn.getName()+" "+database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof CacheDatabase) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " ALTER COLUMN " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof DB2Database) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " ALTER COLUMN " + aColumn.getName() + " SET DATA TYPE " + database.getColumnType(aColumn.getType(), false)));
        		sql.add(new ReorganizeTableStatement(schemaName, getTableName()));
        } else {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " ALTER COLUMN " + aColumn.getName() + " TYPE " + database.getColumnType(aColumn.getType(), false)));
        }
      }
        
      return sql.toArray(new SqlStatement[sql.size()]);
    }
    
    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {

		// SQLite does not support this ALTER TABLE operation until now.
		// For more information see: http://www.sqlite.org/omitted.html.
		// This is a small work around...
    	
    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
    	
    	// define alter table logic
		AlterTableVisitor rename_alter_visitor = 
		new AlterTableVisitor() {
			public ColumnConfig[] getColumnsToAdd() {
				return new ColumnConfig[0];
			}
			public boolean copyThisColumn(ColumnConfig column) {
				return true;
			}
			public boolean createThisColumn(ColumnConfig column) {
				for (ColumnConfig cur_column: columns) {
					if (cur_column.getName().equals(column.getName())) {
						column.setType(cur_column.getType());
						break;
					}
				}
				return true;
			}
			public boolean createThisIndex(Index index) {
				return true;
			}
		};
    		
    	try {
    		// alter table
			statements.addAll(SQLiteDatabase.getAlterTableStatements(
					rename_alter_visitor,
					database,getSchemaName(),getTableName()));
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
    	
    	return statements.toArray(new SqlStatement[statements.size()]);    	
    }

    public String getConfirmationMessage() {
    		List<String> names = new ArrayList<String>(columns.size());
    		for (ColumnConfig col : columns) {
          	names.add(col.getName() + "(" + col.getType() + ")");
    		}

        return "Columns " + StringUtils.join(names, ",") + " of " + getTableName() + " modified";
    }
}
