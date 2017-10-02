package liquibase.database.jvm;

import liquibase.exception.DatabaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class HsqlConnection extends JdbcConnection {

    public HsqlConnection(Connection connection) {
        super(connection);
    }


    @Override
    public void commit() throws DatabaseException {
        super.commit();

        Statement st = null;
        try {
            st = createStatement();
            final String sql = "CHECKPOINT";
            LogService.getLog(getClass()).info(LogType.WRITE_SQL, sql);
            st.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            JdbcUtils.closeStatement(st);
        }
    }

    @Override
    public void rollback() throws DatabaseException {
        super.rollback();

        Statement st = null;
        try {
            st = createStatement();
            final String sql = "CHECKPOINT";
            LogService.getLog(getClass()).info(LogType.WRITE_SQL, sql);
            st.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            JdbcUtils.closeStatement(st);
        }
    }
}
