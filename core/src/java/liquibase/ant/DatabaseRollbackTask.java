package liquibase.ant;

import liquibase.migrator.Migrator;
import org.apache.tools.ant.BuildException;

import java.io.Writer;
import java.util.Date;

/**
 * Ant task for rolling back a database.
 */
public class DatabaseRollbackTask extends BaseLiquibaseTask {

    private Date rollbackDate;
    private String rollbackTag;
    private Integer rollbackCount;

    public Date getRollbackDate() {
        if (rollbackDate == null) {
            return null;
        }
        
        return (Date) rollbackDate.clone();
    }

    public void setRollbackDate(Date rollbackDate) {
        if (rollbackDate != null) {
            this.rollbackDate = new Date(rollbackDate.getTime());
        }
    }

    public String getRollbackTag() {
        return rollbackTag;
    }

    public void setRollbackTag(String rollbackTag) {
        this.rollbackTag = rollbackTag;
    }

    public Integer getRollbackCount() {
        return rollbackCount;
    }

    public void setRollbackCount(Integer rollbackCount) {
        this.rollbackCount = rollbackCount;
    }

    public void execute() throws BuildException {
        if (getRollbackDate() == null && getRollbackCount() == null && getRollbackTag() == null) {
            throw new BuildException("rollbackDatabase requires rollbackTag, rollbackDate, or rollbackCount to be set");
        }
        Migrator migrator = null;
        try {
            migrator = createMigrator();
            Writer writer = createOutputWriter();
            if (getRollbackCount() != null) {
                if (writer == null) {
                    migrator.rollback(getRollbackCount(), getContexts());
                } else {
                    migrator.rollback(getRollbackCount(), getContexts(), writer);
                }
            } else if (getRollbackDate() != null) {
                if (writer == null) {
                    migrator.rollback(getRollbackDate(), getContexts());
                } else {
                    migrator.rollback(getRollbackDate(), getContexts(), writer);
                }
            } else if (getRollbackTag() != null) {
                if (writer == null) {
                    migrator.rollback(getRollbackTag(), getContexts());
                } else {
                    migrator.rollback(getRollbackTag(), getContexts(), writer);
                }
            } else {
                throw new BuildException("Must specify rollbackCount, rollbackDate, or rollbackTag");
            }
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(migrator);
        }
    }
}
