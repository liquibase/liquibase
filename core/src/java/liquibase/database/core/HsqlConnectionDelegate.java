package liquibase.database.core;

import liquibase.database.SQLConnectionDelegate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class HsqlConnectionDelegate extends SQLConnectionDelegate {

    public HsqlConnectionDelegate(Connection connection) {
        super(connection);
    }


    @Override
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

    @Override
    public void rollback() throws SQLException {
        super.rollback();

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
