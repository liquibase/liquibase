package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.SetTableRemarksStatement;

import static liquibase.change.ChangeParameterMetaData.ALL;

@DatabaseChange(name="setTableRemarks", description = "Set remarks on a table", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class SetTableRemarksChange extends AbstractTableChange {

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
                new SetTableRemarksStatement(getCatalogName(), getSchemaName(), getTableName(), getRemarks())
        };
    }

    @DatabaseChangeProperty(description = "Comment to set on the table", requiredForDatabase = ALL)
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
