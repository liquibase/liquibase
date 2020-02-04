package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.SetColumnRemarksStatement;

import static liquibase.change.ChangeParameterMetaData.ALL;

@DatabaseChange(name="setColumnRemarks", description = "Set remarks on a column", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class SetColumnRemarksChange extends AbstractTableChange {

    private String columnName;
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
                new SetColumnRemarksStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getRemarks())
        };
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Comment to set on the column", requiredForDatabase = ALL)
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String getConfirmationMessage() {
        return "Remarks set on " + tableName+"."+columnName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
