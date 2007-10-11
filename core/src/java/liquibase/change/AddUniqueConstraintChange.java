package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.StringUtils;
import liquibase.util.SqlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Adds a unique constraint to an existing column.
 */
public class AddUniqueConstraintChange extends AbstractChange {

    private String tableName;
    private String columnNames;
    private String constraintName;
    private String tablespace;

    public AddUniqueConstraintChange() {
        super("addUniqueConstraint", "Add Unique Constraint");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }


    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        String sql = "ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " ADD CONSTRAINT " + getConstraintName() + " UNIQUE (" + getColumnNames() + ")";

        if (StringUtils.trimToNull(getTablespace()) != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase) {
                sql += " ON "+getTablespace();
            } else if (database instanceof DB2Database) {
                ; //not supported in DB2
            } else {
                sql += " USING INDEX TABLESPACE "+getTablespace();
            }
        }
        

        return new SqlStatement[]{
                new RawSqlStatement(sql)
        };
    }

    public String getConfirmationMessage() {
        return "Unique constraint added to "+getTableName()+"("+getColumnNames()+")";
    }

    protected Change[] createInverses() {
        DropUniqueConstraintChange inverse = new DropUniqueConstraintChange();
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse,
        };
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement(getTagName());
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnNames", getColumnNames());
        element.setAttribute("constraintName", getConstraintName());

        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Table table = new Table();
        table.setName(tableName);
        returnSet.add(table);

        for (String columnName : getColumnNames().split(",")) {
            Column column = new Column();
            column.setTable(table);
            column.setName(columnName.trim());

            returnSet.add(column);
        }

        return returnSet;

    }

}
