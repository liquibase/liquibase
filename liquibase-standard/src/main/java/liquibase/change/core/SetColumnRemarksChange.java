package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.SetColumnRemarksStatement;
import lombok.Setter;

@DatabaseChange(name = "setColumnRemarks", description = "Set remarks on a column", priority = ChangeMetaData.PRIORITY_DEFAULT)
@Setter
public class SetColumnRemarksChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String remarks;
    private String columnDataType;
    private String columnParentType;

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.addAll(super.validate(database));

        return validationErrors;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new SetColumnRemarksStatement(catalogName, schemaName, tableName, columnName, remarks, columnDataType, columnParentType)
        };
    }

    @DatabaseChangeProperty(description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the table containing the column to set remarks on")
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(description = "Name of the column to set remarks on")
    public String getColumnName() {
        return columnName;
    }

    @DatabaseChangeProperty(description = "A brief descriptive comment written to the column metadata.")
    public String getRemarks() {
        return remarks;
    }

    @Override
    public String getConfirmationMessage() {
        return "Remarks set on " + tableName + "." + columnName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @DatabaseChangeProperty(description = "Data type of the column")
    public String getColumnDataType() {
        return columnDataType;
    }

    @DatabaseChangeProperty(description = "Indicates the parent object type of the column we are setting remarks against. " +
        "Valid values are VIEW and TABLE. Default: TABLE.")
    public String getColumnParentType() {
        return columnParentType;
    }

}
