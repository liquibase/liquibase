package liquibase.migrator;

import java.util.Date;

public class RanChangeSet {
    private String changeLog;
    private String id;
    private String author;
    private String md5sum;
    private Date dateExecuted;
    private String tag;

    public RanChangeSet(ChangeSet changeSet) {
        this.changeLog = changeSet.getDatabaseChangeLog().getFilePath();
        this.id = changeSet.getId();
        this.author = changeSet.getAuthor();
        this.md5sum = changeSet.getMd5sum();
        this.dateExecuted = new Date();
    }

    public RanChangeSet(String changeLog, String id, String author, String md5sum, Date dateExecuted, String tag) {
        this.changeLog = changeLog;
        this.id = id;
        this.author = author;
        this.md5sum = md5sum;
        this.dateExecuted = dateExecuted;
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
        return dateExecuted;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RanChangeSet that = (RanChangeSet) o;

        if (!author.equals(that.author)) return false;
        if (!changeLog.equals(that.changeLog)) return false;
        return id.equals(that.id);

    }

    public int hashCode() {
        int result;
        result = changeLog.hashCode();
        result = 29 * result + id.hashCode();
        result = 29 * result + author.hashCode();
        return result;
    }

    public boolean isSameAs(ChangeSet changeSet) {
        return  this.getChangeLog().equals(changeSet.getDatabaseChangeLog().getFilePath())
                && this.getId().equals(changeSet.getId())
                && this.getAuthor().equals(changeSet.getAuthor());
    }
}
