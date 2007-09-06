package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class HSQLConnectionDelegate extends SQLConnectionDelegate {

    public HSQLConnectionDelegate(Connection connection) {
        super(connection);
    }


    public void commit() throws SQLException {
        super.commit();

        Statement st = null;
        try {
            st = createStatement();
            st.execute("CHECKPOINT");
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }
}
