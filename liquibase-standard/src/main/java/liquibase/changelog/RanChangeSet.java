package liquibase.changelog;

import liquibase.ChecksumVersion;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.Paths;
import java.util.Date;

/**
 * Encapsulates information about a previously-ran changeset.  Used to build rollback statements.
 */
@NoArgsConstructor
@Getter
@Setter
public class RanChangeSet {
    private String changeLog;
    private String storedChangeLog;
    private String id;
    private String author;
    private CheckSum lastCheckSum;
    private Date dateExecuted;
    private String tag;
    private ChangeSet.ExecType execType;
    private String description;
    private String comments;
    private Integer orderExecuted;
    private ContextExpression contextExpression;
    private Labels labels;
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

    public Date getDateExecuted() {
        if (dateExecuted == null) {
            return null;
        }
        return (Date) dateExecuted.clone();
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
        return DatabaseChangeLog.normalizePath(getChangeLog()) + "::" + getId() + "::" + getAuthor();
    }

    public boolean isSameAs(ChangeSet changeSet) {
        return this.getId().equalsIgnoreCase(changeSet.getId())
                && this.getAuthor().equalsIgnoreCase(changeSet.getAuthor())
                && isSamePath(changeSet.getFilePath());

    }

    /**
     * Liquibase path handling has changed over time leading to valid paths not being
     * accepted thus breaking changesets. This method aims to check for all possible path variations
     * that Liquibase used over time.
     *
     * @param filePath the file path
     * @return does it somehow match what we have at database?
     */
    private boolean isSamePath(String filePath) {
        String normalizedFilePath = DatabaseChangeLog.normalizePath(this.getChangeLog());
        return normalizedFilePath.equalsIgnoreCase(DatabaseChangeLog.normalizePath(filePath));
    }
}
