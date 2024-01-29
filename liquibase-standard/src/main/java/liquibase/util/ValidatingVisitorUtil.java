package liquibase.util;

import liquibase.ChecksumVersion;
import liquibase.Scope;
import liquibase.change.AbstractSQLChange;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.DatabaseChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.DropIndexChange;
import liquibase.change.core.SQLFileChange;
import liquibase.changelog.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Util class to offload methods that are used by {@link liquibase.changelog.visitor.ValidatingVisitor} class
 * and may make it more complex than it should be
 */
public class ValidatingVisitorUtil {

    private ValidatingVisitorUtil() {}

    public static boolean isChecksumIssue(ChangeSet changeSet, RanChangeSet ranChangeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        return ValidatingVisitorUtil.validateMongoDbExtensionIssue(changeSet, ranChangeSet, databaseChangeLog, database) ||
               ValidatingVisitorUtil.validateAbstractSqlChangeV8ChecksumVariant(changeSet, ranChangeSet) ||
               ValidatingVisitorUtil.validateCreateFunctionChangeV8ChecksumVariant(changeSet, ranChangeSet) ||
               ValidatingVisitorUtil.validateSqlFileChangeAndExpandExpressions(changeSet, ranChangeSet, database);
    }


    /**
     * AbstractSqlChange checksum had the checksum calculated value changed for Liquibase versions 4.19.1 to 4.23.1
     * due to some changes on the way that we call it when using runWith="anything".
     * This method validates the v8 checksum using the alternative algorithm as a way to allow users to upgrade to
     * checksums v9 without facing any errors or unexpected behaviours. To accomplish that it will check for:
     * * do we have runWith set?
     * * are we working with a v8 checksum?
     * * does this change extends from AbstractSQLChange?
     * * Changing splitStaments makes it work?
     */
    private static boolean validateAbstractSqlChangeV8ChecksumVariant(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        if (StringUtil.isNotEmpty(changeSet.getRunWith()) &&
            ChecksumVersion.V8.lowerOrEqualThan(Scope.getCurrentScope().getChecksumVersion())) {

            List<AbstractSQLChange> changes = changeSet.getChanges().stream()
                    .filter(AbstractSQLChange.class::isInstance).map(c -> (AbstractSQLChange) c)
                    .collect(Collectors.toList());
            if (!changes.isEmpty()) {
                revertIgnoreOriginalSplitStatementsFlag(changeSet, changes);
                boolean valid = changeSet.isCheckSumValid(ranChangeSet.getLastCheckSum());
                if (!valid) { // whops, something really changed. Revert what we just did.
                    revertIgnoreOriginalSplitStatementsFlag(changeSet, changes);
                }
                return valid;
            }
        }
        return false;
    }

    /**
     * CreateFunctionChange checksum had the calculated value changed for Liquibase versions 4.21.X
     * due to incorrect annotations being added to CreateProcedureChange .
     * This method validates the v8 checksum using the alternative algorithm as a way to allow users to upgrade to
     * checksums v9 without facing any errors or unexpected behaviours.
     */
    private static boolean validateCreateFunctionChangeV8ChecksumVariant(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        if (ChecksumVersion.V8.lowerOrEqualThan(Scope.getCurrentScope().getChecksumVersion())) {
            List<Change> changes = changeSet.getChanges().stream()
                    .filter(c -> c.getClass().getTypeName().equals("com.datical.liquibase.ext.storedlogic.function.change.CreateFunctionChange"))
                    .collect(Collectors.toList());
            if (!changes.isEmpty() && checkLiquibaseVersionIs(ranChangeSet.getLiquibaseVersion(), 4, 21)) {
                // packageText was not used when calculating a 4.21.x checksum as it was before
                setUp421xChecksumFlagForCreateFunctionChange(changes, true);
                changeSet.clearCheckSum();
                boolean valid = changeSet.isCheckSumValid(ranChangeSet.getLastCheckSum());
                if (!valid) {
                    setUp421xChecksumFlagForCreateFunctionChange(changes, false);
                }
                return valid;
            }
        }
        return false;
    }

    private static boolean checkLiquibaseVersionIs(String version, int major, int minor) {
        String[] liquibaseVersion = version.split("\\.");
        try {
            return (liquibaseVersion.length == 3 && Integer.parseInt(liquibaseVersion[0]) == major && Integer.parseInt(liquibaseVersion[1]) == minor);
        } catch (NumberFormatException ne) { //we don't have numbers were we expected them to be
            return false;
        }
    }

    private static boolean checkLiquibaseVersionMinorThan(String version, int major, int minor) {
        if (StringUtil.isEmpty(version)) {
            return false;
        }
        String[] liquibaseVersion = version.split("\\.");
        try {
            return (liquibaseVersion.length == 3 && Integer.parseInt(liquibaseVersion[0]) == major &&
                    Integer.parseInt(liquibaseVersion[1]) < minor);
        } catch (NumberFormatException ne) { //we don't have numbers were we expected them to be
            return false;
        }
    }

    /**
     * AS we don't have core here, we use reflection to call this method that changes the checksum behavior for this specific class.
     */
    private static void setUp421xChecksumFlagForCreateFunctionChange(List<Change> changes, boolean set) {
        changes.forEach(change -> {
            try {
                change.getClass().getMethod("setUseChecksumV8ForLiquibase421x", boolean.class).invoke(change, set);
            } catch (IllegalAccessException | InvocationTargetException  | NoSuchMethodException e) {
                Scope.getCurrentScope().getLog(ValidatingVisitorUtil.class).severe("Commercial jar version doesn't provide " +
                        "method setUseChecksumV8ForLiquibase421x method for CreateFunctionChange. " +
                        "Make sure that you are using a commercial jar version compatible with this core version.", e);
            }
        });
    }

