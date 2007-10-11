package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.*;
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
 * Creates a new table.
 */
public class CreateTableChange extends AbstractChange {

    private List<ColumnConfig> columns;
    private String tableName;
    private String tablespace;

    public CreateTableChange() {
        super("createTable", "Create Table");
        columns = new ArrayList<ColumnConfig>();
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {

        CreateTableStatement statement = new CreateTableStatement(tableName);
        for (ColumnConfig column : getColumns()) {
            ConstraintsConfig constraints = column.getConstraints();
            if (constraints != null && constraints.isPrimaryKey() != null && constraints.isPrimaryKey()) {
                boolean isAutoIncrement = column.isAutoIncrement() != null && column.isAutoIncrement();

                statement.addPrimaryKeyColumn(column.getName(),
                        database.getColumnType(column.getType(), column.isAutoIncrement()),
                        isAutoIncrement);

            } else {
                String defaultValue = null;
                if (column.hasDefaultValue()) {
                    defaultValue = StringUtils.trimToNull(column.getDefaultColumnValue(database));
                }
                statement.addColumn(column.getName(),
                        database.getColumnType(column.getType(), column.isAutoIncrement()),
                        defaultValue);
            }


            if (constraints != null) {
                if (constraints.isNullable() != null && !constraints.isNullable()) {
                    statement.addColumnConstraint(new NotNullConstraint(column.getName()));
                }

                if (constraints.getReferences() != null) {
                    ForeignKeyConstraint fkConstraint = new ForeignKeyConstraint(constraints.getForeignKeyName(), constraints.getReferences());
                    fkConstraint.setColumn(column.getName());
                    fkConstraint.setDeleteCascade(constraints.isDeleteCascade() != null && constraints.isDeleteCascade());
                    fkConstraint.setInitiallyDeferred(constraints.isInitiallyDeferred() != null && constraints.isInitiallyDeferred());
                    fkConstraint.setDeferrable(constraints.isDeferrable() != null && constraints.isDeferrable());
                    statement.addColumnConstraint(fkConstraint);
                }

                if (constraints.isUnique() != null && constraints.isUnique()) {
                    statement.addColumnConstraint(new UniqueConstraint().addColumns(column.getName()));
                }
            }
        }

        statement.setTablespace(StringUtils.trimToNull(getTablespace()));

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        statements.add(statement);

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    protected Change[] createInverses() {
        DropTableChange inverse = new DropTableChange();
        inverse.setTableName(getTableName());

        return new Change[]{
                inverse
        };
    }

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }


    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }


    public String getConfirmationMessage() {
        return "Table " + tableName + " created";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("createTable");
        element.setAttribute("tableName", getTableName());
        if (StringUtils.trimToNull(tablespace) != null) {
            element.setAttribute("tablespace", tablespace);
        }
        for (ColumnConfig column : getColumns()) {
            element.appendChild(column.createNode(currentChangeLogFileDOM));
        }
        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Table table = new Table();
        table.setName(tableName);
        returnSet.add(table);

        for (ColumnConfig columnConfig : getColumns()) {
            Column column = new Column();
            column.setTable(table);
            column.setName(columnConfig.getName());

            returnSet.add(column);
        }

        return returnSet;
    }
}
