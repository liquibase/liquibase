package liquibase.change;

import liquibase.database.*;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.ReorganizeTableStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Index;
import liquibase.database.structure.Table;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        this.schemaName = schemaName;
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

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(tableName) == null) {
            throw new InvalidChangeDefinitionException("tableName is required", this);
        }

        for (ColumnConfig column : columns) {
            if (StringUtils.trimToNull(column.getName()) == null) {
                throw new InvalidChangeDefinitionException("column name is required", this);
            }
        }
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
    	
    	if (database instanceof SQLiteDatabase) {
    		// return special statements for SQLite databases
    		return generateStatementsForSQLiteDatabase(database);
        }
    	
    	List<SqlStatement> sql = new ArrayList<SqlStatement>();
    	
      for (ColumnConfig aColumn : columns) {

          String schemaName = getSchemaName() == null?database.getDefaultSchemaName():getSchemaName();
          if(database instanceof SybaseDatabase) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " MODIFY " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof MSSQLDatabase) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " ALTER COLUMN " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof MySQLDatabase) {
        		sql.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(schemaName, getTableName()) + " MODIFY COLUMN " + aColumn.getName() + " " + database.getColumnType(aColumn.getType(), false)));
        } else if (database instanceof OracleDatabase || database instanceof MaxDBDatabase) {
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
    
    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) 
			throws UnsupportedChangeException {

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
		} catch (JDBCException e) {
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

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("modifyColumn");
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }        
        node.setAttribute("tableName", getTableName());
        
        for (ColumnConfig col : getColumns()) {
          Element subNode = col.createNode(currentChangeLogFileDOM);
          node.appendChild(subNode);
        }

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
      List<DatabaseObject> result = new ArrayList<DatabaseObject>(columns.size());

      Table table = new Table(getTableName());
      for (ColumnConfig aColumn : columns) {
          Column each = new Column();
          each.setTable(table);
          each.setName(aColumn.getName());
          result.add(each);
      }

      return new HashSet<DatabaseObject>(result);
    }

}