    /**
     * This method reverses flag ignoreOriginalSplitStatements on the AbstractSQLChange list and clears the changeset calculated checksum
     *  so it is recalculated when it's used again
     */

    private static void revertIgnoreOriginalSplitStatementsFlag(ChangeSet changeSet, List<AbstractSQLChange> changes) {
        changes.forEach(change -> change.setIgnoreOriginalSplitStatements(!BooleanUtil.isTrue(change.isIgnoreOriginalSplitStatements())));
        changeSet.clearCheckSum();
    }

    /**
     * MongoDB's extension was incorrectly messing with CreateIndex and DropIndex checksums when the extension was added to the lib folder
     * but a database other than mongodb was used. This method checks:
     * * is it a CreateIndex or DropIndex change?
     * * are we not using mongo?
     * * do we have mongo extension loaded?
     * * If I use CreateIndex or DropIndex from mongo extension, does the checksum matches?
     * If everything matches than we fix the checksum on the database and say it's fine to continue.
     */
    private static boolean validateMongoDbExtensionIssue(ChangeSet changeSet, RanChangeSet ranChangeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        Optional<Change> change = changeSet.getChanges().stream()
                .filter(c -> c instanceof CreateIndexChange || c instanceof DropIndexChange).findFirst();
        if (change.isPresent() && !database.getShortName().equals("mongodb")) {
            try {
                ChangeFactory changeFactory = Scope.getCurrentScope().getSingleton(ChangeFactory.class);
                changeFactory.setPerformSupportsDatabaseValidation(false);
                DatabaseChange databaseChange = change.get().getClass().getAnnotation(DatabaseChange.class);
                Change newChange = changeFactory.create(databaseChange.name());
                // do we have a mongodb change with the same name present?
                if (newChange.getClass().getTypeName().equalsIgnoreCase("liquibase.ext.mongodb.change." + databaseChange.name() + "Change")) {
                    ChangeSet newChangeset = generateNewChangeSet(databaseChangeLog, change.get(), newChange, changeSet);
                    if (newChangeset.isCheckSumValid(ranChangeSet.getLastCheckSum())) {
                        // now it matches, so it means that we are have a broken checksum in the database.
                        // Let's fix it and move ahead
                        ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
                        changeLogService.replaceChecksum(changeSet);
                        return true;
                    } else {
                        changeSet.clearCheckSum();
                    }
                }
            } catch (DatabaseException e) {
                throw new UnexpectedLiquibaseException(e);
            } finally {
                Scope.getCurrentScope().getSingleton(ChangeFactory.class).setPerformSupportsDatabaseValidation(true);
            }
        }
        return false;
    }

    private static ChangeSet generateNewChangeSet(DatabaseChangeLog databaseChangeLog, Change originalChange, Change newChange, ChangeSet changeSet) {
        ChangeSet newChangeset = new ChangeSet(changeSet.getId(), changeSet.getAuthor(), changeSet.shouldAlwaysRun(),
                changeSet.isRunOnChange(), changeSet.getFilePath(), null, null,
                databaseChangeLog);
        for (Change c : changeSet.getChanges()) {
            if (!(originalChange.getClass().isInstance(c))) {
                newChangeset.addChange(c);
            } else {
                newChangeset.addChange(newChange);
            }
        }
        return  newChangeset;
    }

    /**
     * returns true iff the changeset's checksum was incorrect due to SQLFileChange doing
     * property substitution on the contents of an external SQL file. If it returns true,
     * the changeset's checksum has been fixed.
     *
     * @param changeSet
     * @param ranChangeSet
     * @param database
     * @return
     */
    private static boolean validateSqlFileChangeAndExpandExpressions(ChangeSet changeSet, RanChangeSet ranChangeSet, Database database) {
        List<SQLFileChange> changes = changeSet.getChanges().stream()
                                               .filter(SQLFileChange.class::isInstance)
                                               .map(c -> (SQLFileChange) c)
                                               .collect(Collectors.toList());
        if (changes.isEmpty() || !checkLiquibaseVersionMinorThan(ranChangeSet.getLiquibaseVersion(), 4, 26)) {
            return false;
        } else {
            try {
                /*
                 * There could be more than one SQLFileChange change in the changeset,
                 * so call setDoExpandExpressionsInGenerateChecksum(true) on all of them
                 * before generating the checksum.
                 */
                for (SQLFileChange change : changes) {
                    change.setDoExpandExpressionsInGenerateChecksum(true);
                }
                changeSet.clearCheckSum();
                /*
                 * If the changeset's checksum with SQLFileChange.doExpandExpressionsInGenerateChecksum=true
                 * matches ranChangeSet's checksum, then ranChangeSet's checksum was calculated incorrectly
                 * (i.e., with expressions expanded). In that case, calculate the changeset's checksum correctly
                 * (with expressions not expanded) and update the database.
                 */
                boolean valid = changeSet.isCheckSumValid(ranChangeSet.getLastCheckSum());
                for (SQLFileChange change : changes) {
                    change.setDoExpandExpressionsInGenerateChecksum(false);
                }
                changeSet.clearCheckSum();
                if (valid) {
                    ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
                    changeLogService.replaceChecksum(changeSet);
                }
                return valid;
            } catch (DatabaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
    }
}
