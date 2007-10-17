package liquibase.change;

import liquibase.database.*;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.SqlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Removes the default value from an existing column.
 */
public class DropDefaultValueChange extends AbstractChange {
    private String tableName;
    private String columnName;

    public DropDefaultValueChange() {
        super("dropDefaultValue", "Drop Default Value");
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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof MSSQLDatabase) {
//smarter drop        return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " DROP CONSTRAINT select d.name from syscolumns c,sysobjects d, sysobjects t where c.id=t.id AND d.parent_obj=t.id AND d.type='D' AND t.type='U' AND c.name='"+getColumnName()+"' AND t.name='"+getTableName()+"'"),};
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " DROP CONSTRAINT "+((MSSQLDatabase) database).generateDefaultConstraintName(getTableName(), getColumnName())),};
        } else if (database instanceof MySQLDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " ALTER " + getColumnName() + " DROP DEFAULT"),};
        } else if (database instanceof OracleDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " MODIFY " + getColumnName() + " DEFAULT NULL"),};
        } else if (database instanceof DerbyDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " ALTER COLUMN  " + getColumnName() + " WITH DEFAULT NULL"),};
        }

        return new SqlStatement[]{
                new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " ALTER COLUMN  " + getColumnName() + " SET DEFAULT NULL"),
        };
    }

    public String getConfirmationMessage() {
        return "Default value dropped from "+getTableName()+"."+getColumnName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        node.setAttribute("tableName", getTableName());
        node.setAttribute("columnName", getColumnName());

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Column column = new Column();

        Table table = new Table();
        table.setName(tableName);
        column.setTable(table);

        column.setName(columnName);

        return new HashSet<DatabaseObject>(Arrays.asList(table, column));

    }
    
}
