package liquibase.database.jvm;

import liquibase.exception.DatabaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyConnection extends JdbcConnection {

    public DerbyConnection(Connection connection) {
        super(connection);
    }


    @Override
    public void commit() throws DatabaseException {
        super.commit();

        checkPoint();
    }

    @Override
    public void rollback() throws DatabaseException {
        super.rollback();

        checkPoint();
    }

    private void checkPoint() throws DatabaseException {
        Statement st = null;
        try {
            st = createStatement();
            final String sql = "CALL SYSCS_UTIL.SYSCS_CHECKPOINT_DATABASE()";
            LogService.getLog(getClass()).info(LogType.WRITE_SQL, sql);
            st.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            JdbcUtils.closeStatement(st);
        }
    }
}