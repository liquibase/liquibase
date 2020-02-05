package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import static liquibase.change.ChangeParameterMetaData.ALL;

/**
 * Drops a not-null constraint from an existing column.
 */
@DatabaseChange(
    name="dropNotNullConstraint",
    description = "Makes a column nullable",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "column")
public class DropNotNullConstraintChange extends AbstractTableChange {

    private String columnName;
    private String columnDataType;

    @DatabaseChangeProperty(
        description = "Name of the table containing that the column to drop the constraint from",
        mustEqualExisting = "notNullConstraint.table", requiredForDatabase = ALL
    )
    public String getTableName() {
        return super.getTableName();
    }

    @DatabaseChangeProperty(
        description = "Name of the column to drop the constraint from",
        mustEqualExisting = "notNullConstraint.column", requiredForDatabase = ALL
    )
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Current data type of the column")
    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] { new SetNullableStatement(
                getCatalogName(),
                getSchemaName(),
                getTableName(), getColumnName(), getColumnDataType(), true)
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

        return new Change[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Null constraint dropped from " + getTableName() + "." + getColumnName();
    }
}
