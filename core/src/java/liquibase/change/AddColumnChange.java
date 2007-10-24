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

import java.util.*;

/**
 * Adds a column to an existing table.
 */
public class AddColumnChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private ColumnConfig column;

    public AddColumnChange() {
        super("addColumn", "Add Column");
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

    public ColumnConfig getColumn() {
        return column;
    }

    public void setColumn(ColumnConfig column) {
        this.column = column;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        List<SqlStatement> sql = new ArrayList<SqlStatement>();

        String alterTable = "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ADD " + getColumn().getName() + " " + database.getColumnType(getColumn().getType(), getColumn().isAutoIncrement());

        if (defaultClauseBeforeNotNull(database)) {
            alterTable += getDefaultClause(database);
        }

        if (column.getConstraints() != null) {
            if (column.getConstraints().isNullable() != null && !column.getConstraints().isNullable()) {
                alterTable += " NOT NULL";
            } else {
                if (database instanceof SybaseDatabase) {
                    alterTable += " NULL";
                }
            }
        } else {
            //For Sybase only the null is not optional and hence if no constraints are specified we need to default the value
            //to nullable
            if (database instanceof SybaseDatabase) {
                alterTable += " NULL";
            }
        }

        if (!defaultClauseBeforeNotNull(database)) {
            alterTable += getDefaultClause(database);
        }

        sql.add(new RawSqlStatement(alterTable));
        if (database instanceof DB2Database) {
            sql.add(new RawSqlStatement("CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + "')"));
        }

//        if (getColumn().getDefaultValue() != null
//                || getColumn().getDefaultValueBoolean() != null
//                || getColumn().getDefaultValueDate() != null
//                || getColumn().getDefaultValueNumeric() != null) {
//            AddDefaultValueChange change = new AddDefaultValueChange();
//            change.setTableName(getTableName());
//            change.setColumnName(getColumn().getName());
//            change.setDefaultValue(getColumn().getDefaultValue());
//            change.setDefaultValueNumeric(getColumn().getDefaultValueNumeric());
//            change.setDefaultValueDate(getColumn().getDefaultValueDate());
//            change.setDefaultValueBoolean(getColumn().getDefaultValueBoolean());
//
//            sql.addAll(Arrays.asList(change.generateStatements(database)));
//        }

        if (getColumn().getConstraints() != null) {
            if (getColumn().getConstraints().isPrimaryKey() != null && getColumn().getConstraints().isPrimaryKey()) {
                AddPrimaryKeyChange change = new AddPrimaryKeyChange();
                change.setTableName(getTableName());
                change.setColumnNames(getColumn().getName());

                sql.addAll(Arrays.asList(change.generateStatements(database)));
            }

//            if (getColumn().getConstraints().isNullable() != null && !getColumn().getConstraints().isNullable()) {
//                AddNotNullConstraintChange change = new AddNotNullConstraintChange();
//                change.setTableName(getTableName());
//                change.setColumnName(getColumn().getName());
//                change.setColumnDataType(getColumn().getType());
//
//                sql.addAll(Arrays.asList(change.generateStatements(database)));
//            }
        }

        return sql.toArray(new SqlStatement[sql.size()]);
    }

    private boolean defaultClauseBeforeNotNull(Database database) {
        return database instanceof OracleDatabase
                || database instanceof HsqlDatabase
                || database instanceof DerbyDatabase
                || database instanceof DB2Database
                || database instanceof FirebirdDatabase;
    }

    private String getDefaultClause(Database database) {
        String clause = "";
        if (column.getDefaultValue() != null
                || column.getDefaultValueBoolean() != null
                || column.getDefaultValueDate() != null
                || column.getDefaultValueNumeric() != null) {
            if (database instanceof MSSQLDatabase) {
                clause += " CONSTRAINT "+ ((MSSQLDatabase) database).generateDefaultConstraintName(tableName, column.getName());
            }
            clause += " DEFAULT " + column.getDefaultColumnValue(database);
        }
        return clause;
    }

    protected Change[] createInverses() {
        List<Change> inverses = new ArrayList<Change>();

        if (column.hasDefaultValue()) {
            DropDefaultValueChange dropChange = new DropDefaultValueChange();
            dropChange.setTableName(getTableName());
            dropChange.setColumnName(getColumn().getName());

            inverses.add(dropChange);
        }


        DropColumnChange inverse = new DropColumnChange();
        inverse.setColumnName(getColumn().getName());
        inverse.setTableName(getTableName());
        inverses.add(inverse);

        return inverses.toArray(new Change[inverses.size()]);
    }

    public String getConfirmationMessage() {
        return "Column " + column.getName() + "(" + column.getType() + ") added to " + tableName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement("addColumn");
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }
        node.setAttribute("tableName", getTableName());
        node.appendChild(getColumn().createNode(currentChangeLogFileDOM));

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Column column = new Column();

        Table table = new Table(getTableName());
        column.setTable(table);

        column.setName(this.column.getName());

        return new HashSet<DatabaseObject>(Arrays.asList(table, column));

    }
}
