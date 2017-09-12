package liquibase.database.sqlplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by sbt-gette-is on 12.09.2017.
 */
public class SqlPlusConnection {
    // sqlplus "user/pass@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(Host=hostname.network)(Port=1521))(CONNECT_DATA=(SID=remote_SID)))"
    private String url;
    private String username;
    private String password;
    private String initSQL;

    public SqlPlusConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;

        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("sqlplus/init.sql")));
        StringBuilder initSQL = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                initSQL.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.initSQL = initSQL.toString();

    }

    //TODO: Возможно, SID приходит не в урле, а каким-то другим образом. Необходимо это учесть
    public String getConnectionAsString() {
        return username + "/" + password + "@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(Host=" + url.split(":")[0] + ")(Port=" + url.split(":")[1] + "))(CONNECT_DATA=(SID=" + url.split(":")[2] + ")))";
    }

    public String getInitSQL() {
        return initSQL;
    }
}
