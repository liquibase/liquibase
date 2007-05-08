package liquibase.migrator.ant;

import liquibase.migrator.Migrator;
import org.apache.tools.ant.BuildException;

import java.sql.SQLException;
import java.util.Date;

public class DatabaseRollbackTask extends BaseLiquibaseTask {

    private Date rollbackDate;

    public Date getRollbackDate() {
        return rollbackDate;
    }

    public void setRollbackDate(Date rollbackDate) {
        this.rollbackDate = rollbackDate;
    }

    public void execute() throws BuildException {
        Migrator migrator = null;
        try {
            migrator = createMigrator();
            migrator.setMode(Migrator.EXECUTE_ROLLBACK_MODE);
            migrator.setRollbackToDate(getRollbackDate());
            migrator.migrate();
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            if (migrator != null && migrator.getDatabase() != null && migrator.getDatabase().getConnection() != null) {
                try {
                    migrator.getDatabase().getConnection().close();
                } catch (SQLException e) {
                    throw new BuildException(e);
                }
            }
        }
    }
}
