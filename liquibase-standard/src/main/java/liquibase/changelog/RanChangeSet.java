package liquibase.changelog;

import liquibase.ChecksumVersion;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.change.core.TagDatabaseChange;
import liquibase.util.LiquibaseUtil;
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
     * see {@link #equals(Object)}). To record that a changeset has been run, use
     * {@link #RanChangeSet(ChangeSet, ChangeSet.ExecType, Integer)} instead.
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
     * <p>
     * To record that a changeset has been run with the same information as the standard {@code DATABASECHANGELOG}
     * table, use {@link #RanChangeSet(ChangeSet, ChangeSet.ExecType, Integer)} instead.
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
     * Creates an entry for the given changeset which mirrors what
     * {@link liquibase.sqlgenerator.core.MarkChangeSetRanGenerator} persists in the {@code DATABASECHANGELOG} table when
     * the changeset is marked as ran. Compared to {@link #RanChangeSet(ChangeSet, ChangeSet.ExecType, ContextExpression, Labels)},
     * this:
     * <ul>
     *     <li>always generates the checksum with {@link ChecksumVersion#latest()} (like
     *     {@link liquibase.sqlgenerator.core.MarkChangeSetRanGenerator} does), ignoring the version of any stored
     *     checksum</li>
     *     <li>takes the contexts from {@link ChangeSet#buildFullContext()} and the labels from
     *     {@link ChangeSet#buildFullLabels()}, i.e. including the ones inherited from the parent changelogs and
     *     include/includeAll statements</li>
     *     <li>sets the tag of a {@link TagDatabaseChange} contained in the changeset, if any</li>
     *     <li>sets the deployment id of the current {@link Scope}</li>
     *     <li>sets the Liquibase version as returned by {@link LiquibaseUtil#getBuildVersionForChangeLogHistory()}</li>
     *     <li>sets the given {@code orderExecuted}</li>
     * </ul>
     * <p>
     * This is the constructor to use when recording that a changeset has been run. It is intended for
     * {@link ChangeLogHistoryService} implementations which persist {@link RanChangeSet} entries themselves
     * (e.g. for databases which do not use SQL) and want to store the same information as the standard
     * {@code DATABASECHANGELOG} table.
     *
     * @param changeSet      the changeset which has been run
     * @param execType       how the changeset has been run
     * @param orderExecuted  the execution order of the changeset, usually obtained from
     *                       {@link ChangeLogHistoryService#getNextSequenceValue()}; may be {@code null} if unknown
     */
    public RanChangeSet(ChangeSet changeSet, ChangeSet.ExecType execType, Integer orderExecuted) {
        this(changeSet, execType, orderExecuted,
                new ContextExpression(changeSet.buildFullContext()), new Labels(changeSet.buildFullLabels()));
    }

    /**
     * Implementation of {@link #RanChangeSet(ChangeSet, ChangeSet.ExecType, Integer)}. The full contexts and labels
     * are passed in by the caller so that they are only computed once.
     *
     * @param changeSet     the changeset which has been run
     * @param execType      how the changeset has been run
     * @param orderExecuted the execution order of the changeset; may be {@code null} if unknown
     * @param contexts      the full contexts of the changeset
     * @param labels        the full labels of the changeset
     */
    private RanChangeSet(ChangeSet changeSet, ChangeSet.ExecType execType, Integer orderExecuted,
                          ContextExpression contexts, Labels labels) {
        this(changeSet.getFilePath(),
                changeSet.getId(),
                changeSet.getAuthor(),
                changeSet.generateCheckSum(ChecksumVersion.latest()),
                new Date(),
                getTagFromChangeSet(changeSet),
                execType,
                changeSet.getDescription(),
                changeSet.getComments(),
                contexts,
                labels,
                Scope.getCurrentScope().getDeploymentId(),
                changeSet.getStoredFilePath());
        this.liquibaseVersion = LiquibaseUtil.getBuildVersionForChangeLogHistory();
        this.orderExecuted = orderExecuted;
    }

    private static String getTagFromChangeSet(ChangeSet changeSet) {
        for (Change change : changeSet.getChanges()) {
            if (change instanceof TagDatabaseChange) {
                return ((TagDatabaseChange) change).getTag();
            }
        }
        return null;
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
     * <p>
     * To create an entry for a {@link ChangeSet} which has just been run, use
     * {@link #RanChangeSet(ChangeSet, ChangeSet.ExecType, Integer)} instead.
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
