package liquibase.changelog.visitor;

import liquibase.change.Change;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.ResourceDependentChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.util.file.FilenameUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class ResourceValidationChangeSetVisitor implements ChangeSetVisitor {

    private final ValidationErrors validationErrors;
    private final ResourceAccessor resourceAccessor;

    public ResourceValidationChangeSetVisitor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
        validationErrors = new ValidationErrors();
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        validateChanges(changeSet.getChanges(), changeSet);
        validateChanges(changeSet.getRollback().getChanges(), changeSet);

    }

    private void validateChanges(List<Change> changes, ChangeSet changeSet) {
        for (Change change : changes) {
            if (change instanceof ResourceDependentChange) {
                ResourceDependentChange resourceDependentChange = (ResourceDependentChange) change;
                validateResourceDependentChange(resourceDependentChange);
            } else if (change instanceof ChangeWithColumns) {
                ChangeWithColumns<ColumnConfig> changeWithColumns = (ChangeWithColumns) change;
                validateChangeWithColumns(changeWithColumns, changeSet);
            }
        }
    }

    private void validateChangeWithColumns(ChangeWithColumns changeWithColumns, ChangeSet changeSet) {
        List<ColumnConfig> columns = changeWithColumns.getColumns();
        for (ColumnConfig columnConfig : columns) {
            String pathBlob = columnConfig.getValueBlobFile();
            String pathClob = columnConfig.getValueClobFile();
            validateLobFile(pathBlob, changeSet);
            validateLobFile(pathClob, changeSet);
        }
    }

    private void validateResourceDependentChange(ResourceDependentChange resourceDependentChange) {
        InputStream is = null;
        try {
            is = resourceDependentChange.openStream();
        } catch (IOException e) {
            validationErrors.addError(e.getMessage());
        } finally {
            closeInputStream(is);
        }
    }

    private void validateLobFile(String lobFilePath, ChangeSet changeSet) {
        if (lobFilePath != null) {
            InputStream is = null;
            try {
                is = getResourceAsStream(lobFilePath, changeSet);
                if (is == null) {
                    throw new FileNotFoundException(lobFilePath);
                }
            } catch (IOException e) {
                validationErrors.addError(e.getMessage());
            } finally {
                closeInputStream(is);
            }
        }
    }

    private void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* #getResourceAsStream is copy-pasted from ExecutablePreparedStatementBase, cause the one has logic for lob files. */
    private InputStream getResourceAsStream(String valueLobFile, ChangeSet changeSet) throws IOException {
        String fileName = getFileName(valueLobFile, changeSet);
        Set<InputStream> streams = this.resourceAccessor.getResourcesAsStream(fileName);
        if (streams == null || streams.size() == 0) {
            return null;
        }
        if (streams.size() > 1) {
            for (InputStream stream : streams) {
                stream.close();
            }

            throw new IOException(streams.size() + " matched " + valueLobFile);
        }
        return streams.iterator().next();
    }

    /* #getFileName is copy-pasted from ExecutablePreparedStatementBase, cause the one has logic for lob files. */
    private String getFileName(String fileName, ChangeSet changeSet) {

        String relativeBaseFileName = changeSet.getChangeLog().getPhysicalFilePath();

        String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeBaseFileName), fileName);
        if (tempFile != null) {
            fileName = tempFile;
        } else {
            fileName = FilenameUtils.getFullPath(relativeBaseFileName) + fileName;
        }

        return fileName;
    }

    public ValidationErrors getValidationErrors() {
        return validationErrors;
    }
}
