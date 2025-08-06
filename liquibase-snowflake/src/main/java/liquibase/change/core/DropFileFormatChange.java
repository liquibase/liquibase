package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Catalog;
import liquibase.CatalogAndSchema;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropFileFormatStatement;
import liquibase.structure.core.Schema;

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
    public SqlStatement[] generateRollbackStatements(Database database) throws RuntimeException {
        try {
            // Capture the file format definition before dropping it
            Schema schema = new Schema(getCatalogName(), getSchemaName());
            FileFormat fileFormat = new FileFormat(getFileFormatName());
            fileFormat.setSchema(schema);
            
            // Create a database snapshot to capture the current state
            SnapshotControl snapshotControl = new SnapshotControl(database, FileFormat.class);
            DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(new CatalogAndSchema(getCatalogName(), getSchemaName()), database, snapshotControl);
            
            // Find the existing file format in the snapshot
            FileFormat existingFormat = null;
            for (FileFormat format : snapshot.get(FileFormat.class)) {
                if (format.getName().equals(getFileFormatName())) {
                    existingFormat = format;
                    break;
                }
            }
            
            if (existingFormat == null) {
                throw new RuntimeException("Cannot rollback DROP FILE FORMAT: " + 
                    getFileFormatName() + " does not exist in database");
            }
            
            // Create a CreateFileFormatChange to restore the file format
            CreateFileFormatChange rollbackChange = new CreateFileFormatChange();
            rollbackChange.setFileFormatName(existingFormat.getName());
            
            if (existingFormat.getSchema() != null) {
                rollbackChange.setCatalogName(existingFormat.getSchema().getCatalogName());
                rollbackChange.setSchemaName(existingFormat.getSchema().getName());
            }
            
            // Copy all properties from the existing format
            rollbackChange.setFileFormatType(existingFormat.getFormatType());
            rollbackChange.setCompression(existingFormat.getCompression());
            rollbackChange.setRecordDelimiter(existingFormat.getRecordDelimiter());
            rollbackChange.setFieldDelimiter(existingFormat.getFieldDelimiter());
            rollbackChange.setSkipHeader(existingFormat.getSkipHeader());
            rollbackChange.setDateFormat(existingFormat.getDateFormat());
            rollbackChange.setTimeFormat(existingFormat.getTimeFormat());
            rollbackChange.setTimestampFormat(existingFormat.getTimestampFormat());
            rollbackChange.setBinaryFormat(existingFormat.getBinaryFormat());
            rollbackChange.setTrimSpace(existingFormat.getTrimSpace());
            rollbackChange.setNullIf(existingFormat.getNullIf());
            rollbackChange.setSkipBlankLines(existingFormat.getSkipBlankLines());
            rollbackChange.setErrorOnColumnCountMismatch(existingFormat.getErrorOnColumnCountMismatch());
            rollbackChange.setEmptyFieldAsNull(existingFormat.getEmptyFieldAsNull());
            
            // Additional properties from requirements
            rollbackChange.setReplaceInvalidCharacters(existingFormat.getReplaceInvalidCharacters());
            rollbackChange.setSkipByteOrderMark(existingFormat.getSkipByteOrderMark());
            rollbackChange.setEncoding(existingFormat.getEncoding());
            rollbackChange.setFileExtension(existingFormat.getFileExtension());
            rollbackChange.setEscape(existingFormat.getEscape());
            rollbackChange.setEscapeUnenclosedField(existingFormat.getEscapeUnenclosedField());
            rollbackChange.setFieldOptionallyEnclosedBy(existingFormat.getFieldOptionallyEnclosedBy());
            rollbackChange.setParseHeader(existingFormat.getParseHeader());
            
            return rollbackChange.generateStatements(database);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate rollback for DROP FILE FORMAT: " + 
                e.getMessage(), e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "File format " + getFileFormatName() + " dropped";
    }
}