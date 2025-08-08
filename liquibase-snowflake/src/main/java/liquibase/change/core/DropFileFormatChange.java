package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropFileFormatStatement;

/**
 * Drops a file format in Snowflake.
 */
@DatabaseChange(
    name = "dropFileFormat",
    description = "Drops a file format",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "fileFormat",
    since = "4.33"
)
public class DropFileFormatChange extends AbstractChange {

    private String fileFormatName;
    private String catalogName;
    private String schemaName;
    private Boolean ifExists;

    @DatabaseChangeProperty(description = "Name of the file format to drop", requiredForDatabase = "snowflake")
    public String getFileFormatName() {
        return fileFormatName;
    }

    public void setFileFormatName(String fileFormatName) {
        this.fileFormatName = fileFormatName;
    }

    @DatabaseChangeProperty(description = "Catalog (database) name")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(description = "Schema name")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Use IF EXISTS clause")
    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        DropFileFormatStatement statement = new DropFileFormatStatement();
        statement.setFileFormatName(getFileFormatName());
        statement.setCatalogName(getCatalogName());
        statement.setSchemaName(getSchemaName());
        statement.setIfExists(getIfExists());

        return new SqlStatement[] { statement };
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.addAll(super.validate(database));

        if (getFileFormatName() == null || getFileFormatName().trim().isEmpty()) {
            validationErrors.addError("fileFormatName is required");
        }

        return validationErrors;
    }

    @Override
    public boolean supportsRollback(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public String getConfirmationMessage() {
        return "File format " + getFileFormatName() + " dropped";
    }
}