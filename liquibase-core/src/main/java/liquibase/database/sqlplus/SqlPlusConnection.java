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
    private String exitSQL;

    public SqlPlusConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;

        this.initSQL = readFile("sqlplus/init.sql").toString();
        this.exitSQL = readFile("sqlplus/exit.sql").toString();

    }

    //TODO: Возможно, SID приходит не в урле, а каким-то другим образом. Необходимо это учесть
    public String getConnectionAsString() {
        return username + "/" + password + "@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(Host=" + url.split("@")[1].split(":")[0] + ")(Port=" + url.split("@")[1].split(":")[1] + "))(CONNECT_DATA=(SID=" + url.split("@")[1].split(":")[2] + ")))";
    }

    public String getInitSQL() {
        return initSQL;
    }

    public String getExitSQL() {
        return exitSQL;
    }

    public StringBuilder readFile(String resource){
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(resource)));
        StringBuilder SQL = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                SQL.append(line+System.getProperty("line.separator"));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SQL;
    }
}
