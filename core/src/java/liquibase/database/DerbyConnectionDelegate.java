package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyConnectionDelegate extends SQLConnectionDelegate {

    public DerbyConnectionDelegate(Connection connection) {
        super(connection);
    }


    public void commit() throws SQLException {
        super.commit();

        Statement st = null;
        try {
            st = createStatement();
            st.execute("CALL SYSCS_UTIL.SYSCS_CHECKPOINT_DATABASE()");
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }
}