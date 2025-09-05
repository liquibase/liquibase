package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterFileFormatChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;

public class ChangedFileFormatChangeGenerator implements ChangedObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (FileFormat.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, 
                             DiffOutputControl control, Database referenceDatabase, 
                             Database comparisonDatabase, ChangeGeneratorChain chain) {
        FileFormat fileFormat = (FileFormat) changedObject;
        
        // Check if there are any property differences
        if (!differences.hasDifferences()) {
            return new Change[0];
        }
        
        AlterFileFormatChange change = new AlterFileFormatChange();
        change.setFileFormatName(fileFormat.getName());
        
        // Only set properties that actually changed - using basic properties available in AlterFileFormatChange
        if (differences.isDifferent("formatType")) {
            change.setNewFileFormatType(fileFormat.getFormatType());
        }
        if (differences.isDifferent("compression")) {
            change.setCompression(fileFormat.getCompression());
        }
        if (differences.isDifferent("dateFormat")) {
            change.setDateFormat(fileFormat.getDateFormat());
        }
        if (differences.isDifferent("timeFormat")) {
            change.setTimeFormat(fileFormat.getTimeFormat());
        }
        if (differences.isDifferent("timestampFormat")) {
            change.setTimestampFormat(fileFormat.getTimestampFormat());
        }
        if (differences.isDifferent("binaryFormat")) {
            change.setBinaryFormat(fileFormat.getBinaryFormat());
        }
        if (differences.isDifferent("trimSpace")) {
            change.setTrimSpace(fileFormat.getTrimSpace());
        }
        if (differences.isDifferent("fieldDelimiter")) {
            change.setFieldDelimiter(fileFormat.getFieldDelimiter());
        }
        if (differences.isDifferent("skipHeader")) {
            change.setSkipHeader(fileFormat.getSkipHeader());
        }
        if (differences.isDifferent("nullIf")) {
            change.setNullIf(fileFormat.getNullIf());
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