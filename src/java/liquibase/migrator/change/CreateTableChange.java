package liquibase.migrator.change;

import liquibase.database.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CreateTableChange extends AbstractChange {

    private List<ColumnConfig> columns;
    private String tableName;

    public CreateTableChange() {
        super("createTable", "Create Table");
        columns = new ArrayList<ColumnConfig>();
    }

    private String[] generateCommonStatements(AbstractDatabase database) {
        StringBuffer fkConstraints = new StringBuffer();

        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE ").append(getTableName()).append(" ");
        buffer.append("(");
        Iterator iterator = getColumns().iterator();
        while (iterator.hasNext()) {
            ColumnConfig column = (ColumnConfig) iterator.next();
            ConstraintsConfig constraints = column.getConstraints();
            buffer.append(column.getName());
            if (column.getType() != null) {
                buffer.append(" ").append(database.getColumnType(column));
            }

            if (constraints != null) {
                if (constraints.isNullable() != null && !constraints.isNullable()) {
                    buffer.append(" NOT NULL");
                }
                if (constraints.isPrimaryKey() != null && constraints.isPrimaryKey()) {
                    buffer.append(" PRIMARY KEY");
                }

                if (constraints.getReferences() != null) {
                    fkConstraints.append(" CONSTRAINT ")
                            .append(constraints.getForeignKeyName())
                            .append(" FOREIGN KEY (")
                            .append(column.getName())
                            .append(") REFERENCES ")
                            .append(constraints.getReferences());

                    if (constraints.isInitiallyDeferred() != null && constraints.isInitiallyDeferred()) {
                        fkConstraints.append(" INITIALLY DEFERRED");
                    }
                    if (constraints.isDeferrable() != null && constraints.isDeferrable()) {
                        fkConstraints.append(" DEFERRABLE,");
                    }
//                    buffer.append(" CONSTRAINT FOREIGN KEY ").append(constraints.getForeignKeyName()).append(" REFERENCES ").append(constraints.getReferences());
                }

                if (constraints.isUnique() != null && constraints.isUnique()) {
                    buffer.append(" UNIQUE");
                }
                if (constraints.getCheck() != null) buffer.append(constraints.getCheck()).append(" ");
            }

            if (column.getDefaultValue() != null) {
                buffer.append(" DEFAULT '").append(column.getDefaultValue()).append("'");
            }

            if (column.isAutoIncrement() != null && column.isAutoIncrement().booleanValue()) {
                buffer.append(" ").append(database.getAutoIncrementClause()).append(" ");
            }

            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }

        if (fkConstraints.length() > 0) {
            buffer.append(", ").append(fkConstraints.toString().replaceFirst(",$", ""));
        }
        buffer.append(")");
        return new String[] { buffer.toString().trim() };
    }

    public String[] generateStatements(MSSQLDatabase database) {
        return generateCommonStatements(database);
    }

    public String[] generateStatements(OracleDatabase database) {
        return generateCommonStatements(database);
    }

    public String[] generateStatements(MySQLDatabase database) {
        return generateCommonStatements(database);
    }

    public String[] generateStatements(PostgresDatabase database) {
        return generateCommonStatements(database);
    }

    protected AbstractChange[] createInverses() {
        DropTableChange inverse = new DropTableChange();
        inverse.setTableName(getTableName());

        return new AbstractChange[] {
                inverse
        };
    }

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }


    public String getConfirmationMessage() {
        return "Table " + tableName + " created";
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element element = currentMigrationFileDOM.createElement("createTable");
        element.setAttribute("name", getTableName());
        for (ColumnConfig column : getColumns()) {
            element.appendChild(column.createNode(currentMigrationFileDOM));
        }
        return element;
    }
}
