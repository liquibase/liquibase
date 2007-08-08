package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.migrator.exception.UnsupportedChangeException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Creates a new table.
 */
public class CreateTableChange extends AbstractChange {

    private List<ColumnConfig> columns;
    private String tableName;

    public CreateTableChange() {
        super("createTable", "Create Table");
        columns = new ArrayList<ColumnConfig>();
    }

    public String[] generateStatements(Database database) throws UnsupportedChangeException {

        Set<String> pkColumns = new HashSet<String>();

        for (ColumnConfig column : getColumns()) {
            if (column.getConstraints() != null && column.getConstraints().isPrimaryKey() != null && column.getConstraints().isPrimaryKey()) {
                pkColumns.add(column.getName());
            }
        }

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

            if (column.getDefaultValue() != null
                    || column.getDefaultValueBoolean() != null
                    || column.getDefaultValueDate() != null
                    || column.getDefaultValueNumeric() != null) {
                buffer.append(" DEFAULT ").append(column.getDefaultColumnValue(database));
            }
            
            if (column.isAutoIncrement() != null && column.isAutoIncrement()) {
                buffer.append(" ").append(database.getAutoIncrementClause()).append(" ");
            }

            if (constraints != null) {
                if (constraints.isNullable() != null && !constraints.isNullable()) {
                    buffer.append(" NOT NULL");
                } else {
//                    buffer.append(" NULL");
                }
                if (pkColumns.size() == 1 && constraints.isPrimaryKey() != null && constraints.isPrimaryKey()) {
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
                        fkConstraints.append(" DEFERRABLE");
                    }
                    fkConstraints.append(",");
//                    buffer.append(" CONSTRAINT FOREIGN KEY ").append(constraints.getForeignKeyName()).append(" REFERENCES ").append(constraints.getReferences());
                }

                if (constraints.isUnique() != null && constraints.isUnique()) {
                    buffer.append(" UNIQUE");
                }
                if (constraints.getCheck() != null) buffer.append(constraints.getCheck()).append(" ");

            }

            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }

        if (fkConstraints.length() > 0) {
            buffer.append(", ").append(fkConstraints.toString().replaceFirst(",$", ""));
        }
        buffer.append(")");

        List<String> statements = new ArrayList<String>();
        statements.add(buffer.toString().trim());

        if (pkColumns.size() > 1) {
            AddPrimaryKeyChange addPKChange = new AddPrimaryKeyChange();
            addPKChange.setTableName(getTableName());
            addPKChange.setConstraintName(("PK_"+getTableName()).toUpperCase());
            addPKChange.setColumnNames(StringUtils.join(pkColumns, ","));

            statements.addAll(Arrays.asList(addPKChange.generateStatements(database)));
        }

        return statements.toArray(new String[statements.size()]);
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

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("createTable");
        element.setAttribute("tableName", getTableName());
        for (ColumnConfig column : getColumns()) {
            element.appendChild(column.createNode(currentChangeLogFileDOM));
        }
        return element;
    }
}
