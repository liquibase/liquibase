package liquibase.change.core;

import static liquibase.statement.SqlStatement.EMPTY_SQL_STATEMENT;

import java.util.ArrayList;
import java.util.List;

import liquibase.ChecksumVersion;
import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.SetColumnRemarksStatement;
import liquibase.statement.core.SetTableRemarksStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtil;
import lombok.Setter;

/**
 * Creates a new table.
 */
@DatabaseChange(name = "createTable", description = "Creates a table", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateTableChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private List<ColumnConfig> columns;
    /*Table type used by some RDBMS (Snowflake, SAP HANA) supporting different ... types ... of tables (e.g. column- vs. row-based) */
    @Setter
    private String tableType;
    @Setter
    private String catalogName;
    @Setter
    private String schemaName;
    @Setter
    private String tableName;
    @Setter
    private String tablespace;
    @Setter
    private String remarks;
    @Setter
    private Boolean ifNotExists;

    public CreateTableChange() {
        super();
        columns = new ArrayList<>();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.addAll(super.validate(database));

        if (columns != null) {
            for (ColumnConfig columnConfig : columns) {
                if (columnConfig.getType() == null && !ObjectUtil.defaultIfNull(columnConfig.getComputed(), false)) {
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
            boolean isAutoIncrement = (column.isAutoIncrement() != null) && column.isAutoIncrement();

            Object defaultValue = column.getDefaultValueObject();

            LiquibaseDataType columnType = null;
            if (column.getType() != null) {
                columnType = DataTypeFactory.getInstance().fromDescription(column.getType() + (isAutoIncrement ? "{autoIncrement:true}" : ""), database);
                isAutoIncrement |= columnType.isAutoIncrement();
            }

            if ((constraints != null) && (constraints.isPrimaryKey() != null) && constraints.isPrimaryKey()) {
                statement.addPrimaryKeyColumn(column.getName(), columnType, defaultValue, constraints.getValidatePrimaryKey(),
                        constraints.isDeferrable() != null && constraints.isDeferrable(),
                        constraints.isInitiallyDeferred() != null && constraints.isInitiallyDeferred(),
                    constraints.getPrimaryKeyName(),constraints.getPrimaryKeyTablespace(),
                        column.getRemarks());

            } else {
                statement.addColumn(column.getName(),
                        columnType,
                        column.getDefaultValueConstraintName(),
                        defaultValue,
                        column.getRemarks());
            }


            if (constraints != null) {
                if (constraints.isNullable() != null && !constraints.isNullable()) {
                    NotNullConstraint notNullConstraint = new NotNullConstraint(column.getName())
                            .setConstraintName(constraints.getNotNullConstraintName())
                            .setValidateNullable(constraints.getValidateNullable() == null || constraints.getValidateNullable());
                    statement.addColumnConstraint(notNullConstraint);
                }

                if ((constraints.getReferences() != null) || ((constraints.getReferencedTableName() != null) &&
                    (constraints.getReferencedColumnNames() != null))) {
                    if (StringUtil.trimToNull(constraints.getForeignKeyName()) == null) {
                        throw new UnexpectedLiquibaseException("createTable with references requires foreignKeyName");
                    }
                    ForeignKeyConstraint fkConstraint = new ForeignKeyConstraint(constraints.getForeignKeyName(),
                            constraints.getReferences(), constraints.getReferencedTableName(), constraints.getReferencedColumnNames());
                    fkConstraint.setReferencedTableCatalogName(constraints.getReferencedTableCatalogName());
                    fkConstraint.setReferencedTableSchemaName(constraints.getReferencedTableSchemaName());

                    fkConstraint.setColumn(column.getName());
                    fkConstraint.setDeleteCascade((constraints.isDeleteCascade() != null) && constraints
                        .isDeleteCascade());
                    fkConstraint.setInitiallyDeferred((constraints.isInitiallyDeferred() != null) && constraints
                        .isInitiallyDeferred());
                    fkConstraint.setDeferrable((constraints.isDeferrable() != null) && constraints.isDeferrable());
                    Boolean validate = constraints.getValidateForeignKey();
                    if (validate!=null) {
                        fkConstraint.setValidateForeignKey(constraints.getValidateForeignKey());
                    }
                    statement.addColumnConstraint(fkConstraint);
                }

                if ((constraints.isUnique() != null) && constraints.isUnique()) {
                    statement.addColumnConstraint(new UniqueConstraint(constraints.getUniqueConstraintName(),
                            constraints.getValidateUnique() == null || constraints.getValidateUnique()).addColumns(column.getName()));
                }
            }

            if (isAutoIncrement) {
                statement.addColumnConstraint(new AutoIncrementConstraint(column.getName(), column.getStartWith(), column.getIncrementBy(), column.getGenerationType(), column.getDefaultOnNull()));
            }
        }

        statement.setTablespace(StringUtil.trimToNull(getTablespace()));

        List<SqlStatement> statements = new ArrayList<>();
        statements.add(statement);

        if (StringUtil.trimToNull(remarks) != null && !(database instanceof MySQLDatabase)) {
            SetTableRemarksStatement remarksStatement = new SetTableRemarksStatement(catalogName, schemaName, tableName, remarks);
            if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                statements.add(remarksStatement);
            }
        }

        for (ColumnConfig column : getColumns()) {
            String columnRemarks = StringUtil.trimToNull(column.getRemarks());
            if (columnRemarks != null && !(database instanceof MySQLDatabase)) {
                SetColumnRemarksStatement remarksStatement = new SetColumnRemarksStatement(catalogName, schemaName, tableName, column.getName(), columnRemarks, column.getType());
                if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                    statements.add(remarksStatement);
                }
            }

            final Boolean computed = column.getComputed();
            if (computed != null && computed) {
                statement.setComputed(column.getName());
            }
        }

        return statements.toArray(EMPTY_SQL_STATEMENT);
    }

    protected CreateTableStatement generateCreateTableStatement() {
        return new CreateTableStatement(getCatalogName(), getSchemaName(), getTableName(), getRemarks(), getTableType(), Boolean.TRUE.equals(getIfNotExists()));
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
    public ChangeStatus checkStatus(Database database) {
        try {
            Table example = (Table) new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName());
            ChangeStatus status = new ChangeStatus();
            Table tableSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            status.assertComplete(tableSnapshot != null, "Table does not exist");

            if (tableSnapshot != null) {
                for (ColumnConfig columnConfig : getColumns()) {
                    Column columnSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(columnConfig).setRelation(tableSnapshot), database);
                    status.assertCorrect(columnSnapshot != null, "Column "+columnConfig.getName()+" is missing");
                    if (columnSnapshot != null) {
                        ConstraintsConfig constraints = columnConfig.getConstraints();
                        if (constraints != null) {
                            if ((constraints.isPrimaryKey() != null) && constraints.isPrimaryKey()) {
                                PrimaryKey tablePk = tableSnapshot.getPrimaryKey();
                                status.assertCorrect((tablePk != null) && tablePk.getColumnNamesAsList().contains
                                    (columnConfig.getName()), "Column "+columnConfig.getName()+" is not part of the primary key");
                            }
                            if (constraints.isNullable() != null) {
                                if (constraints.isNullable()) {
                                    status.assertCorrect((columnSnapshot.isNullable() == null) || columnSnapshot
                                        .isNullable(), "Column "+columnConfig.getName()+" nullability does not match");
                                } else {
                                    status.assertCorrect((columnSnapshot.isNullable() != null) && !columnSnapshot
                                        .isNullable(), "Column "+columnConfig.getName()+" nullability does not match");
                                }
                            }
                        }
                    }
                }
            }

            return status;
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    @Override
    @DatabaseChangeProperty(requiredForDatabase = "all", description = "Column definitions")
    public List<ColumnConfig> getColumns() {
        if (columns == null) {
            return new ArrayList<>();
        }
        return columns;
    }

    @Override
    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    @DatabaseChangeProperty(since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the table to create")
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(description = "Tablespace to create the table in. Corresponds to file group in mssql")
    public String getTablespace() {
        return tablespace;
    }

    @Override
    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    @DatabaseChangeProperty(description = "A brief descriptive comment to store in the table metadata")
    public String getRemarks() {
        return remarks;
    }

    @Override
    public String getConfirmationMessage() {
        return "Table " + tableName + " created";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @DatabaseChangeProperty(description = "In some databases, specifies the type of the table (column-based, row-based...)")
    public String getTableType() {
        return tableType;
    }

    @DatabaseChangeProperty(description = "If true, creates the table only if it does not already exist. Appends IF NOT EXISTS syntax to SQL query")
    public Boolean getIfNotExists() {
        return ifNotExists;
    }

    @Override
    public String[] getExcludedFieldFilters(ChecksumVersion version) {
        return new String[] {
                "ifNotExists"
        };
    }
}
