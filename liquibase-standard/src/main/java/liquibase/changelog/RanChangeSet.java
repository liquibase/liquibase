package liquibase.changelog;

import liquibase.ChecksumVersion;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;

import java.util.Date;

/**
 * Encapsulates information about a previously-ran changeset.  Used to build rollback statements.
 */
public class RanChangeSet {
    private final String changeLog;
    private final String storedChangeLog;
    private final String id;
    private final String author;
    private final CheckSum lastCheckSum;
    private final Date dateExecuted;
    private String tag;
    private final ChangeSet.ExecType execType;
    private String description;
    private String comments;
    private Integer orderExecuted;
    private final ContextExpression contextExpression;
    private final Labels labels;
    private String deploymentId;
    private String liquibaseVersion;


    public RanChangeSet(ChangeSet changeSet) {
        this(changeSet, null, null, null);
    }

    public RanChangeSet(ChangeSet changeSet, ChangeSet.ExecType execType, ContextExpression contexts, Labels labels) {
        this(changeSet.getFilePath(),
                changeSet.getId(),
                changeSet.getAuthor(),
                changeSet.generateCheckSum((changeSet.getStoredCheckSum() != null) ?
                        ChecksumVersion.enumFromChecksumVersion(changeSet.getStoredCheckSum().getVersion()) : ChecksumVersion.latest()),
                new Date(),
                null,
                execType,
                changeSet.getDescription(),
                changeSet.getComments(),
                contexts,
                labels,
                null,
                changeSet.getStoredFilePath());
    }

    public RanChangeSet(String changeLog, String id, String author, CheckSum lastCheckSum, Date dateExecuted, String tag, ChangeSet.ExecType execType, String description, String comments, ContextExpression contextExpression, Labels labels, String deploymentId) {
        this(changeLog, id, author, lastCheckSum, dateExecuted, tag, execType, description, comments, contextExpression, labels, deploymentId, null);
    }

    public RanChangeSet(String changeLog, String id, String author, CheckSum lastCheckSum, Date dateExecuted, String tag, ChangeSet.ExecType execType, String description, String comments, ContextExpression contextExpression, Labels labels, String deploymentId, String storedChangeLog) {
        this.changeLog = changeLog;
        this.storedChangeLog = storedChangeLog;
        this.id = id;
        this.author = author;
        this.lastCheckSum = lastCheckSum;
        if (dateExecuted == null) {
            this.dateExecuted = null;
        } else {
            this.dateExecuted = new Date(dateExecuted.getTime());
        }
        this.tag = tag;
        this.execType = execType;
        this.description = description;
        this.comments = comments;
        this.contextExpression = contextExpression;
        this.labels = labels;
        this.deploymentId = deploymentId;
    }

    public String getChangeLog() {
        return changeLog;
    }

    /**
     * Get the path stored in the DatabaseChangeLog table
     */
    public String getStoredChangeLog() {
        return storedChangeLog;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public CheckSum getLastCheckSum() {
        return lastCheckSum;
    }

    public Date getDateExecuted() {
        if (dateExecuted == null) {
            return null;
        }
        return (Date) dateExecuted.clone();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ChangeSet.ExecType getExecType() {
        return execType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public ContextExpression getContextExpression() {
        return contextExpression;
    }

    public Labels getLabels() {
        return labels;
    }

    public Integer getOrderExecuted() {
        return orderExecuted;
    }

    public void setOrderExecuted(Integer orderExecuted) {
        this.orderExecuted = orderExecuted;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getLiquibaseVersion() {
        return liquibaseVersion;
    }

    public void setLiquibaseVersion(String liquibaseVersion) {
        this.liquibaseVersion = liquibaseVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final RanChangeSet that = (RanChangeSet) o;

        return author.equals(that.author) && changeLog.equals(that.changeLog) && id.equals(that.id);

    }

    @Override
    public int hashCode() {
        int result;
        result = changeLog.hashCode();
        result = (29 * result) + id.hashCode();
        result = (29 * result) + author.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getChangeLog() + "::" + getId() + "::" + getAuthor();
    }

    public boolean isSameAs(ChangeSet changeSet) {
        return DatabaseChangeLog.normalizePath(this.getChangeLog()).equalsIgnoreCase(DatabaseChangeLog.normalizePath(changeSet.getFilePath()))
                && this.getId().equalsIgnoreCase(changeSet.getId())
                && this.getAuthor().equalsIgnoreCase(changeSet.getAuthor());
    }
}
