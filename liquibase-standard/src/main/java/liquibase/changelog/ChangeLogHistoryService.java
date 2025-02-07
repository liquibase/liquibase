package liquibase.changelog;

import liquibase.Beta;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.LiquibaseException;
import liquibase.plugin.Plugin;

import java.util.Date;
import java.util.List;

public interface ChangeLogHistoryService extends Plugin {
    int getPriority();

    boolean supports(Database database);

    void setDatabase(Database database);

    void reset();

    /**
     * Ensures the change log history container is correctly initialized for use. This method may be called multiple times so it should check state as needed.
     */
    void init() throws DatabaseException;

    /**
     * Updates null checksum values
     */
    void upgradeChecksums(final DatabaseChangeLog databaseChangeLog, final Contexts contexts, LabelExpression labels) throws DatabaseException;

    List<RanChangeSet> getRanChangeSets() throws DatabaseException;

    /**
     * This method was created to clear out MD5sum for upgrade purpose but after some refactoring the logic was moved to Update commands and it should have been removed
     * as everywhere it is called only with boolean false, so for core it is the same as getRanChangeSets().
     *
     * @param allowChecksumsUpgrade
     * @deprecated use {@link #getRanChangeSets()} instead
     */
    @Deprecated
    default List<RanChangeSet> getRanChangeSets(boolean allowChecksumsUpgrade) throws DatabaseException {
        return this.getRanChangeSets();
    }

    RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    /**
     * Returns the date the given changeSet was run. Returns null if changeSet was not null.
     */
    Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    void setExecType(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException;

    void removeFromHistory(ChangeSet changeSet) throws DatabaseException;

    int getNextSequenceValue() throws LiquibaseException;

    void tag(String tagString) throws DatabaseException;

    boolean tagExists(String tag) throws DatabaseException;

    void clearAllCheckSums() throws LiquibaseException;

    void destroy() throws DatabaseException;

    /**
     * @deprecated use {@link Scope#getDeploymentId()}
     */
    @Deprecated
    String getDeploymentId();

    /**
     * @deprecated This is now handled automatically by the root scope
     */
    @Deprecated
    void resetDeploymentId();

    /**
     * @deprecated This is now handled automatically by the root scope
     */
    @Deprecated
    void generateDeploymentId();

    /**
     *  This method should return true if all checksums in dbcl table have the same version as {@link liquibase.ChecksumVersion#latest().getVersion()}.
     *  This method is used by Update command family in order to know if there are old checksum versions in the database that should be updated or if it can proceed with fast checksum update process.
     *  IF your implementation does not validate dbcl table then return false.
     *
     * @return false if we have checksums different from  {@link liquibase.ChecksumVersion#latest().getVersion()} in the dbcl table.
     */
    boolean isDatabaseChecksumsCompatible();

    /**
     * By default does nothing to keep compatibility with older versions, but subclasses may like to implement
     * this method to support checksum upgrades.
     */
    default void replaceChecksum(ChangeSet changeSet) throws DatabaseException {
    }

    /**
     * By default does nothing to keep compatibility with older versions, but subclasses may like to implement
     * this method to support eventual minor file path fixes.
     *
     * @Deprecated to be removed around Liquibase 4.34.0 - DO NOT USE
     */
    @Beta
    @Deprecated
    default void replaceFilePath(ChangeSet changeSet, String oldPath) throws DatabaseException {
    }
}
