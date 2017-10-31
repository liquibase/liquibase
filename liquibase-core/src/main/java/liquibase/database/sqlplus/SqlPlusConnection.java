package liquibase.database.sqlplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author gette
 */
public class SqlPlusConnection {
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

    public String getConnectionAsDescription() {
        return username + "/" + password + "@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(Host=" + url.split("@")[1].split(":")[0] + ")(Port=" + url.split("@")[1].split(":")[1] + "))(CONNECT_DATA=(SID=" + url.split("@")[1].split(":")[2] + ")))";
    }

    public String getConnectionAsString() {
        return username + "/" + password + "@//" + url.split("@")[1].split(":")[0] + ":" + url.split("@")[1].split(":")[1] + "/" + url.split("@")[1].split(":")[2];
    }

    public String getInitSQL() {
        return initSQL;
    }

    public String getExitSQL() {
        return exitSQL;
    }

    public StringBuilder readFile(String resource) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(resource)));
        StringBuilder SQL = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                SQL.append(line).append(System.getProperty("line.separator"));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SQL;
    }
}
