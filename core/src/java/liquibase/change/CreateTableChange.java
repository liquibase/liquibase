package liquibase.change;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.*;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
        super("createTable", "Create Table", ChangeMetaData.PRIORITY_DEFAULT);
        columns = new ArrayList<ColumnConfig>();
    }

    public SqlStatement[] generateStatements(Database database) {

        String schemaName = getSchemaName() == null ? (database == null ? null: database.getDefaultSchemaName()) : getSchemaName();
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
                        database == null ? column.getType() : database.getColumnType(column.getType(), column.isAutoIncrement()),
                        defaultValue);
            }


            if (constraints != null) {
                if (constraints.isNullable() != null && !constraints.isNullable()) {
                    statement.addColumnConstraint(new NotNullConstraint(column.getName()));
                }

                if (constraints.getReferences() != null) {
                    if (StringUtils.trimToNull(constraints.getForeignKeyName()) == null) {
                        throw new UnexpectedLiquibaseException("createTable with references requires foreignKeyName");
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
            if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                statements.add(remarksStatement);
            }
        }

        for (ColumnConfig column : getColumns()) {
            String columnRemarks = StringUtils.trimToNull(column.getRemarks());
            if (columnRemarks != null) {
                SetColumnRemarksStatement remarksStatement = new SetColumnRemarksStatement(schemaName, tableName, column.getName(), columnRemarks);
                if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                    statements.add(remarksStatement);
                }
            }
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
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
}
