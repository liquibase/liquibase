package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.*;
import liquibase.database.statement.generator.SqlGeneratorFactory;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
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
 * Creates a new table.
 */
public class CreateTableChange extends AbstractChange implements ChangeWithColumns {

    private List<ColumnConfig> columns;
    private String schemaName;
    private String tableName;
    private String tablespace;
    private String remarks;

    public CreateTableChange() {
        super("createTable", "Create Table");
        columns = new ArrayList<ColumnConfig>();
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (getColumns().size() == 0) {
            throw new InvalidChangeDefinitionException("No columns defined", this);
        }
        for (ColumnConfig column : getColumns()) {
            if (StringUtils.trimToNull(column.getName()) == null) {
                throw new InvalidChangeDefinitionException("Column name is required", this);
            }
            if (StringUtils.trimToNull(column.getType()) == null) {
                throw new InvalidChangeDefinitionException("Column type is required", this);
            }
        }
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {

        String schemaName = getSchemaName() == null ? database.getDefaultSchemaName() : getSchemaName();
        CreateTableStatement statement = new CreateTableStatement(schemaName, getTableName());
        for (ColumnConfig column : getColumns()) {
            ConstraintsConfig constraints = column.getConstraints();
            boolean isAutoIncrement = column.isAutoIncrement() != null && column.isAutoIncrement();

            String defaultValue = null;
            if (column.hasDefaultValue()) {
                defaultValue = StringUtils.trimToNull(column.getDefaultColumnValue(database));
            }

            if (constraints != null && constraints.isPrimaryKey() != null && constraints.isPrimaryKey()) {

                statement.addPrimaryKeyColumn(column.getName(), database.getColumnType(column.getType(), isAutoIncrement), defaultValue, constraints.getPrimaryKeyName());

            } else {
                statement.addColumn(column.getName(),
                        database.getColumnType(column.getType(), column.isAutoIncrement()),
                        defaultValue);
            }


            if (constraints != null) {
                if (constraints.isNullable() != null && !constraints.isNullable()) {
                    statement.addColumnConstraint(new NotNullConstraint(column.getName()));
                }

                if (constraints.getReferences() != null) {
                    if (StringUtils.trimToNull(constraints.getForeignKeyName()) == null) {
                        throw new UnsupportedChangeException("createTable with references requires foreignKeyName");
                    }
                    ForeignKeyConstraint fkConstraint = new ForeignKeyConstraint(constraints.getForeignKeyName(), constraints.getReferences());
                    fkConstraint.setColumn(column.getName());
                    fkConstraint.setDeleteCascade(constraints.isDeleteCascade() != null && constraints.isDeleteCascade());
                    fkConstraint.setInitiallyDeferred(constraints.isInitiallyDeferred() != null && constraints.isInitiallyDeferred());
                    fkConstraint.setDeferrable(constraints.isDeferrable() != null && constraints.isDeferrable());
                    statement.addColumnConstraint(fkConstraint);
                }

                if (constraints.isUnique() != null && constraints.isUnique()) {
                    statement.addColumnConstraint(new UniqueConstraint(constraints.getUniqueConstraintName()).addColumns(column.getName()));
                }
            }

            if (isAutoIncrement) {
                statement.addColumnConstraint(new AutoIncrementConstraint(column.getName()));
            }
        }

        statement.setTablespace(StringUtils.trimToNull(getTablespace()));

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        statements.add(statement);

        if (StringUtils.trimToNull(remarks) != null) {
            SetTableRemarksStatement remarksStatement = new SetTableRemarksStatement(schemaName, tableName, remarks);
            if (SqlGeneratorFactory.getInstance().statementSupported(remarksStatement, database)) {
                statements.add(remarksStatement);
            }
        }

        for (ColumnConfig column : getColumns()) {
            String columnRemarks = StringUtils.trimToNull(column.getRemarks());
            if (columnRemarks != null) {
                SetColumnRemarksStatement remarksStatement = new SetColumnRemarksStatement(schemaName, tableName, column.getName(), columnRemarks);
                if (SqlGeneratorFactory.getInstance().statementSupported(remarksStatement, database)) {
                    statements.add(remarksStatement);
                }
            }
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    protected Change[] createInverses() {
        DropTableChange inverse = new DropTableChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());

        return new Change[]{
                inverse
        };
    }

    public List<ColumnConfig> getColumns() {
        return columns;
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


    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getConfirmationMessage() {
        return "Table " + tableName + " created";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("createTable");
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

        element.setAttribute("tableName", getTableName());
        if (StringUtils.trimToNull(tablespace) != null) {
            element.setAttribute("tablespace", tablespace);
        }
        if (StringUtils.trimToNull(remarks) != null) {
            element.setAttribute("remarks", remarks);
        }
        for (ColumnConfig column : getColumns()) {
            element.appendChild(column.createNode(currentChangeLogFileDOM));
        }
        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Table table = new Table(getTableName());
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
