package liquibase.changelog;

import liquibase.change.CheckSum;

import java.util.Date;

/**
 * Encapsulates information about a previously-ran change set.  Used to build rollback statements. 
 */
public class RanChangeSet {
    private final String changeLog;
    private final String id;
    private final String author;
    private final CheckSum lastCheckSum;
    private final Date dateExecuted;
    private String tag;
    private ChangeSet.ExecType execType;

    public RanChangeSet(ChangeSet changeSet) {
        this(changeSet, null);
    }

    public RanChangeSet(ChangeSet changeSet, ChangeSet.ExecType execType) {
        this(changeSet.getFilePath(),
             changeSet.getId(),
             changeSet.getAuthor(),
             changeSet.generateCheckSum(),
             new Date(),
             null,
             execType);
    }

    public RanChangeSet(String changeLog, String id, String author, CheckSum lastCheckSum, Date dateExecuted, String tag, ChangeSet.ExecType execType) {
        this.changeLog = changeLog;
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
    }

    public String getChangeLog() {
        return changeLog;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RanChangeSet that = (RanChangeSet) o;

        return author.equals(that.author) && changeLog.equals(that.changeLog) && id.equals(that.id);

    }

    @Override
    public int hashCode() {
        int result;
        result = changeLog.hashCode();
        result = 29 * result + id.hashCode();
        result = 29 * result + author.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getChangeLog() + "::" + getId() + "::" + getAuthor();
    }

    public boolean isSameAs(ChangeSet changeSet) {
        return this.getChangeLog().replace('\\', '/').equalsIgnoreCase(changeSet.getFilePath().replace('\\', '/'))
                && this.getId().equalsIgnoreCase(changeSet.getId())
                && this.getAuthor().equalsIgnoreCase(changeSet.getAuthor());
    }
}
