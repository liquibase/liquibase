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

    /**
     * Creates a minimal entry for the given changeset, without exec type, contexts and labels.
     * <p>
     * This is mainly useful to identify a changeset (e.g. for lookups or removals in a list of {@link RanChangeSet}s,
     * see {@link #equals(Object)}).
     *
     * @param changeSet the changeset to create the entry for
     * @see #RanChangeSet(ChangeSet, ChangeSet.ExecType, ContextExpression, Labels)
     */
    public RanChangeSet(ChangeSet changeSet) {
        this(changeSet, null, null, null);
    }

    /**
     * Creates an entry for the given changeset with the given exec type, contexts and labels.
     * <p>
     * The checksum is generated with the checksum version of the changeset's stored checksum if there is one,
     * otherwise with {@link ChecksumVersion#latest()}. The execution date is set to now. The tag, deployment id,
     * Liquibase version and execution order are <em>not</em> set.
     *
     * @param changeSet the changeset to create the entry for
     * @param execType  how the changeset has been run; may be {@code null}
     * @param contexts  the contexts to store; may be {@code null}
     * @param labels    the labels to store; may be {@code null}
     */
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

    /**
     * Same as {@link #RanChangeSet(String, String, String, CheckSum, Date, String, ChangeSet.ExecType, String, String, ContextExpression, Labels, String, String)}
     * without a stored changelog path.
     */
    public RanChangeSet(String changeLog, String id, String author, CheckSum lastCheckSum, Date dateExecuted, String tag, ChangeSet.ExecType execType, String description, String comments, ContextExpression contextExpression, Labels labels, String deploymentId) {
        this(changeLog, id, author, lastCheckSum, dateExecuted, tag, execType, description, comments, contextExpression, labels, deploymentId, null);
    }

    /**
     * Creates an entry from the given raw values, e.g. when reading a row of the {@code DATABASECHANGELOG} table.
     * All values are stored as given; nothing is derived from a {@link ChangeSet}. The Liquibase version and
     * execution order are not set and can be set with the corresponding setters.
     *
     * @param changeLog         the path of the changelog file
     * @param id                the changeset id
     * @param author            the changeset author
     * @param lastCheckSum      the stored checksum
     * @param dateExecuted      when the changeset was run; may be {@code null}
     * @param tag               the tag; may be {@code null}
     * @param execType          how the changeset was run
     * @param description       the changeset description
     * @param comments          the changeset comments
     * @param contextExpression the stored contexts
     * @param labels            the stored labels
     * @param deploymentId      the id of the deployment which ran the changeset
     * @param storedChangeLog   the changelog path as stored, if it differs from {@code changeLog}; may be {@code null}
     */
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

    public String getStoredChangeLog() {
        if (storedChangeLog == null) {
            return changeLog;
        }
        return storedChangeLog;
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
