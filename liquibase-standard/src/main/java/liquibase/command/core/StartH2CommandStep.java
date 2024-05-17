package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.configuration.ConfigurationValueObfuscator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

public class StartH2CommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"init", "startH2"};

    public static final CommandArgumentDefinition<String> BIND_ARG;
    public static final CommandArgumentDefinition<Integer> DB_PORT_ARG;
    public static final CommandArgumentDefinition<Integer> WEB_PORT_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<Boolean> LAUNCH_BROWSER_ARG;

    private static final CommandArgumentDefinition<Boolean> DETACHED;
    /**
     * List of threads that are running the H2 database. Used to stop them when the stopH2 command is run.
     */
    public static final List<Thread> RUNNING_THREADS = new ArrayList<>();

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DB_PORT_ARG = builder.argument("dbPort", Integer.class)
                .description("Port to run h2 database on")
                .defaultValue(9090)
                .build();

        WEB_PORT_ARG = builder.argument("webPort", Integer.class)
                .description("Port to run h2's web interface on")
                .defaultValue(8080)
                .build();

        USERNAME_ARG = builder.argument("username", String.class)
                .description("Username to create in h2")
                .defaultValue("dbuser")
                .build();

        PASSWORD_ARG = builder.argument("password", String.class)
                .description("Password to use for created h2 user")
                .defaultValue("letmein")
                .setValueObfuscator(ConfigurationValueObfuscator.STANDARD)
                .build();

        BIND_ARG = builder.argument("bindAddress", String.class)
                .description("Network address to bind to")
                .defaultValue("127.0.0.1")
                .build();

        LAUNCH_BROWSER_ARG = builder.argument("launchBrowser", Boolean.class)
                .description("Whether to open a browser to the database's web interface")
                .defaultValue(true)
                .build();

        DETACHED = builder.argument("detached", Boolean.class)
                .description("When set to true, Liquibase initiates the H2 database in a new thread without blocking, allowing use within the flow command. Regardless of the parameter setting, data stored in the H2 database is cleared when the JVM exits, such as at the end of the flow command.")
                .defaultValue(false)
                .build();
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();

        System.setProperty("h2.bindAddress", commandScope.getConfiguredValue(BIND_ARG).getValue());

        System.out.println("Starting Example H2 Database...");
        System.out.println("NOTE: The database does not persist data, so stopping and restarting this process will reset it back to a blank database");
        System.out.println();

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            String msg = "ERROR: H2 was not configured properly. To use Liquibase and H2, you need to have the H2 JDBC driver jar file in liquibase/lib. Learn more at https://docs.liquibase.com/";
            System.out.println(msg);
            throw e;
        }

        final String username = commandScope.getConfiguredValue(USERNAME_ARG).getValue();
        final String password = commandScope.getConfiguredValue(PASSWORD_ARG).getValue();
        final Integer dbPort = commandScope.getConfiguredValue(DB_PORT_ARG).getValue();
        final Integer webPort = commandScope.getConfiguredValue(WEB_PORT_ARG).getValue();
        final Boolean detached = commandScope.getConfiguredValue(DETACHED).getValue();

        Thread thread = new Thread(() -> {
            try (Connection devConnection = DriverManager.getConnection("jdbc:h2:mem:dev", username, password);
                 Connection intConnection = DriverManager.getConnection("jdbc:h2:mem:integration", username, password)) {

                startTcpServer(dbPort);

                Object webServer = startWebServer(webPort);
                String devUrl = createWebSession(devConnection, webServer, commandScope.getConfiguredValue(LAUNCH_BROWSER_ARG).getValue());
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


                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    Scope.getCurrentScope().getUI().sendMessage("Shutting down H2 database...");
                }));

                Thread.sleep(Long.MAX_VALUE);
            } catch (Throwable e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                System.exit(-1);
            }
        });

        RUNNING_THREADS.add(thread);
        thread.start();

        if (Boolean.FALSE.equals(detached)) {
            thread.join();
            RUNNING_THREADS.remove(thread);
        }

        resultsBuilder.addResult("statusCode", 0);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        super.adjustCommandDefinition(commandDefinition);
        commandDefinition.setShortDescription(
                "Launches H2, an included open source in-memory database. This Java application is shipped with Liquibase, and is useful in the Getting Started experience and for testing out Liquibase commands.");
        commandDefinition.setGroupShortDescription(new String[]{"init"}, "Init commands");
    }

    protected static void startTcpServer(Integer dbPort) throws Exception {
        final Class<?> serverClass = Class.forName("org.h2.tools.Server");
        final Object tcpServer = serverClass.getMethod("createTcpServer", String[].class)
                .invoke(null, (Object) new String[]{"-tcpAllowOthers", "-tcpPort", dbPort.toString()});

        tcpServer.getClass().getMethod("start")
                .invoke(tcpServer);
    }

    protected static Object startWebServer(Integer webPort) throws Exception {
        final Class<?> serverClass = Class.forName("org.h2.tools.Server");

        final Object webServer = Class.forName("org.h2.server.web.WebServer").newInstance();
        Object web = serverClass.getConstructor(Class.forName("org.h2.server.Service"), String[].class).newInstance(webServer, new String[]{"-webPort", webPort.toString()});
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
}
