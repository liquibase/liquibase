package liquibase.changelog;

import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.logging.LogFactory;
import liquibase.precondition.Conditional;
import liquibase.precondition.core.PreconditionContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the information stored in the change log XML file.
 */
public class DatabaseChangeLog implements Comparable<DatabaseChangeLog>, Conditional {
    private PreconditionContainer preconditionContainer = new PreconditionContainer();
    private String physicalFilePath;
    private String logicalFilePath;

    private boolean skipValidation;

    private List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
    private ChangeLogParameters changeLogParameters;

    public DatabaseChangeLog() {
    }

    public DatabaseChangeLog(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    public PreconditionContainer getPreconditions() {
        return preconditionContainer;
    }

    public void setPreconditions(PreconditionContainer precond) {
        preconditionContainer = precond;
    }


    public ChangeLogParameters getChangeLogParameters() {
        return changeLogParameters;
    }

    public void setChangeLogParameters(ChangeLogParameters changeLogParameters) {
        this.changeLogParameters = changeLogParameters;
    }

    public String getPhysicalFilePath() {
        return physicalFilePath;
    }

    public void setPhysicalFilePath(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    public String getLogicalFilePath() {
        String returnPath = logicalFilePath;
        if (logicalFilePath == null) {
            returnPath = physicalFilePath;
        }
        return returnPath.replaceAll("\\\\","/");
    }

    public void setLogicalFilePath(String logicalFilePath) {
        this.logicalFilePath = logicalFilePath;
    }

    public String getFilePath() {
        if (logicalFilePath == null) {
            return physicalFilePath;
        } else {
            return logicalFilePath;
        }
    }

    @Override
    public String toString() {
        return getFilePath();
    }

    /**
     * @return Whether validation of a changeset should occur
     */
    public boolean isSkipValidation() {
        return skipValidation;
    }

    public void setSkipValidation(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    public int compareTo(DatabaseChangeLog o) {
        return getFilePath().compareTo(o.getFilePath());
    }


    public ChangeSet getChangeSet(String path, String author, String id) {
        for (ChangeSet changeSet : changeSets) {
            if (changeSet.getFilePath().equalsIgnoreCase(path)
                    && changeSet.getAuthor().equalsIgnoreCase(author)
                    && changeSet.getId().equalsIgnoreCase(id)
                    && (null == changeSet.getDbmsSet()
                    || changeSet.getDbmsSet().isEmpty()
                    || changeSet.getDbmsSet().contains(changeLogParameters.getValue("database.typeName").toString()))) {
                return changeSet;
            }
        }

        return null;
    }

    public List<ChangeSet> getChangeSets() {
        return changeSets;
    }

    public void addChangeSet(ChangeSet changeSet) {
        this.changeSets.add(changeSet);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseChangeLog that = (DatabaseChangeLog) o;

        return getFilePath().equals(that.getFilePath());

    }

    @Override
    public int hashCode() {
        return getFilePath().hashCode();
    }

    public void validate(Database database, String... contexts) throws LiquibaseException {

        /**
         * Skip validation of this change log if skipValidation has been set to true.
         * Intended for legacy change sets, improvements can be made without affecting users who have already run the old
         * change sets.
         */
        if (skipValidation) {
            return;
        }

        ChangeLogIterator logIterator = new ChangeLogIterator(this, new DbmsChangeSetFilter(database), new ContextChangeSetFilter(contexts));

        ValidatingVisitor validatingVisitor = new ValidatingVisitor(database.getRanChangeSetList());
        validatingVisitor.validate(database, this);
        logIterator.run(validatingVisitor, database);

        for (String message : validatingVisitor.getWarnings().getMessages()) {
            LogFactory.getLogger().warning(message);
        }
        
        if (!validatingVisitor.validationPassed()) {
            throw new ValidationFailedException(validatingVisitor);
        }
    }

    public ChangeSet getChangeSet(RanChangeSet ranChangeSet) {
        return getChangeSet(ranChangeSet.getChangeLog(), ranChangeSet.getAuthor(), ranChangeSet.getId());
    }
}
