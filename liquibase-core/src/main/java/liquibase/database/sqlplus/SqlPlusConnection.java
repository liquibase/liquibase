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

    /*
    Oracle JDBC Thin using a Service Name:

    jdbc:oracle:thin:@//<host>:<port>/<service_name>

    Oracle JDBC Thin using an SID:

    jdbc:oracle:thin:@<host>:<port>:<SID>

    Oracle JDBC Thin using a TNSName:

    jdbc:oracle:thin:@<TNSName>

 */
    public String getConnectionAsString() {
        String conn = url.split("@")[1];
        if (conn.startsWith("//"))
            return username + "/" + password + "@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(Host=" + conn.split(":")[0].replaceFirst("//", "") + ")(Port=" + conn.split(":")[1].split("/")[0] + "))(CONNECT_DATA=(SERVICE_NAME=" + conn.split(":")[1].split("/")[1] + ")))";
        else if (conn.split(":").length > 2)
            return username + "/" + password + "@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(Host=" + conn.split(":")[0] + ")(Port=" + conn.split(":")[1] + "))(CONNECT_DATA=(SID=" + conn.split(":")[2] + ")))";
        else
            return username + "/" + password + "@" + conn;
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
