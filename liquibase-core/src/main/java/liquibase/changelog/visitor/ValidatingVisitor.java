package liquibase.changelog.visitor;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.precondition.ErrorPrecondition;
import liquibase.precondition.FailedPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.util.StringUtils;

import java.util.*;

public class ValidatingVisitor implements ChangeSetVisitor {

    private List<String> invalidMD5Sums = new ArrayList<>();
    private List<FailedPrecondition> failedPreconditions = new ArrayList<>();
    private List<ErrorPrecondition> errorPreconditions = new ArrayList<>();
    private Set<ChangeSet> duplicateChangeSets = new HashSet<>();
    private List<SetupException> setupExceptions = new ArrayList<>();
    private List<Throwable> changeValidationExceptions = new ArrayList<>();
    private ValidationErrors validationErrors = new ValidationErrors();
    private Warnings warnings = new Warnings();

    private Set<String> seenChangeSets = new HashSet<>();

    private Map<String, RanChangeSet> ranIndex;
    private Database database;

    public ValidatingVisitor(List<RanChangeSet> ranChangeSets) {
        ranIndex = new HashMap<>();
        for(RanChangeSet changeSet:ranChangeSets) {
            ranIndex.put(changeSet.toString(), changeSet);
        }
    }

    public void validate(Database database, DatabaseChangeLog changeLog) {
        this.database = database;
        PreconditionContainer preconditions = changeLog.getPreconditions();
        try {
            if (preconditions == null) {
                return;
            }
            preconditions.check(database, changeLog, null, null);
        } catch (PreconditionFailedException e) {
            LogService.getLog(getClass()).debug(LogType.LOG, "Precondition Failed: "+e.getMessage(), e);
            failedPreconditions.addAll(e.getFailedPreconditions());
        } catch (PreconditionErrorException e) {
            LogService.getLog(getClass()).debug(LogType.LOG, "Precondition Error: "+e.getMessage(), e);
            errorPreconditions.addAll(e.getErrorPreconditions());
        } finally {
            try {
                if (database.getConnection() != null) {
                    database.rollback();
                }
            } catch (DatabaseException e) {
                LogService.getLog(getClass()).warning(LogType.LOG, "Error rolling back after precondition check", e);
            }
        }
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    private RanChangeSet findChangeSet(ChangeSet changeSet) {
        RanChangeSet result = ranIndex.get(changeSet.toString(false));
        if (result == null) {
            for (RanChangeSet ranChangeSet : ranIndex.values()) {
                if (ranChangeSet.getId().equalsIgnoreCase(changeSet.getId())) {
                    if (ranChangeSet.getAuthor().equalsIgnoreCase(changeSet.getAuthor())) {
                        String changeSetPath = normalizePath(changeSet.getFilePath());
                        String ranChangeSetPath = normalizePath(ranChangeSet.getChangeLog());
                        if (ranChangeSetPath.equalsIgnoreCase(changeSetPath)
                            || ranChangeSetPath.endsWith(changeSetPath) || changeSetPath.endsWith(ranChangeSetPath)) {
                            result = ranChangeSet;
                        }
                    }
                }
            }
        }
        return result;
    }
        
    private String normalizePath(String filePath) {
        return filePath.replaceFirst("^classpath:", "");
    }

        @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        RanChangeSet ranChangeSet = findChangeSet(changeSet);
        boolean ran = ranChangeSet != null;
        boolean shouldValidate = !ran || changeSet.shouldRunOnChange() || changeSet.shouldAlwaysRun();
        for (Change change : changeSet.getChanges()) {
            try {
                change.finishInitialization();
            } catch (SetupException se) {
                setupExceptions.add(se);
            }
            
            
            if(shouldValidate){
                warnings.addAll(change.warn(database));

                try {
                    ValidationErrors foundErrors = change.validate(database);
                    if ((foundErrors != null)) {
                        if (foundErrors.hasErrors() && (changeSet.getOnValidationFail().equals
                                (ChangeSet.ValidationFailOption.MARK_RAN))) {
                            LogService.getLog(getClass()).info(
                                    LogType.LOG, "Skipping change set " + changeSet + " due to validation error(s): " +
                                            StringUtils.join(foundErrors.getErrorMessages(), ", "));
                            changeSet.setValidationFailed(true);
                        } else {
                            if (!foundErrors.getWarningMessages().isEmpty())
                                LogService.getLog(getClass()).warning(
                                        LogType.LOG, "Change set " + changeSet + ": " +
                                                StringUtils.join(foundErrors.getWarningMessages(), ", "));
                            validationErrors.addAll(foundErrors, changeSet);
                        }
                    }
                } catch (Exception e) {
                    changeValidationExceptions.add(e);
                }
            }
        }

        if(ranChangeSet != null){
            if (!changeSet.isCheckSumValid(ranChangeSet.getLastCheckSum())) {
                if (!changeSet.shouldRunOnChange()) {
                    invalidMD5Sums.add(changeSet.toString(false)+" was: "+ranChangeSet.getLastCheckSum().toString()+" but is now: "+changeSet.generateCheckSum().toString());
                }
            }
        }

        // Did we already see this ChangeSet?
        String changeSetString = changeSet.toString(false);
        if (seenChangeSets.contains(changeSetString)) {
            duplicateChangeSets.add(changeSet);
            return;
        } else {
            seenChangeSets.add(changeSetString);
        }
    } // public void visit(...)

    public List<String> getInvalidMD5Sums() {
        return invalidMD5Sums;
    }


    public List<FailedPrecondition> getFailedPreconditions() {
        return failedPreconditions;
    }

    public List<ErrorPrecondition> getErrorPreconditions() {
        return errorPreconditions;
    }

    public Set<ChangeSet> getDuplicateChangeSets() {
        return duplicateChangeSets;
    }

    public List<SetupException> getSetupExceptions() {
        return setupExceptions;
    }

    public List<Throwable> getChangeValidationExceptions() {
        return changeValidationExceptions;
    }

    public ValidationErrors getValidationErrors() {
        return validationErrors;
    }

    public Warnings getWarnings() {
        return warnings;
    }

    public boolean validationPassed() {
        return invalidMD5Sums.isEmpty() && failedPreconditions.isEmpty() && errorPreconditions.isEmpty() &&
            duplicateChangeSets.isEmpty() && changeValidationExceptions.isEmpty() && setupExceptions.isEmpty() &&
            !validationErrors.hasErrors();
    }

    public Database getDatabase() {
        return database;
    }
}
