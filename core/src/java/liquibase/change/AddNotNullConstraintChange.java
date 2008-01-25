package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.sql.ReorganizeTableStatement;
import liquibase.database.sql.SetNullableStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.UpdateStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Types;
import java.util.*;

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
        this.schemaName = schemaName;
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


    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
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


    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("addNotNullConstraint");
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        element.setAttribute("defaultNullValue", getDefaultNullValue());
        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Table table = new Table(getTableName());

        Column column = new Column();
        column.setTable(table);
        column.setName(getColumnName());


        return new HashSet<DatabaseObject>(Arrays.asList(table, column));

    }
}
