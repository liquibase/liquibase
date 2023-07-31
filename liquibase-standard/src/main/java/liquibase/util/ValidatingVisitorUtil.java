package liquibase.util;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.DatabaseChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.DropIndexChange;
import liquibase.changelog.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.Optional;

/**
 * Util class to offload methods that are used by {@link liquibase.changelog.visitor.ValidatingVisitor} class
 * and may make it more complex than it should be
 */
public class ValidatingVisitorUtil {

    private ValidatingVisitorUtil() {}

    /**
     * MongoDB's extension was incorrectly messing with CreateIndex and DropIndex checksums when the extension was added to the lib folder
     * but a database other than mongodb was used. This method checks:
     * * is it a CreateIndex or DropIndex change?
     * * are we not using mongo?
     * * do we have mongo extension loaded?
     * * If I use CreateIndex or DropIndex from mongo extension, does the checksum matches?
     * If everything matches than we fix the checksum on the database and say it's fine to continue.
     */
    public static boolean validateMongoDbExtensionIssue(ChangeSet changeSet, RanChangeSet ranChangeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        Optional<Change> change = changeSet.getChanges().stream()
                .filter(c -> c instanceof CreateIndexChange || c instanceof DropIndexChange).findFirst();
        if (change.isPresent() && !database.getShortName().equals("mongodb")) {
            try {
                ChangeFactory changeFactory = Scope.getCurrentScope().getSingleton(ChangeFactory.class);
                changeFactory.setPerformSupportsDatabaseValidation(false);
                DatabaseChange databaseChange = change.get().getClass().getAnnotation(DatabaseChange.class);
                Change newChange = changeFactory.create(databaseChange.name());
                // do we have a mongodb change with the same name present?
                if (newChange.getClass().getTypeName().equals("liquibase.ext.mongodb.change" + databaseChange.name())) {
                    ChangeSet newChangeset = generateNewChangeSet(databaseChangeLog, newChange, changeSet);
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

    private static ChangeSet generateNewChangeSet(DatabaseChangeLog databaseChangeLog, Change newChange, ChangeSet changeSet) {
        ChangeSet newChangeset = new ChangeSet(databaseChangeLog);
        for (Change c : changeSet.getChanges()) {
            if (!(c instanceof CreateIndexChange)) {
                newChangeset.addChange(c);
            } else {
                newChangeset.addChange(newChange);
            }
        }
        return  newChangeset;
    }
}
