package liquibase.changeset;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ModifyChangeSets;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.plugin.Plugin;

/**
 *
 * The ChangSetService allows for creation and modification of ChangeSets to be pluggable
 * The implemented createChangeSet methods support calls from the Liquibase Core to create
 * Change Sets.  Not all ChangeSet constructors are supported at this point.  Those
 * constructors will need to be called directly.
 *
 */
public interface ChangeSetService extends Plugin {
    /**
     *
     * Return the plugin priority
     * @return  int
     *
     */
    int getPriority();

    /**
     *
     * Create a change set with the indicated arguments
     *
     * @param  id
     * @param  author
     * @param  alwaysRun
     * @param  runOnChange
     * @param  filePath
     * @param  contextFilter
     * @param  dbmsList
     * @param  runWith
     * @param  runWithSpoolFile
     * @param  runInTransaction
     * @param  quotingStrategy
     * @param  databaseChangeLog
     * @return ChangeSet
     *
     */
    ChangeSet createChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange,
                              String filePath, String contextFilter, String dbmsList,
                              String runWith, String runWithSpoolFile, boolean runInTransaction,
                              ObjectQuotingStrategy quotingStrategy, DatabaseChangeLog databaseChangeLog);

    /**
     *
     * Create a change set with the indicated arguments
     *
     * @param  id
     * @param  author
     * @param  alwaysRun
     * @param  runOnChange
     * @param  filePath
     * @param  contextFilter
     * @param  dbmsList
     * @param  databaseChangeLog
     * @return ChangeSet
     *
     */
    ChangeSet createChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange,
                              String filePath, String contextFilter, String dbmsList,
                              DatabaseChangeLog databaseChangeLog);
    /**
     *
     * Create a change set with the changelog
     *
     * @param  databaseChangeLog                Construct this change set with the DatabaseChangeLog
     * @return ChangeSet
     *
     */
    ChangeSet createChangeSet(DatabaseChangeLog databaseChangeLog);

    /**
     *
     * Create the ModifyChangeSets instance which will do the modifications
     *
     * @param  node                       The ParsedNode that was created during load
     * @return ModifyChangeSets           The object which will perform the modifications
     * @throws ParsedNodeException        Thrown if unable to access values from the node
     *
     */
    ModifyChangeSets createModifyChangeSets(ParsedNode node) throws ParsedNodeException;

    /**
     *
     * Create the ModifyChangeSets instance which will do the modifications
     *
     * @param  runWith                    The runWith value
     * @param  runWithSpool               The runWithSpool value
     * @return ModifyChangeSets           The object which will perform the modifications
     *
     */
    default ModifyChangeSets createModifyChangeSets(String runWith, String runWithSpool) {
        return new ModifyChangeSets(runWith, runWithSpool);
    }

    /**
     *
     * Given a change set and a ModifyChangeSets instance, perform the modifications
     *
     * @param changeSet                  The change set to modify
     * @param modifyChangeSets           The modifier
     *
     */
    void modifyChangeSets(ChangeSet changeSet, ModifyChangeSets modifyChangeSets);

    /**
     *
     * Check for an override for a change-level delimiter
     *
     * @param  endDelimiter   The endDelimiter to override
     * @return String         The override setting
     *
     */
    default String getOverrideDelimiter(String endDelimiter) {
        return endDelimiter;
    }

    /**
     *
     * Default implementation returns null
     *
     * @param   changeSet            Unused
     * @return  null
     *
     */
    default String getEndDelimiter(ChangeSet changeSet) {
        return null;
    }

}
