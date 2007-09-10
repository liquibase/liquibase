package liquibase;

import java.util.Date;

/**
 * Encapsulates information about a previously-ran change set.  Used to build rollback statements. 
 */
public class RanChangeSet {
    private final String changeLog;
    private final String id;
    private final String author;
    private final String md5sum;
    private final Date dateExecuted;
    private String tag;

    public RanChangeSet(ChangeSet changeSet) {
        this(changeSet.getDatabaseChangeLog().getFilePath(),
             changeSet.getId(),
             changeSet.getAuthor(),
             changeSet.getMd5sum(),
             new Date(),
             null);
    }

    public RanChangeSet(String changeLog, String id, String author, String md5sum, Date dateExecuted, String tag) {
        this.changeLog = changeLog;
        this.id = id;
        this.author = author;
        this.md5sum = md5sum;
        this.dateExecuted = new Date(dateExecuted.getTime());
        this.tag = tag;
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

    public String getMd5sum() {
        return md5sum;
    }

    public Date getDateExecuted() {
        return (Date) dateExecuted.clone();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

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

    public int hashCode() {
        int result;
        result = changeLog.hashCode();
        result = 29 * result + id.hashCode();
        result = 29 * result + author.hashCode();
        return result;
    }

    public boolean isSameAs(ChangeSet changeSet) {
        return this.getChangeLog().equals(changeSet.getDatabaseChangeLog().getFilePath())
                && this.getId().equals(changeSet.getId())
                && this.getAuthor().equals(changeSet.getAuthor());
    }
}
