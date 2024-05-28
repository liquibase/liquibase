package liquibase.helper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDriver;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.DatabaseChangeRegistration;

public class DBTest {
    static final String USERNAME = "PROSCHEMA";
    static final String PASSWORD = "PROSCHEMA";
    static String URL = "jdbc:oracle:thin:@localhost:1521/BUCKET_01";

    public static void main(String[] args) {
        DBTest oracleDCN = new DBTest();
        try {
            oracleDCN.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void run() throws Exception {
        OracleConnection conn = connect();
        Statement stmt= conn.createStatement();
        ResultSet rs = stmt.executeQuery("select regid,callback from USER_CHANGE_NOTIFICATION_REGS");
        while(rs.next()) {
            long regid = rs.getLong(1);
            String callback = rs.getString(2);
            ((OracleConnection)conn).unregisterDatabaseChangeNotification(regid,callback);
        }
        stmt.close();
        Properties prop = new Properties();
        prop.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true");
        DatabaseChangeRegistration dcr = conn.registerDatabaseChangeNotification(prop);

        try {
            dcr.addListener(new DatabaseChangeListener() {
                public void onDatabaseChangeNotification(DatabaseChangeEvent dce) {
                    System.out.println("GIVE ME SOMETHING!");
                }
            });
            //conn.unregisterDatabaseChangeNotification(dcr);
            stmt = conn.createStatement();
            ((OracleStatement) stmt).setDatabaseChangeRegistration(dcr);
            rs = stmt.executeQuery("select * from hello");
            while (rs.next()) {
            }
            rs.close();
            stmt.close();
            conn.unregisterDatabaseChangeNotification(dcr);
            conn.close();
        } catch (SQLException ex) {
            if (conn != null) {
                conn.unregisterDatabaseChangeNotification(dcr);
                conn.close();
            }
            throw ex;
        }
    }

    OracleConnection connect() throws SQLException {
        OracleDriver dr = new OracleDriver();
        Properties prop = new Properties();
        prop.setProperty("user", DBTest.USERNAME);
        prop.setProperty("password", DBTest.PASSWORD);
        return (OracleConnection) dr.connect(DBTest.URL, prop);
    }
}