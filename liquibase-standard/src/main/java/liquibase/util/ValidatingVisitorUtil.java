package liquibase.util;

import liquibase.ChecksumVersion;
import liquibase.Scope;
import liquibase.change.AbstractSQLChange;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.DatabaseChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.DropIndexChange;
import liquibase.changelog.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;

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
               ValidatingVisitorUtil.validateAbstractSqlChangeV8ChecksumVariant(changeSet, ranChangeSet, databaseChangeLog, database);
    }


    /**
     * AbstractSqlChange checksum had the default calculated value changed from Liquibase versions 4.19.1 to 4.23.1
     * when using runWith="anything". This method validatesthe v8 checksum using the alternative algorithm as a way
     * to allow users to upgrade to checksums v9 without facing any errors or unexpected behaviours. To accomplish
     * that it will check for:
     * * do we have runWith set?
     * * are we working with a v8 checksum?
     * * does this change extends from AbstractSQLChange?
     * * Changing splitStaments makes it work?
     */
    private static boolean validateAbstractSqlChangeV8ChecksumVariant(ChangeSet changeSet, RanChangeSet ranChangeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        if (StringUtil.isNotEmpty(changeSet.getRunWith()) &&
            ChecksumVersion.V8.lowerOrEqualThan(Scope.getCurrentScope().getChecksumVersion())) {

            List<Change> changes = changeSet.getChanges().stream()
                    .filter(AbstractSQLChange.class::isInstance).collect(Collectors.toList());
            if (!changes.isEmpty()) {
                clearChecksumAndRevertOriginalSpliStatements(changeSet, changes);
                boolean valid = changeSet.isCheckSumValid(ranChangeSet.getLastCheckSum());
                if (!valid) { // whops, something really changed. Revert what we just did.
                    clearChecksumAndRevertOriginalSpliStatements(changeSet, changes);
                }
                return valid;
            }
        }
        return false;
    }

    private static void clearChecksumAndRevertOriginalSpliStatements(ChangeSet changeSet, List<Change> changes) {
        changes.forEach(change -> {
            AbstractSQLChange abstractSQLChange = (AbstractSQLChange) change;
            abstractSQLChange.setOriginalSplitStatements(!abstractSQLChange.isOriginalSplitStatements());
        });
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
}
