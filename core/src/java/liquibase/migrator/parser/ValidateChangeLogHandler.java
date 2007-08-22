package liquibase.migrator.parser;

import liquibase.migrator.ChangeSet;
import liquibase.migrator.FileOpener;
import liquibase.migrator.IncludeMigrator;
import liquibase.migrator.Migrator;
import liquibase.migrator.change.Change;
import liquibase.migrator.exception.*;
import liquibase.migrator.preconditions.FailedPrecondition;
import liquibase.migrator.preconditions.PreconditionSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidateChangeLogHandler extends BaseChangeLogHandler {

    private static List<ChangeSet> invalidMD5Sums;
    private static List<FailedPrecondition> failedPreconditions;
    private static Set<ChangeSet> duplicateChangeSets;
    private static List<SetupException> setupExceptions;

    private Set<String> seenChangeSets = new HashSet<String>();


    public ValidateChangeLogHandler(Migrator migrator, String physicalChangeLogLocation, FileOpener fileOpener) {
        super(migrator, physicalChangeLogLocation,fileOpener);
        if (invalidMD5Sums == null) {
            invalidMD5Sums = new ArrayList<ChangeSet>();
        }

        if (failedPreconditions == null) {
            failedPreconditions = new ArrayList<FailedPrecondition>();
        }

        if (duplicateChangeSets == null) {
            duplicateChangeSets = new HashSet<ChangeSet>();
        }

        if (setupExceptions == null) {
            setupExceptions = new ArrayList<SetupException>();
        }
    }

    protected void handleChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException, MigrationFailedException, IOException {
        for(Change change : changeSet.getChanges()) {
            try {
                change.setUp();
            } catch(SetupException se) {
                setupExceptions.add(se);
            }
        }
        
        if (changeSet.getDatabaseChangeLog().getMigrator().getRunStatus(changeSet).equals(ChangeSet.RunStatus.INVALID_MD5SUM)) {
            invalidMD5Sums.add(changeSet);
        }

        String changeSetString = changeSet.toString(false);
        if (seenChangeSets.contains(changeSetString)) {
            duplicateChangeSets.add(changeSet);
        } else {
            seenChangeSets.add(changeSetString);
        }
    }


    protected void handlePreCondition(PreconditionSet preconditions) {
        try {
            preconditions.checkConditions();
        } catch (PreconditionFailedException e) {
            failedPreconditions.addAll(e.getFailedPreconditions());
        }
    }

    protected void handleIncludedChangeLog(String fileName) throws MigrationFailedException, IOException, JDBCException {
        new IncludeMigrator(fileName, migrator).validate();
    }


    public List<ChangeSet> getInvalidMD5Sums() {
        return invalidMD5Sums;
    }


    public List<FailedPrecondition> getFailedPreconditions() {
        return failedPreconditions;
    }


    public Set<ChangeSet> getDuplicateChangeSets() {
        return duplicateChangeSets;
    }
    
    public List<SetupException> getSetupExceptions() {
        return setupExceptions;
    }

    public boolean validationPassed() {
        return invalidMD5Sums.size() == 0
                && failedPreconditions.size() == 0
                && duplicateChangeSets.size() == 0 
                && setupExceptions.size() ==0;
    }
}
