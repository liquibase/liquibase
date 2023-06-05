package liquibase.change.core;

import static liquibase.change.ChangeParameterMetaData.ALL;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

/**
 * Drops a not-null constraint from an existing column.
 */
@DatabaseChange(
    name = "dropNotNullConstraint",
    description = "Makes a column nullable",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "column")
public class DropNotNullConstraintChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private String constraintName;
    private Boolean shouldValidate;

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting ="notNullConstraint.table.catalog",
        description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="notNullConstraint.table.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(
        description = "Name of the table containing the column to drop the constraint from",
        mustEqualExisting = "notNullConstraint.table"
    )
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(
        description = "Name of the column to drop the constraint from",
        mustEqualExisting = "notNullConstraint.column"
    )
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Current data type of the column",
        requiredForDatabase = {"informix", "mariadb", "mssql", "mysql"},
        supportsDatabase = ALL)
    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @DatabaseChangeProperty(description = "Name of the constraint to drop (if database supports names for NOT NULL constraints)")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] { new SetNullableStatement(
                getCatalogName(),
                getSchemaName(),
                getTableName(), getColumnName(), getColumnDataType(), true, getConstraintName())
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            Column snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
                new Column(
                    Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName()
                ),
                database
            );
            Boolean nullable = snapshot.isNullable();
            return new ChangeStatus().assertComplete((nullable == null) || nullable, "Column is not null");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }
    
    @Override
    protected Change[] createInverses() {
        AddNotNullConstraintChange inverse = new AddNotNullConstraintChange();
        inverse.setColumnName(getColumnName());
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Null constraint dropped from " + getTableName() + "." + getColumnName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
