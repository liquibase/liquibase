package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.SetTableRemarksStatement;
import lombok.Setter;

@DatabaseChange(name = "setTableRemarks", description = "Set remarks on a table", priority = ChangeMetaData.PRIORITY_DEFAULT)
@Setter
public class SetTableRemarksChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String remarks;

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.addAll(super.validate(database));

        return validationErrors;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new SetTableRemarksStatement(catalogName, schemaName, tableName, remarks)
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

    @DatabaseChangeProperty(description = "Name of the table to set remarks on")
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(description = "A brief descriptive comment stored in the table metadata")
    public String getRemarks() {
        return remarks;
    }

    @Override
    public String getConfirmationMessage() {
        return "Remarks set on " + tableName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
