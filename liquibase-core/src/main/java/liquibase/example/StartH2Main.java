package liquibase.example;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Wrapper around the h2 console for use in the "examples" directory
 */
public class StartH2Main {

    private static final String dbPort = "9090";
    private static final String webPort = "8090";
    private static String username;
    private static String password;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Example H2 Database...");
        System.out.println("NOTE: The database does not persist data, so stopping and restarting this process will reset it back to a blank database");
        System.out.println();

        parseUsernameAndPassword(args);

        Class.forName("org.h2.Driver");

        try (Connection devConnection = DriverManager.getConnection("jdbc:h2:mem:dev", username, password);
             Connection intConnection = DriverManager.getConnection("jdbc:h2:mem:integration", username, password)) {

            startTcpServer();

            Object webServer = startWebServer();
            String devUrl = createWebSession(devConnection, webServer, true);
            String intUrl = createWebSession(intConnection, webServer, false);

            System.out.println("Connection Information:" + System.lineSeparator() +
                    "  Dev database: " + System.lineSeparator() +
                    "    JDBC URL: jdbc:h2:tcp://localhost:" + dbPort + "/mem:dev" + System.lineSeparator() +
                    "    Username: " + username + System.lineSeparator() +
                    "    Password: " + password + System.lineSeparator() +
                    "  Integration database: " + System.lineSeparator() +
                    "    JDBC URL: jdbc:h2:tcp://localhost:" + dbPort + "/mem:integration" + System.lineSeparator() +
                    "    Username: " + username + System.lineSeparator() +
                    "    Password: " + password + System.lineSeparator() +
                    "" + System.lineSeparator() +
                    "Opening Database Console in Browser..." + System.lineSeparator() +
                    "  Dev Web URL: " + devUrl + System.lineSeparator() +
                    "  Integration Web URL: " + intUrl + System.lineSeparator());


            long seconds = 60;
            long millis = 1000;
            while (true) {
                Thread.sleep(seconds * millis);
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

    protected static Object startWebServer() throws Exception {
        final Class<?> serverClass = Class.forName("org.h2.tools.Server");

        final Object webServer = Class.forName("org.h2.server.web.WebServer").newInstance();
        Object web = serverClass.getConstructor(Class.forName("org.h2.server.Service"), String[].class).newInstance(webServer, (Object) new String[]{"-webPort", webPort});
        web.getClass().getMethod("start").invoke(web);

        return webServer;
    }

    private static String createWebSession(Connection connection, Object webServer, boolean openBrowser) throws Exception {
        final Class<?> serverClass = Class.forName("org.h2.tools.Server");

        String url = (String) webServer.getClass().getMethod("addSession", Connection.class).invoke(webServer, connection);

        if (openBrowser) {
            try {
                serverClass.getMethod("openBrowser", String.class).invoke(null, url);
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null && e.getCause() != null) {
                    message = e.getCause().getMessage();
                }
                System.out.println("Cannot open browser: "+ message);
                System.out.println("");
            }
        }

        return url;
    }

    private static void parseUsernameAndPassword(String[] args) throws Exception {
        Options options = new Options();
        Option argUsername = new Option("u", "username", true, "Database username");
        argUsername.setRequired(true);
        options.addOption(argUsername);

        Option argPassword = new Option("p", "password", true, "Database password");
        argPassword.setRequired(true);
        options.addOption(argPassword);

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);
        username = commandLine.getOptionValue("username");
        password = commandLine.getOptionValue("password");
    }
}
