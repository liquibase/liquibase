package liquibase.example;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Wrapper around the h2 console for use in the "examples" directory
 */
public class StartH2Main {

    private static final String dbPort = "9090";
    private static final String webPort = "8090";
    private static final String tcpUrl = "jdbc:h2:tcp://localhost:" + dbPort + "/mem:example";
    private static final String username = "dbuser";
    private static final String password = "letmein";

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Example H2 Database...");
        System.out.println("NOTE: The database does not persist data, so stopping and restarting this process will reset it back to a blank database");
        System.out.println();

        Class.forName("org.h2.Driver");

        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:example", username, password)) {

            System.out.println("Connection Information:" + System.lineSeparator() +
                    "  Database URL: " + tcpUrl + System.lineSeparator() +
                    "  Username: " + username + System.lineSeparator() +
                    "  Password: " + password + System.lineSeparator() +
                    "" + System.lineSeparator() +
                    "Opening Database Console in Browser..." + System.lineSeparator() +
                    "  Web URL: http://localhost:" + webPort + System.lineSeparator());

            startTcpServer();
            startWebServer(conn);

            while (true) {
                Thread.sleep(60 * 1000);
            }

        } catch (Throwable e) {
            System.err.println("Error starting H2");
            e.printStackTrace();
            System.exit(-1);
        }


    }

    protected static void startTcpServer() throws Exception {
        final Class<?> serverClass = Class.forName("org.h2.tools.Server");
        final Object tcpServer = serverClass.getMethod("createTcpServer", String[].class)
                .invoke(null, (Object) new String[]{"-tcpAllowOthers", "-tcpPort", dbPort});

        tcpServer.getClass().getMethod("start")
                .invoke(tcpServer);
    }

    protected static void startWebServer(Connection conn) throws Exception {
        final Class<?> serverClass = Class.forName("org.h2.tools.Server");

//        final Object webServer = serverClass.getMethod("startWebServer", Connection.class)
//                .invoke(serverClass, conn);

        final Object webServer = Class.forName("org.h2.server.web.WebServer").newInstance();
        Object web = serverClass.getConstructor(Class.forName("org.h2.server.Service"), String[].class).newInstance(webServer, (Object) new String[]{"-webPort", webPort});
        web.getClass().getMethod("start").invoke(web);

        Object url = webServer.getClass().getMethod("addSession", Connection.class).invoke(webServer, conn);
        System.out.println(url);

        serverClass.getMethod("openBrowser", String.class).invoke(null, url);

//        webServer.getClass().getMethod("start")
//                .invoke(webServer);
    }

}
