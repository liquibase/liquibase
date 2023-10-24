package liquibase.changeset;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ModifyChangeSets;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.plugin.Plugin;

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
     * @param  databaseChangeLog
     * @return ChangeSet
     *
     */
    ChangeSet createChangeSet(DatabaseChangeLog changeLog);

    /**
     *
     * Create the ModifyChangeSets instance which will do the modifications
     *
     * @param  node                       The ParsedNode that was created during load
     * @return ModifyChangeSets           The object which will perform the modifications
     * @throws ParsedNodeException
     *
     */
    ModifyChangeSets createModifyChangeSets(ParsedNode node) throws ParsedNodeException;

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
     * Default implementation returns null
     *
     * @param   changeSet
     * @return  null
     *
     */
    default String getEndDelimiter(ChangeSet changeSet) {
        return null;
    }
}
