package liquibase.database.jvm;

import liquibase.exception.DatabaseException;
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
            st.execute("CALL SYSCS_UTIL.SYSCS_CHECKPOINT_DATABASE()");
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            JdbcUtils.closeStatement(st);
        }
    }
}