package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.SetColumnRemarksStatement;
import liquibase.statement.core.SetTableRemarksStatement;
import liquibase.util.StringUtils;

/**
 * Creates a new table.
 */
@ChangeClass(name="createTable", description = "Create Table", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateTableChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private List<ColumnConfig> columns;
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tablespace;
    private String remarks;

    public CreateTableChange() {
        columns = new ArrayList<ColumnConfig>();
    }

    public SqlStatement[] generateStatements(Database database) {

        CreateTableStatement statement = new CreateTableStatement(getCatalogName(), getSchemaName(), getTableName());
        for (ColumnConfig column : getColumns()) {
            ConstraintsConfig constraints = column.getConstraints();
            boolean isAutoIncrement = column.isAutoIncrement() != null && column.isAutoIncrement();

            Object defaultValue = column.getDefaultValueObject();

            LiquibaseDataType columnType = database.getDataTypeFactory().fromDescription(column.getType() + (isAutoIncrement ? "{autoIncrement:true}" : ""));
            if (constraints != null && constraints.isPrimaryKey() != null && constraints.isPrimaryKey()) {

                statement.addPrimaryKeyColumn(column.getName(), columnType, defaultValue, constraints.getPrimaryKeyName(), constraints.getPrimaryKeyTablespace());

            } else {
                statement.addColumn(column.getName(),
                        columnType,
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
                statement.addColumnConstraint(new AutoIncrementConstraint(column.getName(), column.getStartWith(), column.getIncrementBy()));
            }
        }

        statement.setTablespace(StringUtils.trimToNull(getTablespace()));

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        statements.add(statement);

        if (StringUtils.trimToNull(remarks) != null) {
            SetTableRemarksStatement remarksStatement = new SetTableRemarksStatement(catalogName, schemaName, tableName, remarks);
            if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                statements.add(remarksStatement);
            }
        }

        for (ColumnConfig column : getColumns()) {
            String columnRemarks = StringUtils.trimToNull(column.getRemarks());
            if (columnRemarks != null) {
                SetColumnRemarksStatement remarksStatement = new SetColumnRemarksStatement(catalogName, schemaName, tableName, column.getName(), columnRemarks);
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
        inverse.setCatalogName(getCatalogName());
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());

        return new Change[]{
                inverse
        };
    }

    @ChangeProperty(requiredForDatabase = "all")
    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @ChangeProperty(requiredForDatabase = "all")
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
