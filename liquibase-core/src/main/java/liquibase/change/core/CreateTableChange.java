package liquibase.change.core;

import liquibase.action.ActionStatus;
import liquibase.change.*;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.*;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.SetTableRemarksStatement;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a new table.
 */
@DatabaseChange(name="createTable", description = "Create Table", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateTableChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private List<ColumnConfig> columns;
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tablespace;
    private String remarks;

    public CreateTableChange() {
        super();
        columns = new ArrayList<ColumnConfig>();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.addAll(super.validate(database));

        if (columns != null) {
            for (ColumnConfig columnConfig : columns) {
                if (columnConfig.getType() == null) {
                    validationErrors.addError("column 'type' is required for all columns");
                }
                if (columnConfig.getName() == null) {
                    validationErrors.addError("column 'name' is required for all columns");
                }
            }
        }
        return validationErrors;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        CreateTableStatement statement = generateCreateTableStatement();
        for (ColumnConfig column : getColumns()) {
            ConstraintsConfig constraints = column.getConstraints();
            boolean isAutoIncrement = column.isAutoIncrement() != null && column.isAutoIncrement();

            Object defaultValue = column.getDefaultValueObject();

            LiquibaseDataType columnType = DataTypeFactory.getInstance().fromDescription(column.getType() + (isAutoIncrement ? "{autoIncrement:true}" : ""), database);
            if (constraints != null && constraints.isPrimaryKey() != null && constraints.isPrimaryKey()) {

                statement.addPrimaryKeyColumn(column.getName(), columnType, defaultValue, constraints.getPrimaryKeyName(), constraints.getPrimaryKeyTablespace());

            } else {
                statement.addColumn(column.getName(),
                        columnType,
                        defaultValue,
                        column.getRemarks());
            }


            if (constraints != null) {
                if (constraints.isNullable() != null && !constraints.isNullable()) {
                    statement.addColumnConstraint(new NotNullConstraint(column.getName()));
                }

                if (constraints.getReferences() != null ||
                        (constraints.getReferencedTableName() != null && constraints.getReferencedColumnNames() != null)) {
                    if (StringUtils.trimToNull(constraints.getForeignKeyName()) == null) {
                        throw new UnexpectedLiquibaseException("createTable with references requires foreignKeyName");
                    }
                    ForeignKeyConstraint fkConstraint = new ForeignKeyConstraint(constraints.getForeignKeyName(),
                            constraints.getReferences(), constraints.getReferencedTableName(), constraints.getReferencedColumnNames());
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


        return statements.toArray(new SqlStatement[statements.size()]);
    }

    protected CreateTableStatement generateCreateTableStatement() {
        return new CreateTableStatement(getCatalogName(), getSchemaName(), getTableName(),getRemarks());
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

    @Override
    public ActionStatus checkStatus(Database database) {
        try {
            Table example = (Table) new Table(new ObjectName(getCatalogName(), getSchemaName(), getTableName()));
            ActionStatus status = new ActionStatus();
            Table tableSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            status.assertApplied(tableSnapshot != null, "Table does not exist");

            if (tableSnapshot != null) {
                for (ColumnConfig columnConfig : getColumns()) {
                    Column exampleToSnapshot = new Column(columnConfig);
//                    exampleToSnapshot.relation = tableSnapshot;
                    Column columnSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(exampleToSnapshot, database);
                    status.assertCorrect(columnSnapshot != null, "Column "+columnConfig.getName()+" is missing");
                    if (columnSnapshot != null) {
                        ConstraintsConfig constraints = columnConfig.getConstraints();
                        if (constraints != null) {
                            if (constraints.isPrimaryKey() != null && constraints.isPrimaryKey()) {
                                PrimaryKey tablePk = tableSnapshot.primaryKey;
                                status.assertCorrect(tablePk != null && tablePk.getColumnNamesAsList().contains(columnConfig.getName()), "Column "+columnConfig.getName()+" is not part of the primary key");
                            }
                            if (constraints.isNullable() != null) {
                                if (constraints.isNullable()) {
                                    status.assertCorrect(columnSnapshot.nullable == null || columnSnapshot.nullable, "Column "+columnConfig.getName()+" nullability does not match");
                                } else {
                                    status.assertCorrect(columnSnapshot.nullable != null && !columnSnapshot.nullable, "Column "+columnConfig.getName()+" nullability does not match");
                                }
                            }
                        }
                    }
                }
            }

            return status;
        } catch (Exception e) {
            return new ActionStatus().unknown(e);
        }
    }

    @Override
    @DatabaseChangeProperty(requiredForDatabase = "all")
    public List<ColumnConfig> getColumns() {
        if (columns == null) {
            return new ArrayList<ColumnConfig>();
        }
        return columns;
    }

    @Override
    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    @DatabaseChangeProperty(since = "3.0")
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

    @DatabaseChangeProperty()
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

    @Override
    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String getConfirmationMessage() {
        return "Table " + tableName + " created";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
