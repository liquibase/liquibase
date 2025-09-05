package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateFileFormatChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;

public class MissingFileFormatChangeGenerator implements MissingObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (FileFormat.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, 
                             Database referenceDatabase, Database comparisonDatabase, 
                             ChangeGeneratorChain chain) {
        FileFormat fileFormat = (FileFormat) missingObject;
        
        CreateFileFormatChange change = new CreateFileFormatChange();
        change.setFileFormatName(fileFormat.getName());
        
        // Map all FileFormat properties
        if (fileFormat.getFormatType() != null) {
            change.setFileFormatType(fileFormat.getFormatType());
        }
        if (fileFormat.getRecordDelimiter() != null) {
            change.setRecordDelimiter(fileFormat.getRecordDelimiter());
        }
        if (fileFormat.getFieldDelimiter() != null) {
            change.setFieldDelimiter(fileFormat.getFieldDelimiter());
        }
        if (fileFormat.getSkipHeader() != null) {
            change.setSkipHeader(fileFormat.getSkipHeader());
        }
        if (fileFormat.getFieldOptionallyEnclosedBy() != null) {
            change.setFieldOptionallyEnclosedBy(fileFormat.getFieldOptionallyEnclosedBy());
        }
        if (fileFormat.getEscape() != null) {
            change.setEscape(fileFormat.getEscape());
        }
        if (fileFormat.getTrimSpace() != null) {
            change.setTrimSpace(fileFormat.getTrimSpace());
        }
        if (fileFormat.getCompression() != null) {
            change.setCompression(fileFormat.getCompression());
        }
        if (fileFormat.getDateFormat() != null) {
            change.setDateFormat(fileFormat.getDateFormat());
        }
        if (fileFormat.getTimeFormat() != null) {
            change.setTimeFormat(fileFormat.getTimeFormat());
        }
        if (fileFormat.getTimestampFormat() != null) {
            change.setTimestampFormat(fileFormat.getTimestampFormat());
        }
        if (fileFormat.getBinaryFormat() != null) {
            change.setBinaryFormat(fileFormat.getBinaryFormat());
        }
        if (fileFormat.getNullIf() != null) {
            change.setNullIf(fileFormat.getNullIf());
        }
        if (fileFormat.getEscapeUnenclosedField() != null) {
            change.setEscapeUnenclosedField(fileFormat.getEscapeUnenclosedField());
        }
        if (fileFormat.getErrorOnColumnCountMismatch() != null) {
            change.setErrorOnColumnCountMismatch(fileFormat.getErrorOnColumnCountMismatch());
        }
        
        return new Change[] { change };
    }

    @Override
    public Change[] fixSchema(Change[] changes, liquibase.diff.compare.CompareControl.SchemaComparison[] schemaComparisons) {
        return changes;
    }

    @Override
    public Change[] fixOutputAsSchema(Change[] changes, liquibase.diff.compare.CompareControl.SchemaComparison[] schemaComparisons) {
        return changes;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }
}