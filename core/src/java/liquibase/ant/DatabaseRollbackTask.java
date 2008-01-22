package liquibase.ant;

import liquibase.migrator.Migrator;
import org.apache.tools.ant.BuildException;

import java.sql.SQLException;
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
            if (getRollbackCount() != null) {
                migrator.rollback(getRollbackCount(), getContexts());
            } else if (getRollbackDate() != null) {
                migrator.rollback(getRollbackDate(), getContexts());
            } else if (getRollbackTag() != null) {
                migrator.rollback(getRollbackTag(), getContexts());
            } else {
                throw new BuildException("Must specify rollbackCount, rollbackDate, or rollbackTag");
            }
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            if (migrator != null && migrator.getDatabase() != null && migrator.getDatabase().getConnection() != null) {
                try {
                    migrator.getDatabase().getConnection().close();
                } catch (SQLException e) {
                    ; //could not close
                }
            }
        }
    }
}
