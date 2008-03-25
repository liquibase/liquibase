package liquibase.change;

import liquibase.database.*;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.ReorganizeTableStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
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
public class ModifyColumnChange extends AbstractChange {

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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
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
