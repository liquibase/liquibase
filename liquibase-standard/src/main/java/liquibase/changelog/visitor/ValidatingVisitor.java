package liquibase.changelog.visitor;

import liquibase.ChecksumVersion;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.core.CreateIndexChange;
import liquibase.changelog.*;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.exception.*;
import liquibase.precondition.ErrorPrecondition;
import liquibase.precondition.FailedPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.util.StringUtil;

import java.util.*;

public class ValidatingVisitor implements ChangeSetVisitor {

    private final List<String> invalidMD5Sums = new ArrayList<>();
    private final List<FailedPrecondition> failedPreconditions = new ArrayList<>();
    private final List<ErrorPrecondition> errorPreconditions = new ArrayList<>();
    private final Set<ChangeSet> duplicateChangeSets = new HashSet<>();
    private final List<SetupException> setupExceptions = new ArrayList<>();
    private final List<Throwable> changeValidationExceptions = new ArrayList<>();
    private final ValidationErrors validationErrors = new ValidationErrors();
    private final Warnings warnings = new Warnings();

    private final Set<String> seenChangeSets = new HashSet<>();

    private Map<String, RanChangeSet> ranIndex;
    private Database database;

    //
    // Added for test
    //
    public ValidatingVisitor() {
    }

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
            final ValidationErrors foundErrors = preconditions.validate(database);
            if (foundErrors.hasErrors()) {
                this.validationErrors.addAll(foundErrors);
            } else {
                preconditions.check(database, changeLog, null, null);
            }
        } catch (PreconditionFailedException e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Precondition Failed: "+e.getMessage(), e);
            failedPreconditions.addAll(e.getFailedPreconditions());
        } catch (PreconditionErrorException e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Precondition Error: "+e.getMessage(), e);
            errorPreconditions.addAll(e.getErrorPreconditions());
        } finally {
            try {
                if (database.getConnection() != null) {
                    database.rollback();
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Error rolling back after precondition check", e);
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
                if (ranChangeSet.isSameAs(changeSet)) {
                    result = ranChangeSet;
                    break;
                }
            }
        }
        return result;
    }
        
    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        if (changeSet.isIgnore()) {
            Scope.getCurrentScope().getLog(ValidatingVisitor.class).info("Not validating ignored change set '" + changeSet.toString() + "'");
            return;
        }
        RanChangeSet ranChangeSet = findChangeSet(changeSet);
        boolean ran = ranChangeSet != null;
        Set<String> dbmsSet = changeSet.getDbmsSet();
        if(dbmsSet != null) {
            DatabaseList.validateDefinitions(changeSet.getDbmsSet(), validationErrors);
        }
        changeSet.setStoredCheckSum(ran?ranChangeSet.getLastCheckSum():null);
        boolean shouldValidate = !ran || changeSet.shouldRunOnChange() || changeSet.shouldAlwaysRun();

        if (!areChangeSetAttributesValid(changeSet)) {
            changeSet.setValidationFailed(true);
            shouldValidate = false;
        }

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
                            Scope.getCurrentScope().getLog(getClass()).info(
                                    "Skipping changeset " + changeSet + " due to validation error(s): " +
                                            StringUtil.join(foundErrors.getErrorMessages(), ", "));
                            changeSet.setValidationFailed(true);
                        } else {
                            if (!foundErrors.getWarningMessages().isEmpty())
                                Scope.getCurrentScope().getLog(getClass()).warning(
                                        "Changeset " + changeSet + ": " +
                                                StringUtil.join(foundErrors.getWarningMessages(), ", "));
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
                isItAnIncorrectChecksumVersionGeneratedByABuggyExtension(changeSet, ranChangeSet);
                if (!changeSet.shouldRunOnChange() && !changeSet.shouldAlwaysRun()) {
                    invalidMD5Sums.add(changeSet.toString(false) + " was: " + ranChangeSet.getLastCheckSum().toString()
                            + " but is now: " + changeSet.generateCheckSum(ChecksumVersion.enumFromChecksumVersion(ranChangeSet.getLastCheckSum().getVersion())).toString());
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

    /**
     * MongoDB's extension was incorrectly messing with CreateIndex checksum when the extension was added to the lib folder
     * but the database being used was other than mongodb. This method checks:
     * * is it a CreateIndex change?
     * * are we not using mongo?
     * * do we have mongo extension loaded?
     * If everything matches than add a warning message.
     */
    private void isItAnIncorrectChecksumVersionGeneratedByABuggyExtension(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        if (changeSet.getChanges().stream().anyMatch(CreateIndexChange.class::isInstance)
            && !database.getShortName().equals("mongodb")) {
            try {
                ChangeFactory changeFactory = Scope.getCurrentScope().getSingleton(ChangeFactory.class);
                changeFactory.setPerformSupportsDatabaseValidation(false);
                Change newChange = changeFactory.create("createIndex");
                if (newChange.getClass().getTypeName().equals("liquibase.ext.mongodb.change.CreateIndexChange")) {
                    // is it an old mongo change, and we are not using mongodb!? So it means that we
                    // can have a broken checksum in the database.
                    validationErrors.addError(String.format("Checksum value %s for CreateIndexChange probably was created by " +
                            "an old version of mongodb extension and needs to be fixed manually",
                            ranChangeSet.getLastCheckSum().toString()), changeSet);
                }
            } finally {
                Scope.getCurrentScope().getSingleton(ChangeFactory.class).setPerformSupportsDatabaseValidation(true);
            }
        }
    }

    private boolean areChangeSetAttributesValid(ChangeSet changeSet) {
        boolean authorEmpty = StringUtil.isEmpty(changeSet.getAuthor());
        boolean idEmpty = StringUtil.isEmpty(changeSet.getId());
        boolean strictCurrentValue = GlobalConfiguration.STRICT.getCurrentValue();

        boolean valid = false;
        if (authorEmpty && idEmpty) {
            validationErrors.addError("ChangeSet Id and Author are empty", changeSet);
        } else if (authorEmpty && strictCurrentValue) {
            validationErrors.addError("ChangeSet Author is empty", changeSet);
        } else if (idEmpty) {
            validationErrors.addError("ChangeSet Id is empty", changeSet);
        } else {
            valid = true;
        }
        return valid;
    }

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
