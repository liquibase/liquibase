package liquibase.integration.commandline;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.spi.FilterReply;
import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandFactory;
import liquibase.command.LiquibaseCommand;
import liquibase.command.core.*;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.*;
import liquibase.license.*;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogLevel;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.ISODateFormat;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtils;
import liquibase.util.xml.XMLResourceBundle;
import liquibase.util.xml.XmlResourceBundleControl;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.ResourceBundle.getBundle;

/**
 * Class for executing Liquibase via the command line.
 */
public class Main {
    private static final String ERRORMSG_UNEXPECTED_PARAMETERS = "unexpected.command.parameters";
    private static final Logger LOG = LogService.getLog(Main.class);
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    private static XMLResourceBundle commandLineHelpBundle = ((XMLResourceBundle) getBundle
            ("liquibase/i18n/liquibase-commandline-helptext", new XmlResourceBundleControl()));

    protected ClassLoader classLoader;
    protected String driver;
    protected String username;
    protected String password;
    protected String url;
    protected String databaseClass;
    protected String defaultSchemaName;
    protected String outputDefaultSchema;
    protected String outputDefaultCatalog;
    protected String liquibaseCatalogName;
    protected String liquibaseSchemaName;
    protected String databaseChangeLogTableName;
    protected String databaseChangeLogLockTableName;
    protected String databaseChangeLogTablespaceName;
    protected String defaultCatalogName;
    protected String changeLogFile;
    protected String overwriteOutputFile;
    protected String classpath;
    protected String contexts;
    protected String labels;
    protected String driverPropertiesFile;
    protected String propertyProviderClass;
    protected String changeExecListenerClass;
    protected String changeExecListenerPropertiesFile;
    protected Boolean promptForNonLocalDatabase;
    protected Boolean includeSystemClasspath;
    protected Boolean strict = Boolean.TRUE;
    protected String defaultsFile = "liquibase.properties";
    protected String diffTypes;
    protected String changeSetAuthor;
    protected String changeSetContext;
    protected String dataOutputDirectory;
    protected String referenceDriver;
    protected String referenceUrl;
    protected String referenceUsername;
    protected String referencePassword;
    protected String referenceDefaultCatalogName;
    protected String referenceDefaultSchemaName;
    protected String currentDateTimeFunction;
    protected String command;
    protected Set<String> commandParams = new LinkedHashSet<>();
    protected String logLevel;
    protected String logFile;
    protected Map<String, Object> changeLogParameters = new HashMap<>();
    protected String outputFile;
    protected String excludeObjects;
    protected Boolean includeCatalog;
    protected String includeObjects;
    protected Boolean includeSchema;
    protected Boolean includeTablespace;
    protected String outputSchemasAs;
    protected String referenceSchemas;
    protected String schemas;
    protected String snapshotFormat;
    protected String liquibaseProLicenseKey;
    private boolean liquibaseProLicenseValid = false;
    private Boolean managingLogConfig = null;
    private boolean outputsLogMessages = false;
    protected String sqlFile;
    protected String delimiter;
    protected String rollbackScript;

    private static int[] suspiciousCodePoints = {160, 225, 226, 227, 228, 229, 230, 198, 200, 201, 202, 203,
            204, 205, 206, 207, 209, 210, 211, 212, 213, 214, 217, 218, 219,
            220, 222, 223, 232, 233, 234, 235, 236, 237, 238, 239, 241,
            249, 250, 251, 252, 255, 284, 332, 333, 334, 335, 336, 337, 359,
            360, 361, 362, 363, 364, 365, 366, 367, 377, 399,
            8192, 8193, 8194, 8196, 8197, 8199, 8200, 8201, 8202, 8203, 8211, 8287
    };

    protected static class CodePointCheck {
        public int position;
        public char ch;
    }

    /**
     * Entry point. This is what gets executes when starting this program from the command line. This is actually
     * a simple wrapper so that an errorlevel of != 0 is guaranteed in case of an uncaught exception.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int errorLevel = 0;
        try {
            errorLevel = run(args);
        } catch (Throwable e) {
            System.exit(-1);
        }
        System.exit(errorLevel);
    }


    /**
     * Process the command line arguments and perform the appropriate main action (update, rollback etc.)
     *
     * @param args the command line arguments
     * @return the errorlevel to be returned to the operating system, e.g. for further processing by scripts
     * @throws LiquibaseException a runtime exception
     */
    public static int run(String[] args) throws LiquibaseException {
        Main main = null;
        Logger log = null;
        try {
            main = new Main();
            main.reconfigureLogging();

            log = LogService.getLog(Main.class);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }

        try {
            GlobalConfiguration globalConfiguration = LiquibaseConfiguration.getInstance().getConfiguration
                    (GlobalConfiguration.class);

            if (!globalConfiguration.getShouldRun()) {
                log.warning(LogType.USER_MESSAGE, (
                        String.format(coreBundle.getString("did.not.run.because.param.was.set.to.false"),
                                LiquibaseConfiguration.getInstance().describeValueLookupLogic(
                                        globalConfiguration.getProperty(GlobalConfiguration.SHOULD_RUN)))));
                return 0;
            }

            if ((args.length == 0) || ((args.length == 1) && ("--" + OPTIONS.HELP).equals(args[0]))) {
                main.printHelp(System.out);
                return 0;
            } else if (("--" + OPTIONS.VERSION).equals(args[0])) {
                main.command = "";
                main.reconfigureLogging();
                main.parseDefaultPropertyFiles();
                PrintStream stream = System.out;
                stream.println(CommandLineUtils.getBanner());
                stream.println(String.format(coreBundle.getString("version.number"), LiquibaseUtil.getBuildVersion()));

                LicenseService licenseService = LicenseServiceFactory.getInstance().getLicenseService();
                if (licenseService != null && main.liquibaseProLicenseKey != null) {
                    Location licenseKeyLocation =
                                new Location("property liquibaseProLicenseKey", LocationType.BASE64_STRING, main.liquibaseProLicenseKey);
                    LicenseInstallResult result = licenseService.installLicense(licenseKeyLocation);
                    if (result.code != 0) {
                        String allMessages = String.join("\n", result.messages);
                        log.warning(LogType.USER_MESSAGE, allMessages);
                    }
                }
                stream.println(licenseService.getLicenseInfo());

                stream.println(String.format("Running Java under %s (Version %s)",
                        System.getProperties().getProperty("java.home"),
                        System.getProperty("java.version")
                ));
                return 0;
            }

            //
            // Look for characters which cannot be handled
            //
            for (int i = 0; i < args.length; i++) {
                CodePointCheck codePointCheck = checkArg(args[i]);
                if (codePointCheck != null) {
                    String message =
                            "A non-standard character '" + codePointCheck.ch +
                                    "' was detected on the command line at position " +
                                    (codePointCheck.position + 1) + " of argument number " + (i + 1) +
                                    ".\nIf problems occur, please remove the character and try again.";
                    LOG.warning(message);
                    System.err.println(message);
                }
            }

            try {
                main.parseOptions(args);
                if (main.command == null) {
                    main.printHelp(System.out);
                    return 0;
                }
            } catch (CommandLineParsingException e) {
                log.info(LogType.USER_MESSAGE, CommandLineUtils.getBanner());
                log.warning(LogType.USER_MESSAGE, coreBundle.getString("how.to.display.help"));
                throw e;
            }

            main.reconfigureLogging();

            log.info(LogType.LOG, CommandLineUtils.getBanner());
            LicenseService licenseService = LicenseServiceFactory.getInstance().getLicenseService();
            if (licenseService != null) {

                if (main.liquibaseProLicenseKey == null) {
                    log.info(LogType.LOG, "No Liquibase Pro license key supplied. Please set liquibaseProLicenseKey on command line or in liquibase.properties to use Liquibase Pro features.");
                } else {
                    Location licenseKeyLocation = new Location("property liquibaseProLicenseKey", LocationType.BASE64_STRING, main.liquibaseProLicenseKey);
                    LicenseInstallResult result = licenseService.installLicense(licenseKeyLocation);
                    if (result.code != 0) {
                        String allMessages = String.join("\n", result.messages);
                        log.warning(LogType.USER_MESSAGE, allMessages);
                    } else {
                        main.liquibaseProLicenseValid = true;
                    }
                }

                //
                // Check to see if we have an expired license
                //
                if (licenseService.daysTilExpiration() < 0) {
                    main.liquibaseProLicenseValid = false;
                }
                log.info(LogType.USER_MESSAGE, licenseService.getLicenseInfo());
            }

            log.info(LogType.USER_MESSAGE, CommandLineUtils.getBanner());


            if (main.commandParams.contains("--help") &&
                    (main.command.startsWith("rollbackOneChangeSet") ||
                            main.command.startsWith("rollbackOneUpdate") ||
                            (main.command.startsWith("diff") && main.isFormattedDiff()))) {
                //don't need to check setup
            } else {
                List<String> setupMessages = main.checkSetup();
                if (!setupMessages.isEmpty()) {
                    main.printHelp(setupMessages, isStandardOutputRequired(main.command) ? System.err : System.out);
                    return 1;
                }
            }

            main.applyDefaults();
            main.configureClassLoader();
            main.doMigration();

            if (COMMANDS.UPDATE.equals(main.command)) {
                log.info(LogType.USER_MESSAGE, coreBundle.getString("update.successful"));
            } else if (main.command.startsWith(COMMANDS.ROLLBACK) && !main.command.endsWith("SQL")) {
                log.info(LogType.USER_MESSAGE, coreBundle.getString("rollback.successful"));
            } else if (!main.command.endsWith("SQL")) {
                log.info(LogType.USER_MESSAGE, String.format(coreBundle.getString("command.successful"), main.command));
            }
        } catch (Throwable e) {
            String message = e.getMessage();
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            if (message == null) {
                message = coreBundle.getString("unknown.reason");
            }
            // At a minimum, log the message.  We don't need to print the stack
            // trace because the logger already did that upstream.
            try {
                if (e.getCause() instanceof ValidationFailedException) {
                    ((ValidationFailedException) e.getCause()).printDescriptiveError(System.out);
                } else {
                    if (main.outputsLogMessages) {
                        log.severe(LogType.USER_MESSAGE, (String.format(coreBundle.getString("unexpected.error"), message)), e);
                    } else {
                        log.severe(LogType.USER_MESSAGE, (String.format(coreBundle.getString("unexpected.error"), message)));
                        log.severe(LogType.USER_MESSAGE, coreBundle.getString("for.more.information.use.loglevel.flag"));

                        //send it to the LOG in case we're using logFile
                        log.severe(LogType.LOG, (String.format(coreBundle.getString("unexpected.error"), message)), e);
                    }
                }
            } catch (IllegalFormatException e1) {
                e1.printStackTrace();
            }
            throw new LiquibaseException(String.format(coreBundle.getString("unexpected.error"), message), e);
        }
        return 0;
    }

    /**
     * Set up the logging to the STDOUT/STDERR console streams.
     */
    protected void reconfigureLogging() {
        org.slf4j.Logger configuredLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        if (managingLogConfig == null) {
            List<String> appenders = new ArrayList<>();
            if ("ch.qos.logback.classic.Logger".equals(configuredLogger.getClass().getName())) {
                Iterator<Appender<ILoggingEvent>> appenderIterator = ((ch.qos.logback.classic.Logger) configuredLogger).iteratorForAppenders();
                while (appenderIterator.hasNext()) {
                    Appender<ILoggingEvent> next = appenderIterator.next();
                    appenders.add(next.getName());
                }

                if (appenders.size() == 1 && appenders.get(0).equals("console")) {
                    //normal logger setup, can modify it as we need
                    this.managingLogConfig = true;
                } else {
                    LogService.getLog(getClass()).debug("Custom logging config found, not going to modify");
                    this.managingLogConfig = false;
                }
            } else {
                System.err.println(
                        "Liquibase command line logging cannot be configured; a supported org.slf4j.Logger implementation is not on the classpath.");
                this.managingLogConfig = false;
            }
        }

        if (!managingLogConfig) {
            return;
        }

        if (this.command == null) {
            //not yet parsed the call enough to set any log config
            return;
        }

        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        Level logLevel;
        if (this.logLevel == null) {
            logLevel = Level.OFF;
        } else {
            if (this.logLevel.equalsIgnoreCase(LogLevel.DEBUG.name())) {
                this.logLevel = Level.DEBUG.toString();
            } else if (this.logLevel.equalsIgnoreCase(LogLevel.INFO.name())) {
                this.logLevel = Level.INFO.toString();
            } else if (this.logLevel.equalsIgnoreCase(LogLevel.WARNING.name())) {
                this.logLevel = Level.WARN.toString();
            } else if (this.logLevel.equalsIgnoreCase(LogLevel.SEVERE.name())) {
                this.logLevel = Level.ERROR.toString();
            }

            if (this.logLevel.equalsIgnoreCase(java.util.logging.Level.FINE.toString())
                    || this.logLevel.equalsIgnoreCase(java.util.logging.Level.FINER.toString())
                    || this.logLevel.equalsIgnoreCase(java.util.logging.Level.FINEST.toString())) {
                this.logLevel = Level.DEBUG.toString();
            } else if (this.logLevel.equalsIgnoreCase(java.util.logging.Level.INFO.toString())) {
                this.logLevel = Level.INFO.toString();
            } else if (this.logLevel.equalsIgnoreCase(java.util.logging.Level.WARNING.toString())) {
                this.logLevel = Level.WARN.toString();
            } else if (this.logLevel.equalsIgnoreCase(java.util.logging.Level.SEVERE.toString())) {
                this.logLevel = Level.ERROR.toString();
            }


            logLevel = Level.toLevel(this.logLevel);
        }

        rootLogger.setLevel(Level.DEBUG);

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(rootLogger.getLoggerContext());
        consoleAppender.setName("liquibase-cli");

        PatternLayoutEncoder logMessagePattern = new PatternLayoutEncoder();
        logMessagePattern.setContext(rootLogger.getLoggerContext());
        logMessagePattern.setPattern("%d{HH:mm:ss.SSS} %-5level [%logger]: %message%n");
        logMessagePattern.start();

        if (isStandardOutputRequired(this.command)) {
            consoleAppender.setTarget(ConsoleTarget.SystemErr.getName());
        }

        FileAppender fileAppender = null;
        if (this.logFile != null) {
            fileAppender = new FileAppender<>();
            fileAppender.setContext(rootLogger.getLoggerContext());
            fileAppender.setEncoder(logMessagePattern);
            fileAppender.setFile(this.logFile);
            fileAppender.setAppend(true);

            Level fileLogLevel = logLevel;
            if (logLevel == Level.OFF) {
                //if we're making a file, make sure things are logged
                fileLogLevel = Level.DEBUG;
            } else {
                //make console logging off since logs go to file not console
                logLevel = Level.OFF;
            }
            ConsoleFilter logOutputFilter = new ConsoleFilter(fileLogLevel);
            logOutputFilter.start();
            fileAppender.addFilter(logOutputFilter);

            fileAppender.start();
        }

        if (logLevel == Level.OFF) {
            ConsoleFilter userOutput = new ConsoleFilter(logLevel, LogType.USER_MESSAGE);
            userOutput.setContext(rootLogger.getLoggerContext());
            userOutput.start();

            PatternLayoutEncoder userMessagePattern = new PatternLayoutEncoder();
            userMessagePattern.setContext(rootLogger.getLoggerContext());
            userMessagePattern.setPattern("%msg%n");
            userMessagePattern.start();

            consoleAppender.setEncoder(userMessagePattern);

            consoleAppender.addFilter(userOutput);
        } else {
            ConsoleFilter userOutput = new ConsoleFilter(logLevel);
            userOutput.setContext(rootLogger.getLoggerContext());
            userOutput.start();

            consoleAppender.setEncoder(logMessagePattern);

            consoleAppender.addFilter(userOutput);

            this.outputsLogMessages = true;
        }

        consoleAppender.start();

        //replace the existing appenders
        final Iterator<Appender<ILoggingEvent>> appenderIterator = rootLogger.iteratorForAppenders();
        while (appenderIterator.hasNext()) {
            final Appender<ILoggingEvent> next = appenderIterator.next();
            next.stop();
            rootLogger.detachAppender(next);
        }

        rootLogger.addAppender(consoleAppender);
        if (fileAppender != null) {
            rootLogger.addAppender(fileAppender);
        }
    }

    /**
     * Splits a String of the form "key=value" into the respective parts.
     *
     * @param arg The String expression to split
     * @return An array of exactly 2 entries
     * @throws CommandLineParsingException if the string cannot be split into exactly 2 parts
     */
    // What the number 2 stands for is obvious from the context
    @SuppressWarnings("squid:S109")
    private static String[] splitArg(String arg) throws CommandLineParsingException {
        String[] splitArg = arg.split("=", 2);
        if (splitArg.length < 2) {
            throw new CommandLineParsingException(
                    String.format(coreBundle.getString("could.not.parse.expression"), arg)
            );
        }

        splitArg[0] = splitArg[0].replaceFirst("--", "");
        return splitArg;
    }

    /**
     * Returns true if the given command requires stdout
     *
     * @param command the command to check
     * @return true if stdout needs for a command, false if not
     */
    private static boolean isStandardOutputRequired(String command) {
        return COMMANDS.SNAPSHOT.equalsIgnoreCase(command)
                || COMMANDS.SNAPSHOT_REFERENCE.equalsIgnoreCase(command)
                || COMMANDS.CHANGELOG_SYNC_SQL.equalsIgnoreCase(command)
                || COMMANDS.MARK_NEXT_CHANGESET_RAN_SQL.equalsIgnoreCase(command)
                || COMMANDS.UPDATE_COUNT_SQL.equalsIgnoreCase(command)
                || COMMANDS.UPDATE_TO_TAG_SQL.equalsIgnoreCase(command)
                || COMMANDS.UPDATE_SQL.equalsIgnoreCase(command)
                || COMMANDS.ROLLBACK_SQL.equalsIgnoreCase(command)
                || COMMANDS.ROLLBACK_TO_DATE_SQL.equalsIgnoreCase(command)
                || COMMANDS.ROLLBACK_COUNT_SQL.equalsIgnoreCase(command)
                || COMMANDS.FUTURE_ROLLBACK_SQL.equalsIgnoreCase(command)
                || COMMANDS.FUTURE_ROLLBACK_COUNT_SQL.equalsIgnoreCase(command)
                || COMMANDS.FUTURE_ROLLBACK_FROM_TAG_SQL.equalsIgnoreCase(command);
    }

    /**
     * Returns true if the parameter --changeLogFile is requited for a given command
     *
     * @param command the command to test
     * @return true if a ChangeLog is required, false if not.
     */
    private static boolean isChangeLogRequired(String command) {
        return command.toLowerCase().startsWith(COMMANDS.UPDATE)
                || (command.toLowerCase().startsWith(COMMANDS.ROLLBACK) &&
                (!command.equalsIgnoreCase(COMMANDS.ROLLBACK_ONE_CHANGE_SET) &&
                        !command.equalsIgnoreCase(COMMANDS.ROLLBACK_ONE_UPDATE)))
                || COMMANDS.CALCULATE_CHECKSUM.equalsIgnoreCase(command)
                || COMMANDS.STATUS.equalsIgnoreCase(command)
                || COMMANDS.VALIDATE.equalsIgnoreCase(command)
                || COMMANDS.CHANGELOG_SYNC.equalsIgnoreCase(command)
                || COMMANDS.CHANGELOG_SYNC_SQL.equalsIgnoreCase(command)
                || COMMANDS.GENERATE_CHANGELOG.equalsIgnoreCase(command)
                || COMMANDS.DIFF_CHANGELOG.equalsIgnoreCase(command)
                || COMMANDS.ROLLBACK_ONE_CHANGE_SET.equalsIgnoreCase(command)
                || COMMANDS.ROLLBACK_ONE_UPDATE.equalsIgnoreCase(command);
    }

    /**
     * Returns true if the given arg is a valid main command of Liquibase.
     *
     * @param arg the String to test
     * @return true if it is a valid main command, false if not
     */
    private static boolean isCommand(String arg) {
        return COMMANDS.MIGRATE.equals(arg)
                || COMMANDS.MIGRATE_SQL.equalsIgnoreCase(arg)
                || COMMANDS.UPDATE.equalsIgnoreCase(arg)
                || COMMANDS.UPDATE_SQL.equalsIgnoreCase(arg)
                || COMMANDS.UPDATE_COUNT.equalsIgnoreCase(arg)
                || COMMANDS.UPDATE_COUNT_SQL.equalsIgnoreCase(arg)
                || COMMANDS.UPDATE_TO_TAG.equalsIgnoreCase(arg)
                || COMMANDS.UPDATE_TO_TAG_SQL.equalsIgnoreCase(arg)
                || COMMANDS.ROLLBACK.equalsIgnoreCase(arg)
                || COMMANDS.ROLLBACK_TO_DATE.equalsIgnoreCase(arg)
                || COMMANDS.ROLLBACK_COUNT.equalsIgnoreCase(arg)
                || COMMANDS.ROLLBACK_SQL.equalsIgnoreCase(arg)
                || COMMANDS.ROLLBACK_TO_DATE_SQL.equalsIgnoreCase(arg)
                || COMMANDS.ROLLBACK_COUNT_SQL.equalsIgnoreCase(arg)
                || COMMANDS.FUTURE_ROLLBACK_SQL.equalsIgnoreCase(arg)
                || COMMANDS.FUTURE_ROLLBACK_COUNT_SQL.equalsIgnoreCase(arg)
                || COMMANDS.FUTURE_ROLLBACK_TO_TAG_SQL.equalsIgnoreCase(arg)
                || COMMANDS.UPDATE_TESTING_ROLLBACK.equalsIgnoreCase(arg)
                || COMMANDS.TAG.equalsIgnoreCase(arg)
                || COMMANDS.TAG_EXISTS.equalsIgnoreCase(arg)
                || COMMANDS.LIST_LOCKS.equalsIgnoreCase(arg)
                || COMMANDS.HISTORY.equalsIgnoreCase(arg)
                || COMMANDS.DROP_ALL.equalsIgnoreCase(arg)
                || COMMANDS.RELEASE_LOCKS.equalsIgnoreCase(arg)
                || COMMANDS.STATUS.equalsIgnoreCase(arg)
                || COMMANDS.UNEXPECTED_CHANGESETS.equalsIgnoreCase(arg)
                || COMMANDS.VALIDATE.equalsIgnoreCase(arg)
                || COMMANDS.HELP.equalsIgnoreCase(arg)
                || COMMANDS.DIFF.equalsIgnoreCase(arg)
                || COMMANDS.DIFF_CHANGELOG.equalsIgnoreCase(arg)
                || COMMANDS.GENERATE_CHANGELOG.equalsIgnoreCase(arg)
                || COMMANDS.SNAPSHOT.equalsIgnoreCase(arg)
                || COMMANDS.SNAPSHOT_REFERENCE.equalsIgnoreCase(arg)
                || COMMANDS.EXECUTE_SQL.equalsIgnoreCase(arg)
                || COMMANDS.CALCULATE_CHECKSUM.equalsIgnoreCase(arg)
                || COMMANDS.CLEAR_CHECKSUMS.equalsIgnoreCase(arg)
                || COMMANDS.DB_DOC.equalsIgnoreCase(arg)
                || COMMANDS.CHANGELOG_SYNC.equalsIgnoreCase(arg)
                || COMMANDS.CHANGELOG_SYNC_SQL.equalsIgnoreCase(arg)
                || COMMANDS.MARK_NEXT_CHANGESET_RAN.equalsIgnoreCase(arg)
                || COMMANDS.MARK_NEXT_CHANGESET_RAN_SQL.equalsIgnoreCase(arg)
                || COMMANDS.ROLLBACK_ONE_CHANGE_SET.equalsIgnoreCase(arg)
                || COMMANDS.ROLLBACK_ONE_CHANGE_SET_SQL.equalsIgnoreCase(arg)
                || COMMANDS.ROLLBACK_ONE_UPDATE.equalsIgnoreCase(arg)
                || COMMANDS.ROLLBACK_ONE_UPDATE_SQL.equalsIgnoreCase(arg);
    }

    /**
     * Returns true if the given main command arg needs no special parameters.
     *
     * @param arg the main command to test
     * @return true if arg is a valid main command and needs no special parameters, false in all other cases
     */
    private static boolean isNoArgCommand(String arg) {
        return COMMANDS.MIGRATE.equals(arg)
                || COMMANDS.MIGRATE_SQL.equalsIgnoreCase(arg)
                || COMMANDS.UPDATE.equalsIgnoreCase(arg)
                || COMMANDS.UPDATE_SQL.equalsIgnoreCase(arg)
                || COMMANDS.FUTURE_ROLLBACK_SQL.equalsIgnoreCase(arg)
                || COMMANDS.UPDATE_TESTING_ROLLBACK.equalsIgnoreCase(arg)
                || COMMANDS.LIST_LOCKS.equalsIgnoreCase(arg)
                || COMMANDS.DROP_ALL.equalsIgnoreCase(arg)
                || COMMANDS.RELEASE_LOCKS.equalsIgnoreCase(arg)
                || COMMANDS.VALIDATE.equalsIgnoreCase(arg)
                || COMMANDS.HELP.equalsIgnoreCase(arg)
                || COMMANDS.CLEAR_CHECKSUMS.equalsIgnoreCase(arg)
                || COMMANDS.CHANGELOG_SYNC.equalsIgnoreCase(arg)
                || COMMANDS.CHANGELOG_SYNC_SQL.equalsIgnoreCase(arg)
                || COMMANDS.MARK_NEXT_CHANGESET_RAN.equalsIgnoreCase(arg)
                || COMMANDS.MARK_NEXT_CHANGESET_RAN_SQL.equalsIgnoreCase(arg);
    }

    private static void addWarFileClasspathEntries(File classPathFile, List<URL> urls) throws IOException {
        URL jarUrl = new URL("jar:" + classPathFile.toURI().toURL() + "!/WEB-INF/classes/");
        LOG.info(LogType.LOG, "adding '" + jarUrl + "' to classpath");
        urls.add(jarUrl);

        try (
                JarFile warZip = new JarFile(classPathFile)
        ) {
            Enumeration<? extends JarEntry> entries = warZip.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("WEB-INF/lib")
                        && entry.getName().toLowerCase().endsWith(".jar")) {
                    File jar = extract(warZip, entry);
                    URL newUrl = new URL("jar:" + jar.toURI().toURL() + "!/");
                    LOG.info(LogType.LOG, "adding '" + newUrl + "' to classpath");
                    urls.add(newUrl);
                    jar.deleteOnExit();
                }
            }
        }
    }

    /**
     * Extract a single object from a JAR file into a temporary file.
     *
     * @param jar   the JAR file from which we will extract
     * @param entry the object inside the JAR file that to be extracted
     * @return a File object with the temporary file containing the extracted object
     * @throws IOException if an I/O problem occurs
     */
    private static File extract(JarFile jar, JarEntry entry) throws IOException {
        // expand to temp dir and add to list
        File tempFile = File.createTempFile("liquibase.tmp", null);
        // read from jar and write to the tempJar file
        try (
                BufferedInputStream inStream = new BufferedInputStream(jar.getInputStream(entry));
                BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(tempFile))
        ) {
            int status;
            while ((status = inStream.read()) != -1) {
                outStream.write(status);
            }
        }

        return tempFile;
    }

    /**
     * Search for both liquibase.properties (or whatever the name of the current
     * defaultsFile is) and the "local" variant liquibase.local.properties. The contents of the local
     * variant overwrite parameters with the same name in the regular properties file.
     *
     * @throws CommandLineParsingException if an error occurs during parsing
     */
    protected void parseDefaultPropertyFiles() throws CommandLineParsingException {
        File[] potentialPropertyFiles = new File[2];

        potentialPropertyFiles[0] = new File(defaultsFile);
        String localDefaultsPathName = defaultsFile.replaceFirst("(\\.[^\\.]+)$", ".local$1");
        potentialPropertyFiles[1] = new File(localDefaultsPathName);

        for (File potentialPropertyFile : potentialPropertyFiles) {

            try {
                if (potentialPropertyFile.exists()) {
                    parseDefaultPropertyFileFromFile(potentialPropertyFile);
                } else {
                    parseDefaultPropertyFileFromResource(potentialPropertyFile);
                }
            } catch (IOException e) {
                throw new CommandLineParsingException(e);
            }
        }
    }

    /**
     * Open a property file that is embedded as a Java resource and parse it.
     *
     * @param potentialPropertyFile location and file name of the property file
     * @throws IOException                 if the file cannot be opened
     * @throws CommandLineParsingException if an error occurs during parsing
     */
    private void parseDefaultPropertyFileFromResource(File potentialPropertyFile) throws IOException,
            CommandLineParsingException {
        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream
                (potentialPropertyFile.getAbsolutePath())) {
            if (resourceAsStream != null) {
                parsePropertiesFile(resourceAsStream);
            }
        }
    }

    /**
     * Open a regular property file (not embedded in a resource - use {@link #parseDefaultPropertyFileFromResource}
     * for that) and parse it.
     *
     * @param potentialPropertyFile path and file name to the the property file
     * @throws IOException                 if the file cannot be opened
     * @throws CommandLineParsingException if an error occurs during parsing
     */
    private void parseDefaultPropertyFileFromFile(File potentialPropertyFile) throws IOException,
            CommandLineParsingException {
        try (FileInputStream stream = new FileInputStream(potentialPropertyFile)) {
            parsePropertiesFile(stream);
        }
    }

    /**
     * On windows machines, it splits args on '=' signs.  Put it back like it was.
     */
    protected String[] fixupArgs(String[] args) {
        List<String> fixedArgs = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.startsWith("--") || arg.startsWith("-D")) && !arg.contains("=")) {
                String nextArg = null;
                if ((i + 1) < args.length) {
                    nextArg = args[i + 1];
                }
                if ((nextArg != null) && !nextArg.startsWith("--") && !isCommand(nextArg)) {
                    arg = arg + "=" + nextArg;
                    i++;
                }
            }

            // Sometimes, commas are still escaped as \, at this point, fix it:
            arg = arg.replace("\\,", ",");
            fixedArgs.add(arg);
        }

        return fixedArgs.toArray(new String[fixedArgs.size()]);
    }

    /**
     * After parsing, checks if the given combination of main command and can be executed.
     *
     * @return an empty List if successful, or a list of error messages
     */
    protected List<String> checkSetup() {
        List<String> messages = new ArrayList<>();
        if (command == null) {
            messages.add(coreBundle.getString("command.not.passed"));
        } else if (!isCommand(command)) {
            messages.add(String.format(coreBundle.getString("command.unknown"), command));
        } else {
            if (StringUtils.trimToNull(url) == null) {
                messages.add(String.format(coreBundle.getString("option.required"), "--" + OPTIONS.URL));
            }

            if (isChangeLogRequired(command) && (StringUtils.trimToNull(changeLogFile) == null)) {
                messages.add(String.format(coreBundle.getString("option.required"), "--" + OPTIONS.CHANGELOG_FILE));
            }

            if (isNoArgCommand(command) && !commandParams.isEmpty()) {
                messages.add(coreBundle.getString(ERRORMSG_UNEXPECTED_PARAMETERS) + commandParams);
            } else {
                validateCommandParameters(messages);
            }
        }
        return messages;
    }

    /**
     * Checks for unexpected (unknown) command line parameters and, if any problems are found,
     * returns the list of issues in String form.
     *
     * @param messages an array of Strings to which messages for issues found will be added
     */
    private void checkForUnexpectedCommandParameter(List<String> messages) {
        if (COMMANDS.UPDATE_COUNT.equalsIgnoreCase(command)
                || COMMANDS.UPDATE_COUNT_SQL.equalsIgnoreCase(command)
                || COMMANDS.UPDATE_TO_TAG.equalsIgnoreCase(command)
                || COMMANDS.UPDATE_TO_TAG_SQL.equalsIgnoreCase(command)
                || COMMANDS.CALCULATE_CHECKSUM.equalsIgnoreCase(command)
                || COMMANDS.DB_DOC.equalsIgnoreCase(command)
                || COMMANDS.TAG.equalsIgnoreCase(command)
                || COMMANDS.TAG_EXISTS.equalsIgnoreCase(command)) {

            if ((!commandParams.isEmpty()) && commandParams.iterator().next().startsWith("-")) {
                messages.add(coreBundle.getString(ERRORMSG_UNEXPECTED_PARAMETERS) + commandParams);
            }
        } else if (COMMANDS.STATUS.equalsIgnoreCase(command)
                || COMMANDS.UNEXPECTED_CHANGESETS.equalsIgnoreCase(command)) {
            if ((!commandParams.isEmpty())
                    && !commandParams.iterator().next().equalsIgnoreCase("--" + OPTIONS.VERBOSE)) {
                messages.add(coreBundle.getString(ERRORMSG_UNEXPECTED_PARAMETERS) + commandParams);
            }
        } else if (COMMANDS.DIFF.equalsIgnoreCase(command)
                || COMMANDS.DIFF_CHANGELOG.equalsIgnoreCase(command)) {
            if ((!commandParams.isEmpty())) {
                for (String cmdParm : commandParams) {
                    if (!cmdParm.startsWith("--" + OPTIONS.REFERENCE_USERNAME)
                            && !cmdParm.startsWith("--" + OPTIONS.REFERENCE_PASSWORD)
                            && !cmdParm.startsWith("--" + OPTIONS.REFERENCE_DRIVER)
                            && !cmdParm.startsWith("--" + OPTIONS.REFERENCE_DEFAULT_CATALOG_NAME)
                            && !cmdParm.startsWith("--" + OPTIONS.REFERENCE_DEFAULT_SCHEMA_NAME)
                            && !cmdParm.startsWith("--" + OPTIONS.INCLUDE_SCHEMA)
                            && !cmdParm.startsWith("--" + OPTIONS.INCLUDE_CATALOG)
                            && !cmdParm.startsWith("--" + OPTIONS.INCLUDE_TABLESPACE)
                            && !cmdParm.startsWith("--" + OPTIONS.SCHEMAS)
                            && !cmdParm.startsWith("--" + OPTIONS.OUTPUT_SCHEMAS_AS)
                            && !cmdParm.startsWith("--" + OPTIONS.REFERENCE_SCHEMAS)
                            && !cmdParm.startsWith("--" + OPTIONS.REFERENCE_URL)
                            && !cmdParm.startsWith("--" + OPTIONS.EXCLUDE_OBJECTS)
                            && !cmdParm.startsWith("--" + OPTIONS.INCLUDE_OBJECTS)
                            && !cmdParm.startsWith("--" + OPTIONS.DIFF_TYPES)
                            && !cmdParm.startsWith("--" + OPTIONS.FORMAT)
                            && !cmdParm.startsWith("--" + OPTIONS.HELP)
                            && !cmdParm.startsWith("--" + OPTIONS.SNAPSHOT_FORMAT)) {
                        messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                    }
                    if (COMMANDS.DIFF_CHANGELOG.equalsIgnoreCase(command) && cmdParm.startsWith("--" + OPTIONS.FORMAT)) {
                        messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                    }
                }
            }
        } else if ((COMMANDS.SNAPSHOT.equalsIgnoreCase(command)
                || COMMANDS.GENERATE_CHANGELOG.equalsIgnoreCase(command))
                && (!commandParams.isEmpty())) {
            for (String cmdParm : commandParams) {
                if (!cmdParm.startsWith("--" + OPTIONS.INCLUDE_SCHEMA)
                        && !cmdParm.startsWith("--" + OPTIONS.INCLUDE_CATALOG)
                        && !cmdParm.startsWith("--" + OPTIONS.INCLUDE_TABLESPACE)
                        && !cmdParm.startsWith("--" + OPTIONS.SCHEMAS)
                        && !cmdParm.startsWith("--" + OPTIONS.SNAPSHOT_FORMAT)
                        && !cmdParm.startsWith("--" + OPTIONS.DATA_OUTPUT_DIRECTORY)
                        && !cmdParm.startsWith("--" + OPTIONS.OUTPUT_SCHEMAS_AS)) {
                    messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                }
            }
        } else if (COMMANDS.ROLLBACK_ONE_CHANGE_SET.equalsIgnoreCase(command)) {
            for (String cmdParm : commandParams) {
                if (!cmdParm.startsWith("--" + OPTIONS.CHANGE_SET_ID)
                        && !cmdParm.startsWith("--" + OPTIONS.HELP)
                        && !cmdParm.startsWith("--" + OPTIONS.FORCE)
                        && !cmdParm.startsWith("--" + OPTIONS.CHANGE_SET_PATH)
                        && !cmdParm.startsWith("--" + OPTIONS.CHANGE_SET_AUTHOR)
                        && !cmdParm.startsWith("--" + OPTIONS.ROLLBACK_SCRIPT)) {
                    messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                }
            }
        } else if (COMMANDS.ROLLBACK_ONE_CHANGE_SET_SQL.equalsIgnoreCase(command)) {
            for (String cmdParm : commandParams) {
                if (!cmdParm.startsWith("--" + OPTIONS.CHANGE_SET_ID)
                        && !cmdParm.startsWith("--" + OPTIONS.HELP)
                        && !cmdParm.startsWith("--" + OPTIONS.FORCE)
                        && !cmdParm.startsWith("--" + OPTIONS.CHANGE_SET_PATH)
                        && !cmdParm.startsWith("--" + OPTIONS.CHANGE_SET_AUTHOR)
                        && !cmdParm.startsWith("--" + OPTIONS.ROLLBACK_SCRIPT)) {
                    messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                }
            }
        } else if (COMMANDS.ROLLBACK_ONE_UPDATE.equalsIgnoreCase(command)) {
            for (String cmdParm : commandParams) {
                if (!cmdParm.startsWith("--" + OPTIONS.DEPLOYMENT_ID)
                        && !cmdParm.startsWith("--" + OPTIONS.HELP)
                        && !cmdParm.startsWith("--" + OPTIONS.FORCE)) {
                    messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                }
            }
        } else if (COMMANDS.ROLLBACK_ONE_UPDATE_SQL.equalsIgnoreCase(command)) {
            for (String cmdParm : commandParams) {
                if (!cmdParm.startsWith("--" + OPTIONS.DEPLOYMENT_ID)
                        && !cmdParm.startsWith("--" + OPTIONS.HELP)
                        && !cmdParm.startsWith("--" + OPTIONS.FORCE)) {
                    messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                }
            }
        }
    }

    /**
     * Checks the command line for correctness and reports on unexpected, missing and/or malformed parameters.
     *
     * @param messages an array of Strings to which messages for issues found will be added
     */
    private void validateCommandParameters(final List<String> messages) {
        checkForUnexpectedCommandParameter(messages);
        checkForMissingCommandParameters(messages);
        checkForMalformedCommandParameters(messages);
    }

    /**
     * Checks for missing command line parameters and, if any problems are found,
     * returns the list of issues in String form.
     *
     * @param messages an array of Strings to which messages for issues found will be added
     */
    private void checkForMissingCommandParameters(final List<String> messages) {
        if ((commandParams.isEmpty() || commandParams.iterator().next().startsWith("-"))
                && (COMMANDS.CALCULATE_CHECKSUM.equalsIgnoreCase(command))) {
            messages.add(coreBundle.getString("changeset.identifier.missing"));
        }
    }

    /**
     * Checks for incorrectly written command line parameters and, if any problems are found,
     * returns the list of issues in String form.
     *
     * @param messages an array of Strings to which messages for issues found will be added
     */
    private void checkForMalformedCommandParameters(final List<String> messages) {
        if (commandParams.isEmpty()) {
            return;
        }

        final int CHANGESET_MINIMUM_IDENTIFIER_PARTS = 3;

        if (COMMANDS.CALCULATE_CHECKSUM.equalsIgnoreCase(command)) {
            for (final String param : commandParams) {
                if ((param != null) && !param.startsWith("-")) {
                    final String[] parts = param.split("::");
                    if (parts.length < CHANGESET_MINIMUM_IDENTIFIER_PARTS) {
                        messages.add(coreBundle.getString("changeset.identifier.must.have.form.filepath.id.author"));
                        break;
                    }
                }
            }
        } else if (COMMANDS.DIFF_CHANGELOG.equalsIgnoreCase(command) && (diffTypes != null) && diffTypes.toLowerCase
                ().contains("data")) {
            messages.add(String.format(coreBundle.getString("including.data.diffchangelog.has.no.effect"),
                    OPTIONS.DIFF_TYPES, COMMANDS.GENERATE_CHANGELOG
            ));
        }
    }

    /**
     * Reads various execution parameters from an InputStream and sets our internal state according to the values
     * found.
     *
     * @param propertiesInputStream an InputStream from a Java properties file
     * @throws IOException                 if there is a problem reading the InputStream
     * @throws CommandLineParsingException if an invalid property is encountered
     */
    protected void parsePropertiesFile(InputStream propertiesInputStream) throws IOException,
            CommandLineParsingException {
        Properties props = new Properties();
        props.load(propertiesInputStream);
        if (props.containsKey("strict")) {
            strict = Boolean.valueOf(props.getProperty("strict"));
        }

        for (Map.Entry entry : props.entrySet()) {
            try {
                if ("promptOnNonLocalDatabase".equals(entry.getKey())) {
                    continue;
                }
                if (((String) entry.getKey()).startsWith("parameter.")) {
                    changeLogParameters.put(((String) entry.getKey()).replaceFirst("^parameter.", ""), entry.getValue
                            ());
                } else {
                    Field field = getClass().getDeclaredField((String) entry.getKey());
                    Object currentValue = field.get(this);

                    if (currentValue == null) {
                        String value = entry.getValue().toString().trim();
                        if (field.getType().equals(Boolean.class)) {
                            field.set(this, Boolean.valueOf(value));
                        } else {
                            field.set(this, value);
                        }
                    }
                }
            } catch (NoSuchFieldException ignored) {
                if (strict) {
                    throw new CommandLineParsingException(
                            String.format(coreBundle.getString("parameter.unknown"), entry.getKey())
                    );
                } else {
                    LogService.getLog(getClass()).warning(
                            LogType.LOG, String.format(coreBundle.getString("parameter.ignored"), entry.getKey())
                    );
                }
            } catch (IllegalAccessException e) {
                throw new UnexpectedLiquibaseException(
                        String.format(coreBundle.getString("parameter.unknown"), entry.getKey())
                );
            }
        }
    }

    /**
     * If any errors have been found, print the list of errors first, then print the command line help text.
     *
     * @param errorMessages List of error messages
     * @param stream        the output stream to write the text to
     */
    protected void printHelp(List<String> errorMessages, PrintStream stream) {
        stream.println(coreBundle.getString("errors"));
        for (String message : errorMessages) {
            stream.println("  " + message);
        }
        stream.println();
    }

    /**
     * Print instructions on how to use this program from the command line.
     *
     * @param stream the output stream to write the help text to
     */
    protected void printHelp(PrintStream stream) {
        Main main = null;
        main = new Main();
        this.logLevel = Level.ERROR.toString();
        main.reconfigureLogging();
        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.ERROR);
        stream.println(CommandLineUtils.getBanner());
        String helpText = commandLineHelpBundle.getString("commandline-helptext");
        stream.println(helpText);
    }

    /**
     * Check the string for known characters which cannot be handled
     *
     * @param arg Input parameter to check
     * @return int             A CodePointCheck object, or null to indicate all good
     */
    protected static CodePointCheck checkArg(String arg) {
        char[] chars = arg.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            for (int j = 0; j < suspiciousCodePoints.length; j++) {
                if (suspiciousCodePoints[j] == chars[i]) {
                    CodePointCheck codePointCheck = new CodePointCheck();
                    codePointCheck.position = i;
                    codePointCheck.ch = chars[i];
                    return codePointCheck;
                }
            }
        }
        return null;
    }

    /**
     * Parses the command line options. If an invalid argument is given, a CommandLineParsingException is thrown.
     *
     * @param paramArgs the arguments to parse
     * @throws CommandLineParsingException thrown if an invalid argument is passed
     */
    protected void parseOptions(String[] paramArgs) throws CommandLineParsingException {
        String[] args = fixupArgs(paramArgs);

        boolean seenCommand = false;
        for (String arg : args) {

            if (isCommand(arg)) {
                this.command = arg;
                if (this.command.equalsIgnoreCase(COMMANDS.MIGRATE)) {
                    this.command = COMMANDS.UPDATE;
                } else if (this.command.equalsIgnoreCase(COMMANDS.MIGRATE_SQL)) {
                    this.command = COMMANDS.UPDATE_SQL;
                }
                seenCommand = true;
            } else if (seenCommand) {
                // ChangeLog parameter:
                if (arg.startsWith("-D")) {
                    String[] splitArg = splitArg(arg);

                    String attributeName = splitArg[0].replaceFirst("^-D", "");
                    String value = splitArg[1];

                    changeLogParameters.put(attributeName, value);
                } else {
                    commandParams.add(arg);
                    if (arg.startsWith("--")) {
                        parseOptionArgument(arg, true);
                    }
                }
            } else if (arg.startsWith("--")) {
                parseOptionArgument(arg, false);
            } else {
                throw new CommandLineParsingException(
                        String.format(coreBundle.getString("unexpected.value"), arg));
            }
        }

        // Now apply default values from the default property files. We waited with this until this point
        // since command line parameters might have changed the location where we will look for them.
        parseDefaultPropertyFiles();
    }

    /**
     * Parses an option ("--someOption") from the command line
     *
     * @param arg the option to parse (including the "--")
     * @throws CommandLineParsingException if a problem occurs
     */
    private void parseOptionArgument(String arg, boolean okIfNotAField) throws CommandLineParsingException {
        final String PROMPT_FOR_VALUE = "PROMPT";

        if (arg.toLowerCase().startsWith("--" + OPTIONS.VERBOSE) ||
            arg.toLowerCase().startsWith("--" + OPTIONS.HELP)) {
            return;
        }

        if (arg.toLowerCase().equals("--" + OPTIONS.FORCE) || arg.toLowerCase().equals("--" + OPTIONS.HELP)) {
            arg = arg + "=true";
        }

        String[] splitArg = splitArg(arg);

        String attributeName = splitArg[0];
        String value = splitArg[1];

        if (PROMPT_FOR_VALUE.equalsIgnoreCase(StringUtils.trimToEmpty(value))) {
            Console c = System.console();
            if (c == null) {
                throw new CommandLineParsingException(
                        String.format(MessageFormat.format(coreBundle.getString(
                                "cannot.prompt.for.the.value.no.console"), attributeName))
                );
            }
            //Prompt for value
            if (attributeName.toLowerCase().contains("password")) {
                value = new String(c.readPassword(attributeName + ": "));
            } else {
                value = c.readLine(attributeName + ": ");
            }
        }

        try {
            Field field = getClass().getDeclaredField(attributeName);
            if (field.getType().equals(Boolean.class)) {
                field.set(this, Boolean.valueOf(value));
            } else {
                field.set(this, value);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            if (!okIfNotAField) {
                throw new CommandLineParsingException(
                        String.format(coreBundle.getString("option.unknown"), attributeName)
                );
            }
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    /**
     * Set (hopefully) sensible defaults for command line parameters
     */
    protected void applyDefaults() {
        if (this.promptForNonLocalDatabase == null) {
            this.promptForNonLocalDatabase = Boolean.FALSE;
        }
        if (this.logLevel == null) {
            this.logLevel = "off";
        }
        if (this.includeSystemClasspath == null) {
            this.includeSystemClasspath = Boolean.TRUE;
        }

        if (this.outputDefaultCatalog == null) {
            this.outputDefaultCatalog = "true";
        }
        if (this.outputDefaultSchema == null) {
            this.outputDefaultSchema = "true";
        }
        if (this.defaultsFile == null) {
            this.defaultsFile = "liquibase.properties";
        }
        if (this.includeSchema == null) {
            this.includeSchema = false;
        }
        if (this.includeCatalog == null) {
            this.includeCatalog = false;
        }
        if (this.includeTablespace == null) {
            this.includeTablespace = false;
        }

    }

    protected void configureClassLoader() throws CommandLineParsingException {
        final List<URL> urls = new ArrayList<>();
        if (this.classpath != null) {
            String[] classpathSoFar;
            if (isWindows()) {
                classpathSoFar = this.classpath.split(";");
            } else {
                classpathSoFar = this.classpath.split(":");
            }

            for (String classpathEntry : classpathSoFar) {
                File classPathFile = new File(classpathEntry);
                if (!classPathFile.exists()) {
                    throw new CommandLineParsingException(
                            String.format(coreBundle.getString("does.not.exist"), classPathFile.getAbsolutePath()));
                }

                if (classpathEntry.endsWith(FILE_SUFFIXES.WAR_FILE_SUFFIX)) {
                    try {
                        addWarFileClasspathEntries(classPathFile, urls);
                    } catch (IOException e) {
                        throw new CommandLineParsingException(e);
                    }
                } else if (classpathEntry.endsWith(FILE_SUFFIXES.FILE_SUFFIX_EAR)) {
                    try (JarFile earZip = new JarFile(classPathFile)) {
                        Enumeration<? extends JarEntry> entries = earZip.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.getName().toLowerCase().endsWith(".jar")) {
                                File jar = extract(earZip, entry);
                                URL newUrl = new URL("jar:" + jar.toURI().toURL() + "!/");
                                urls.add(newUrl);
                                LOG.debug(LogType.LOG,
                                        String.format(coreBundle.getString("adding.to.classpath"), newUrl));
                                jar.deleteOnExit();
                            } else if (entry.getName().toLowerCase().endsWith("war")) {
                                File warFile = extract(earZip, entry);
                                addWarFileClasspathEntries(warFile, urls);
                            }
                        }
                    } catch (IOException e) {
                        throw new CommandLineParsingException(e);
                    }

                } else {
                    URL newUrl = null;
                    try {
                        newUrl = new File(classpathEntry).toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new CommandLineParsingException(e);
                    }
                    LOG.debug(LogType.LOG, String.format(coreBundle.getString("adding.to.classpath"), newUrl));
                    urls.add(newUrl);
                }
            }
        }
        if (includeSystemClasspath) {
            classLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                @Override
                public URLClassLoader run() {
                    return new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread()
                            .getContextClassLoader());
                }
            });

        } else {
            classLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                @Override
                public URLClassLoader run() {
                    return new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
                }
            });
        }

        ServiceLocator.getInstance().setResourceAccessor(new ClassLoaderResourceAccessor(classLoader));
        Thread.currentThread().setContextClassLoader(classLoader);
    }


    /**
     * Do the actual database migration, i.e. apply the ChangeSets.
     *
     * @throws Exception
     */
    protected void doMigration() throws Exception {
        if (COMMANDS.HELP.equalsIgnoreCase(command)) {
            printHelp(System.err);
            return;
        }

        //
        // Check for a valid license to run PRO commands
        //
        String formatValue = getCommandParam(OPTIONS.FORMAT, null);
        if (isLicenseableCommand(formatValue)) {
            if (isFormattedDiff()) {
                if (formatValue != null && ! formatValue.equalsIgnoreCase("json")) {
                    String messageString =
                        "\nWARNING: The diff command optional Pro parameter '--format' " +
                        "currently supports only 'TXT' or 'JSON' as values.  (Blank defaults to 'TXT')";
                    throw new LiquibaseException(String.format(messageString));
                }
            }
            if (!commandParams.contains("--help") && !liquibaseProLicenseValid) {
                String warningAboutCommand = command;
                if (command.equalsIgnoreCase(COMMANDS.DIFF) && formatValue != null && !formatValue.isEmpty()) {
                    warningAboutCommand = "diff --format=" + formatValue;
                }
                String messageString = String.format(coreBundle.getString("no.pro.license.found"), warningAboutCommand);
                throw new LiquibaseException(messageString);
            }
        }

        try {
//            if (null != logFile) {
//                LogService.getLog(getClass()).setLogLevel(logLevel, logFile);
//            } else {
//                LogService.getLog(getClass()).setLogLevel(logLevel);
//            }
        } catch (IllegalArgumentException e) {
            throw new CommandLineParsingException(e.getMessage(), e);
        }

        FileSystemResourceAccessor fsOpener = new FileSystemResourceAccessor();
        CommandLineResourceAccessor clOpener = new CommandLineResourceAccessor(classLoader);
        CompositeResourceAccessor fileOpener = new CompositeResourceAccessor(fsOpener, clOpener);

        Database database = null;
        if (this.url != null) {
            database = CommandLineUtils.createDatabaseObject(fileOpener, this.url,
                    this.username, this.password, this.driver, this.defaultCatalogName, this.defaultSchemaName,
                    Boolean.parseBoolean(outputDefaultCatalog), Boolean.parseBoolean(outputDefaultSchema),
                    this.databaseClass, this.driverPropertiesFile, this.propertyProviderClass,
                    this.liquibaseCatalogName, this.liquibaseSchemaName, this.databaseChangeLogTableName,
                    this.databaseChangeLogLockTableName);
            database.setLiquibaseTablespaceName(this.databaseChangeLogTablespaceName);
        }

        try {
            if ((excludeObjects != null) && (includeObjects != null)) {
                throw new UnexpectedLiquibaseException(
                        String.format(coreBundle.getString("cannot.specify.both"),
                                OPTIONS.EXCLUDE_OBJECTS, OPTIONS.INCLUDE_OBJECTS));
            }

            //
            // Set the global configuration option based on presence of the dataOutputDirectory
            //
            boolean b = dataOutputDirectory != null;
            LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).setShouldSnapshotData(b);

            ObjectChangeFilter objectChangeFilter = null;
            CompareControl.ComputedSchemas computedSchemas = CompareControl.computeSchemas(
                    schemas,
                    referenceSchemas,
                    outputSchemasAs,
                    defaultCatalogName, defaultSchemaName,
                    referenceDefaultCatalogName, referenceDefaultSchemaName,
                    database);

            CompareControl.SchemaComparison[] finalSchemaComparisons = computedSchemas.finalSchemaComparisons;
            DiffOutputControl diffOutputControl = new DiffOutputControl(
                    includeCatalog, includeSchema, includeTablespace, finalSchemaComparisons);

            if (excludeObjects != null) {
                objectChangeFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE,
                        excludeObjects);
                diffOutputControl.setObjectChangeFilter(objectChangeFilter);
            }

            if (includeObjects != null) {
                objectChangeFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE,
                        includeObjects);
                diffOutputControl.setObjectChangeFilter(objectChangeFilter);
            }

            for (CompareControl.SchemaComparison schema : finalSchemaComparisons) {
                diffOutputControl.addIncludedSchema(schema.getReferenceSchema());
                diffOutputControl.addIncludedSchema(schema.getComparisonSchema());
            }

            if (COMMANDS.DIFF.equalsIgnoreCase(command)) {
                if (commandParams.contains("--help")) {
                    System.out.println("liquibase diff" +
                            "\n" +
                            "          Outputs a description of differences.  If you have a Liquibase Pro key, you can output the differences as JSON using the --format=JSON option\n");
                    System.exit(0);
                }
                if (isFormattedDiff()) {
                    LiquibaseCommand liquibaseCommand = CommandFactory.getInstance().getCommand(COMMANDS.FORMATTED_DIFF);
                    DiffCommand diffCommand = CommandLineUtils.createDiffCommand(
                            createReferenceDatabaseFromCommandParams(commandParams, fileOpener),
                            database,
                            StringUtils.trimToNull(diffTypes), finalSchemaComparisons, objectChangeFilter, new PrintStream(getOutputStream()));
                    Map<String, Object> argsMap = new HashMap<String, Object>();
                    argsMap.put("format", getCommandParam(OPTIONS.FORMAT, "JSON"));
                    argsMap.put("diffCommand", diffCommand);
                    ((AbstractSelfConfiguratingCommand) liquibaseCommand).configure(argsMap);
                    liquibaseCommand.execute();
                } else {
                    CommandLineUtils.doDiff(
                            createReferenceDatabaseFromCommandParams(commandParams, fileOpener),
                            database,
                            StringUtils.trimToNull(diffTypes), finalSchemaComparisons, objectChangeFilter, new PrintStream(getOutputStream()));
                }
                return;
            } else if (COMMANDS.DIFF_CHANGELOG.equalsIgnoreCase(command)) {
                CommandLineUtils.doDiffToChangeLog(changeLogFile,
                        createReferenceDatabaseFromCommandParams(commandParams, fileOpener),
                        database,
                        diffOutputControl, objectChangeFilter, StringUtils.trimToNull(diffTypes), finalSchemaComparisons
                );
                return;
            } else if (COMMANDS.GENERATE_CHANGELOG.equalsIgnoreCase(command)) {
                String currentChangeLogFile = this.changeLogFile;
                if (currentChangeLogFile == null) {
                    //will output to stdout:
                    currentChangeLogFile = "";
                }

                File file = new File(currentChangeLogFile);
                if (file.exists() && (!Boolean.parseBoolean(overwriteOutputFile))) {
                    throw new LiquibaseException(
                            String.format(coreBundle.getString("changelogfile.already.exists"), currentChangeLogFile));
                } else {
                    try {
                        if (!file.delete()) {
                            // Nothing needs to be done
                        }
                    } catch (SecurityException e) {
                        throw new LiquibaseException(
                                String.format(coreBundle.getString("attempt.to.delete.the.file.failed.cannot.continue"),
                                        currentChangeLogFile
                                ), e
                        );
                    }
                }

                CatalogAndSchema[] finalTargetSchemas = computedSchemas.finalTargetSchemas;
                CommandLineUtils.doGenerateChangeLog(currentChangeLogFile, database, finalTargetSchemas,
                        StringUtils.trimToNull(diffTypes), StringUtils.trimToNull(changeSetAuthor),
                        StringUtils.trimToNull(changeSetContext), StringUtils.trimToNull(dataOutputDirectory),
                        diffOutputControl);
                return;
            } else if (COMMANDS.SNAPSHOT.equalsIgnoreCase(command)) {
                SnapshotCommand snapshotCommand = (SnapshotCommand) CommandFactory.getInstance()
                        .getCommand(COMMANDS.SNAPSHOT);
                snapshotCommand.setDatabase(database);
                snapshotCommand.setSchemas(getSchemaParams(database));
                snapshotCommand.setSerializerFormat(getCommandParam(OPTIONS.SNAPSHOT_FORMAT, null));
                Writer outputWriter = getOutputWriter();
                String result = snapshotCommand.execute().print();
                outputWriter.write(result);
                outputWriter.flush();
                outputWriter.close();
                return;
            } else if (COMMANDS.EXECUTE_SQL.equalsIgnoreCase(command)) {
                ExecuteSqlCommand executeSqlCommand = (ExecuteSqlCommand) CommandFactory.getInstance().getCommand(
                        COMMANDS.EXECUTE_SQL);
                executeSqlCommand.setDatabase(database);
                executeSqlCommand.setSql(getCommandParam("sql", null));
                executeSqlCommand.setSqlFile(getCommandParam("sqlFile", null));
                executeSqlCommand.setDelimiter(getCommandParam("delimiter", ";"));
                Writer outputWriter = getOutputWriter();
                outputWriter.write(executeSqlCommand.execute().print());
                outputWriter.flush();
                outputWriter.close();
                return;
            } else if (COMMANDS.SNAPSHOT_REFERENCE.equalsIgnoreCase(command)) {
                SnapshotCommand snapshotCommand = (SnapshotCommand) CommandFactory.getInstance()
                        .getCommand(COMMANDS.SNAPSHOT);
                Database referenceDatabase = createReferenceDatabaseFromCommandParams(commandParams, fileOpener);
                snapshotCommand.setDatabase(referenceDatabase);
                snapshotCommand.setSchemas(getSchemaParams(database));
                snapshotCommand.setSerializerFormat(getCommandParam(OPTIONS.SNAPSHOT_FORMAT, null));
                Writer outputWriter = getOutputWriter();
                outputWriter.write(snapshotCommand.execute().print());
                outputWriter.flush();
                outputWriter.close();

                return;
            }

            Liquibase liquibase = new Liquibase(changeLogFile, fileOpener, database);
            ChangeExecListener listener = ChangeExecListenerUtils.getChangeExecListener(
                    liquibase.getDatabase(), liquibase.getResourceAccessor(),
                    changeExecListenerClass, changeExecListenerPropertiesFile);
            liquibase.setChangeExecListener(listener);

            if (database != null) {
                database.setCurrentDateTimeFunction(currentDateTimeFunction);
            }
            for (Map.Entry<String, Object> entry : changeLogParameters.entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }

            if (COMMANDS.LIST_LOCKS.equalsIgnoreCase(command)) {
                liquibase.reportLocks(System.err);
                return;
            } else if (COMMANDS.RELEASE_LOCKS.equalsIgnoreCase(command)) {
                LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                lockService.forceReleaseLock();
                LogService.getLog(getClass()).info(LogType.USER_MESSAGE, String.format(
                        coreBundle.getString("successfully.released.database.change.log.locks"),
                        liquibase.getDatabase().getConnection().getConnectionUserName() +
                                "@" + liquibase.getDatabase().getConnection().getURL()
                        )
                );
                return;
            } else if (COMMANDS.TAG.equalsIgnoreCase(command)) {
                liquibase.tag(getCommandArgument());
                LogService.getLog(getClass()).info(
                        LogType.USER_MESSAGE, String.format(
                                coreBundle.getString("successfully.tagged"), liquibase.getDatabase()
                                        .getConnection().getConnectionUserName() + "@" +
                                        liquibase.getDatabase().getConnection().getURL()
                        )
                );
                return;
            } else if (COMMANDS.TAG_EXISTS.equalsIgnoreCase(command)) {
                String tag = commandParams.iterator().next();
                boolean exists = liquibase.tagExists(tag);
                if (exists) {
                    LogService.getLog(getClass()).info(
                            LogType.USER_MESSAGE, String.format(coreBundle.getString("tag.exists"), tag,
                                    liquibase.getDatabase().getConnection().getConnectionUserName() + "@" +
                                            liquibase.getDatabase().getConnection().getURL()
                            )
                    );
                } else {
                    LogService.getLog(getClass()).info(
                            LogType.USER_MESSAGE, String.format(coreBundle.getString("tag.does.not.exist"), tag,
                                    liquibase.getDatabase().getConnection().getConnectionUserName() + "@" +
                                            liquibase.getDatabase().getConnection().getURL()
                            )
                    );
                }
                return;
            } else if (COMMANDS.ROLLBACK_ONE_CHANGE_SET.equals(command)) {
                Map<String, Object> argsMap = new HashMap<>();
                loadChangeSetInfoToMap(argsMap);
                LiquibaseCommand liquibaseCommand = createLiquibaseCommand(database, liquibase, COMMANDS.ROLLBACK_ONE_CHANGE_SET, argsMap);
                liquibaseCommand.execute();
                return;
            } else if (COMMANDS.ROLLBACK_ONE_CHANGE_SET_SQL.equals(command)) {
                Writer outputWriter = getOutputWriter();
                Map<String, Object> argsMap = new HashMap<>();
                loadChangeSetInfoToMap(argsMap);
                argsMap.put("outputWriter", outputWriter);
                argsMap.put("force", true);
                LiquibaseCommand liquibaseCommand = createLiquibaseCommand(database, liquibase, COMMANDS.ROLLBACK_ONE_CHANGE_SET, argsMap);
                liquibaseCommand.execute();
                outputWriter.flush();
                outputWriter.close();
                return;
            } else if (COMMANDS.ROLLBACK_ONE_UPDATE.equals(command)) {
                Map<String, Object> argsMap = new HashMap<>();
                argsMap.put("deploymentId", getCommandParam(OPTIONS.DEPLOYMENT_ID, null));
                LiquibaseCommand liquibaseCommand = createLiquibaseCommand(database, liquibase, COMMANDS.ROLLBACK_ONE_UPDATE, argsMap);
                liquibaseCommand.execute();
                return;
            } else if (COMMANDS.ROLLBACK_ONE_UPDATE_SQL.equals(command)) {
                Writer outputWriter = getOutputWriter();
                Map<String, Object> argsMap = new HashMap<>();
                argsMap.put("deploymentId", getCommandParam(OPTIONS.DEPLOYMENT_ID, null));
                argsMap.put("outputWriter", outputWriter);
                argsMap.put("force", true);
                LiquibaseCommand liquibaseCommand = createLiquibaseCommand(database, liquibase, COMMANDS.ROLLBACK_ONE_UPDATE, argsMap);
                liquibaseCommand.execute();
                outputWriter.flush();
                outputWriter.close();
                return;
            } else if (COMMANDS.DROP_ALL.equals(command)) {
                DropAllCommand dropAllCommand = (DropAllCommand) CommandFactory.getInstance().getCommand
                        (COMMANDS.DROP_ALL);
                dropAllCommand.setDatabase(liquibase.getDatabase());
                dropAllCommand.setSchemas(getSchemaParams(database));
                LogService.getLog(getClass()).info(LogType.USER_MESSAGE, dropAllCommand.execute().print());
                return;
            } else if (COMMANDS.STATUS.equalsIgnoreCase(command)) {
                boolean runVerbose = false;

                if (commandParams.contains("--" + OPTIONS.VERBOSE)) {
                    runVerbose = true;
                }
                liquibase.reportStatus(runVerbose, new Contexts(contexts), new LabelExpression(labels),
                        getOutputWriter());
                return;
            } else if (COMMANDS.UNEXPECTED_CHANGESETS.equalsIgnoreCase(command)) {
                boolean runVerbose = false;

                if (commandParams.contains("--" + OPTIONS.VERBOSE)) {
                    runVerbose = true;
                }
                liquibase.reportUnexpectedChangeSets(runVerbose, contexts, getOutputWriter());
                return;
            } else if (COMMANDS.VALIDATE.equalsIgnoreCase(command)) {
                try {
                    liquibase.validate();
                } catch (ValidationFailedException e) {
                    e.printDescriptiveError(System.err);
                    return;
                }
                LogService.getLog(getClass()).info(
                        LogType.USER_MESSAGE, coreBundle.getString("no.validation.errors.found"));
                return;
            } else if (COMMANDS.CLEAR_CHECKSUMS.equalsIgnoreCase(command)) {
                liquibase.clearCheckSums();
                return;
            } else if (COMMANDS.CALCULATE_CHECKSUM.equalsIgnoreCase(command)) {
                CheckSum checkSum = null;
                checkSum = liquibase.calculateCheckSum(commandParams.iterator().next());
                LogService.getLog(getClass()).info(LogType.USER_MESSAGE, checkSum.toString());
                return;
            } else if (COMMANDS.DB_DOC.equalsIgnoreCase(command)) {
                if (commandParams.isEmpty()) {
                    throw new CommandLineParsingException(coreBundle.getString("dbdoc.requires.output.directory"));
                }
                if (changeLogFile == null) {
                    throw new CommandLineParsingException(coreBundle.getString("dbdoc.requires.changelog.parameter"));
                }
                liquibase.generateDocumentation(commandParams.iterator().next(), contexts);
                return;
            }

            try {
                if (COMMANDS.UPDATE.equalsIgnoreCase(command)) {
                    liquibase.update(new Contexts(contexts), new LabelExpression(labels));
                } else if (COMMANDS.CHANGELOG_SYNC.equalsIgnoreCase(command)) {
                    liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(labels));
                } else if (COMMANDS.CHANGELOG_SYNC_SQL.equalsIgnoreCase(command)) {
                    liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if (COMMANDS.MARK_NEXT_CHANGESET_RAN.equalsIgnoreCase(command)) {
                    liquibase.markNextChangeSetRan(new Contexts(contexts), new LabelExpression(labels));
                } else if (COMMANDS.MARK_NEXT_CHANGESET_RAN_SQL.equalsIgnoreCase(command)) {
                    liquibase.markNextChangeSetRan(new Contexts(contexts), new LabelExpression(labels),
                            getOutputWriter());
                } else if (COMMANDS.UPDATE_COUNT.equalsIgnoreCase(command)) {
                    liquibase.update(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts), new
                            LabelExpression(labels));
                } else if (COMMANDS.UPDATE_COUNT_SQL.equalsIgnoreCase(command)) {
                    liquibase.update(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts), new
                            LabelExpression(labels), getOutputWriter());
                } else if (COMMANDS.UPDATE_TO_TAG.equalsIgnoreCase(command)) {
                    if ((commandParams == null) || commandParams.isEmpty()) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.tag"), COMMANDS.UPDATE_TO_TAG));
                    }

                    liquibase.update(commandParams.iterator().next(), new Contexts(contexts), new LabelExpression
                            (labels));
                } else if (COMMANDS.UPDATE_TO_TAG_SQL.equalsIgnoreCase(command)) {
                    if ((commandParams == null) || commandParams.isEmpty()) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.tag"),
                                        COMMANDS.UPDATE_TO_TAG_SQL));
                    }

                    liquibase.update(commandParams.iterator().next(), new Contexts(contexts), new LabelExpression
                            (labels), getOutputWriter());
                } else if (COMMANDS.UPDATE_SQL.equalsIgnoreCase(command)) {
                    liquibase.update(new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if (COMMANDS.ROLLBACK.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.tag"), COMMANDS.ROLLBACK));
                    }
                    liquibase.rollback(getCommandArgument(), getCommandParam(COMMANDS.ROLLBACK_SCRIPT, null), new
                            Contexts(contexts), new LabelExpression(labels));
                } else if (COMMANDS.ROLLBACK_TO_DATE.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.timestamp"),
                                        COMMANDS.ROLLBACK_TO_DATE));
                    }
                    liquibase.rollback(new ISODateFormat().parse(getCommandArgument()), getCommandParam
                            (COMMANDS.ROLLBACK_SCRIPT, null), new Contexts(contexts), new LabelExpression(labels));
                } else if (COMMANDS.ROLLBACK_COUNT.equalsIgnoreCase(command)) {
                    liquibase.rollback(Integer.parseInt(getCommandArgument()), getCommandParam
                            (COMMANDS.ROLLBACK_SCRIPT, null), new Contexts(contexts), new LabelExpression(labels));

                } else if (COMMANDS.ROLLBACK_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.tag"),
                                        COMMANDS.ROLLBACK_SQL));
                    }
                    liquibase.rollback(getCommandArgument(), getCommandParam(COMMANDS.ROLLBACK_SCRIPT, null), new
                            Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if (COMMANDS.ROLLBACK_TO_DATE_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.timestamp"),
                                        COMMANDS.ROLLBACK_TO_DATE_SQL));
                    }
                    liquibase.rollback(new ISODateFormat().parse(getCommandArgument()), getCommandParam
                                    (COMMANDS.ROLLBACK_SCRIPT, null), new Contexts(contexts), new LabelExpression
                                    (labels),
                            getOutputWriter());
                } else if (COMMANDS.ROLLBACK_COUNT_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.count"),
                                        COMMANDS.ROLLBACK_COUNT_SQL));
                    }

                    liquibase.rollback(Integer.parseInt(getCommandArgument()), getCommandParam
                                    (COMMANDS.ROLLBACK_SCRIPT, null), new Contexts(contexts),
                            new LabelExpression(labels),
                            getOutputWriter()
                    );
                } else if (COMMANDS.FUTURE_ROLLBACK_SQL.equalsIgnoreCase(command)) {
                    liquibase.futureRollbackSQL(new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if (COMMANDS.FUTURE_ROLLBACK_COUNT_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.count"),
                                        COMMANDS.FUTURE_ROLLBACK_COUNT_SQL));
                    }

                    liquibase.futureRollbackSQL(Integer.parseInt(getCommandArgument()), new Contexts(contexts), new
                            LabelExpression(labels), getOutputWriter());
                } else if (COMMANDS.FUTURE_ROLLBACK_FROM_TAG_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.tag"),
                                        COMMANDS.FUTURE_ROLLBACK_FROM_TAG_SQL));
                    }

                    liquibase.futureRollbackSQL(getCommandArgument(), new Contexts(contexts), new LabelExpression
                            (labels), getOutputWriter());
                } else if (COMMANDS.UPDATE_TESTING_ROLLBACK.equalsIgnoreCase(command)) {
                    liquibase.updateTestingRollback(new Contexts(contexts), new LabelExpression(labels));
                } else if (COMMANDS.HISTORY.equalsIgnoreCase(command)) {
                    HistoryCommand historyCommand = (HistoryCommand) CommandFactory.getInstance().getCommand("history");
                    historyCommand.setDatabase(database);
                    historyCommand.setOutputStream(new PrintStream(getOutputStream()));
                    historyCommand.execute();
                } else {
                    throw new CommandLineParsingException(
                            String.format(coreBundle.getString("command.unknown"), command));
                }
            } catch (ParseException ignored) {
                throw new CommandLineParsingException(
                        coreBundle.getString("timeformat.invalid"));
            }
        } finally {
            try {
                if (database != null) {
                    database.rollback();
                    database.close();
                }
            } catch (DatabaseException e) {
                LogService.getLog(getClass()).warning(
                        LogType.LOG, coreBundle.getString("problem.closing.connection"), e);
            }
        }
    }

    private boolean isLicenseableCommand(String formatValue) {
        return COMMANDS.ROLLBACK_ONE_CHANGE_SET.equalsIgnoreCase(command) ||
               COMMANDS.ROLLBACK_ONE_CHANGE_SET_SQL.equalsIgnoreCase(command) ||
               COMMANDS.ROLLBACK_ONE_UPDATE.equalsIgnoreCase(command) ||
               COMMANDS.ROLLBACK_ONE_UPDATE_SQL.equalsIgnoreCase(command) ||
               (COMMANDS.DIFF.equalsIgnoreCase(command) && formatValue != null && ! formatValue.toLowerCase().equals("txt"));
    }

    private void loadChangeSetInfoToMap(Map<String, Object> argsMap) throws CommandLineParsingException {
        argsMap.put("changeSetId", getCommandParam(OPTIONS.CHANGE_SET_ID, null));
        argsMap.put("changeSetAuthor", getCommandParam(OPTIONS.CHANGE_SET_AUTHOR, null));
        argsMap.put("changeSetPath", getCommandParam(OPTIONS.CHANGE_SET_PATH, null));
    }

    private boolean isFormattedDiff() throws CommandLineParsingException {
        String formatValue = getCommandParam(OPTIONS.FORMAT, "txt");
        return ! formatValue.equalsIgnoreCase("txt") && ! formatValue.isEmpty();
    }

    private String getSchemaParams(Database database) throws CommandLineParsingException {
        String schemaParams = getCommandParam(OPTIONS.SCHEMAS, schemas);
        if (schemaParams == null || schemaParams.isEmpty()) {
            return database.getDefaultSchema().getSchemaName();
        }
        return schemaParams;
    }

    private LiquibaseCommand createLiquibaseCommand(Database database, Liquibase liquibase, String commandName, Map<String, Object> argsMap)
            throws LiquibaseException {
        argsMap.put("rollbackScript", rollbackScript);
        argsMap.put("changeLogFile", changeLogFile);
        argsMap.put("database", database);
        argsMap.put("liquibase", liquibase);
        if (!commandParams.contains("--help")) {
            argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        }
        argsMap.put("resourceAccessor", liquibase.getResourceAccessor());
        ChangeLogParameters clp = new ChangeLogParameters(database);
        for (Map.Entry<String, Object> entry : changeLogParameters.entrySet()) {
            clp.set(entry.getKey(), entry.getValue());
        }
        argsMap.put("changeLogParameters", clp);

        if (this.commandParams.contains("--force")) {
            argsMap.put("force", true);
        }
        if (this.commandParams.contains("--help")) {
            argsMap.put("help", true);
        }
        LiquibaseCommand liquibaseCommand = CommandFactory.getInstance().getCommand(commandName);
        AbstractSelfConfiguratingCommand configuratingCommand = (AbstractSelfConfiguratingCommand) liquibaseCommand;
        configuratingCommand.configure(argsMap);
        return liquibaseCommand;
    }

    /**
     * Return the first "parameter" from the command line that does NOT have the form of parameter=value. A parameter
     * is a command line argument that follows the main action (e.g. update/rollback/...). Example:
     * For the main action "updateToTagSQL &lt;tag&gt;", &lt;tag&gt; would be the command argument.
     *
     * @return the command argument, if one is given. Otherwise null.
     */
    private String getCommandArgument() {
        for (String param : commandParams) {
            if (!param.contains("=")) {
                return param;
            }
        }

        return null;
    }

    /**
     * Returns the value for a command line parameter of the form parameterName=value, or defaultValue if that
     * parameter has not been specified by the user.
     *
     * @param paramName    name of the parameter
     * @param defaultValue return value if parameter is not given
     * @return the user-specified value for paramName, or defaultValue
     * @throws CommandLineParsingException if a parameter on the command line is un-parsable
     */
    private String getCommandParam(String paramName, String defaultValue) throws CommandLineParsingException {
        for (String param : commandParams) {
            if (!param.contains("=")) {
                continue;
            }
            String[] splitArg = splitArg(param);

            String attributeName = splitArg[0];
            String value = splitArg[1];
            if (attributeName.equalsIgnoreCase(paramName)) {
                return value;
            }
        }

        return defaultValue;
    }

    private Database createReferenceDatabaseFromCommandParams(
            Set<String> commandParams, ResourceAccessor resourceAccessor)
            throws CommandLineParsingException, DatabaseException {
        String refDriver = referenceDriver;
        String refUrl = referenceUrl;
        String refUsername = referenceUsername;
        String refPassword = referencePassword;
        String defSchemaName = this.referenceDefaultSchemaName;
        String defCatalogName = this.referenceDefaultCatalogName;

        for (String param : commandParams) {
            String[] splitArg = splitArg(param);

            String attributeName = splitArg[0];
            String value = splitArg[1];
            if (OPTIONS.REFERENCE_DRIVER.equalsIgnoreCase(attributeName)) {
                refDriver = value;
            } else if (OPTIONS.REFERENCE_URL.equalsIgnoreCase(attributeName)) {
                refUrl = value;
            } else if (OPTIONS.REFERENCE_USERNAME.equalsIgnoreCase(attributeName)) {
                refUsername = value;
            } else if (OPTIONS.REFERENCE_PASSWORD.equalsIgnoreCase(attributeName)) {
                refPassword = value;
            } else if (OPTIONS.REFERENCE_DEFAULT_CATALOG_NAME.equalsIgnoreCase(attributeName)) {
                defCatalogName = value;
            } else if (OPTIONS.REFERENCE_DEFAULT_SCHEMA_NAME.equalsIgnoreCase(attributeName)) {
                defSchemaName = value;
            } else if (OPTIONS.DATA_OUTPUT_DIRECTORY.equalsIgnoreCase(attributeName)) {
                dataOutputDirectory = value;
            }
        }

        if (refUrl == null) {
            throw new CommandLineParsingException(
                    String.format(coreBundle.getString("option.required"), "--referenceUrl"));
        }

        return CommandLineUtils.createDatabaseObject(resourceAccessor, refUrl, refUsername, refPassword, refDriver,
                defCatalogName, defSchemaName, Boolean.parseBoolean(outputDefaultCatalog), Boolean.parseBoolean
                        (outputDefaultSchema), null, null, this.propertyProviderClass, this.liquibaseCatalogName,
                this.liquibaseSchemaName, this.databaseChangeLogTableName, this.databaseChangeLogLockTableName);
    }

    private OutputStream getOutputStream() throws IOException {
        if (outputFile != null) {
            FileOutputStream fileOut;
            try {
                fileOut = new FileOutputStream(outputFile, false);
                return fileOut;
            } catch (IOException e) {
                LogService.getLog(getClass()).severe(LogType.LOG, String.format(
                        coreBundle.getString("could.not.create.output.file"),
                        outputFile));
                throw e;
            }
        } else {
            return System.out;
        }

    }

    private Writer getOutputWriter() throws IOException {
        String charsetName = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class)
                .getOutputEncoding();

        return new OutputStreamWriter(getOutputStream(), charsetName);
    }

    /**
     * Determines if this program is executed on a Microsoft Windows-type of operating system.
     *
     * @return true if running under some variant of MS Windows, false otherwise.
     */
    public boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows ");
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private enum FILE_SUFFIXES {
        ;
        private static final String FILE_SUFFIX_EAR = ".ear";
        private static final String WAR_FILE_SUFFIX = ".war";
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private enum COMMANDS {
        ;
        private static final String CALCULATE_CHECKSUM = "calculateCheckSum";
        private static final String CHANGELOG_SYNC = "changelogSync";
        private static final String CHANGELOG_SYNC_SQL = "changelogSyncSQL";
        private static final String CLEAR_CHECKSUMS = "clearCheckSums";
        private static final String DB_DOC = "dbDoc";
        private static final String DIFF = "diff";
        private static final String DIFF_CHANGELOG = "diffChangeLog";
        private static final String DROP_ALL = "dropAll";
        private static final String EXECUTE_SQL = "executeSql";
        private static final String FUTURE_ROLLBACK_COUNT_SQL = "futureRollbackCountSQL";
        private static final String FUTURE_ROLLBACK_FROM_TAG_SQL = "futureRollbackFromTagSQL";
        private static final String FUTURE_ROLLBACK_SQL = "futureRollbackSQL";
        private static final String FUTURE_ROLLBACK_TO_TAG_SQL = "futureRollbackToTagSQL";
        private static final String GENERATE_CHANGELOG = "generateChangeLog";
        private static final String HELP = OPTIONS.HELP;
        private static final String HISTORY = "history";
        private static final String LIST_LOCKS = "listLocks";
        private static final String MARK_NEXT_CHANGESET_RAN = "markNextChangeSetRan";
        private static final String MARK_NEXT_CHANGESET_RAN_SQL = "markNextChangeSetRanSQL";
        private static final String MIGRATE = "migrate";
        private static final String MIGRATE_SQL = "migrateSQL";
        private static final String RELEASE_LOCKS = "releaseLocks";
        private static final String ROLLBACK_ONE_CHANGE_SET = "rollbackOneChangeSet";
        private static final String ROLLBACK_ONE_CHANGE_SET_SQL = "rollbackOneChangeSetSQL";
        private static final String ROLLBACK_ONE_UPDATE = "rollbackOneUpdate";
        private static final String ROLLBACK_ONE_UPDATE_SQL = "rollbackOneUpdateSQL";
        private static final String FORMATTED_DIFF = "formattedDiff";
        private static final String ROLLBACK = "rollback";
        private static final String ROLLBACK_COUNT = "rollbackCount";
        private static final String ROLLBACK_COUNT_SQL = "rollbackCountSQL";
        private static final String ROLLBACK_SCRIPT = "rollbackScript";
        private static final String ROLLBACK_SQL = "rollbackSQL";
        private static final String ROLLBACK_TO_DATE = "rollbackToDate";
        private static final String ROLLBACK_TO_DATE_SQL = "rollbackToDateSQL";
        private static final String SNAPSHOT = "snapshot";
        private static final String SNAPSHOT_REFERENCE = "snapshotReference";
        private static final String STATUS = "status";
        private static final String TAG = "tag";
        private static final String TAG_EXISTS = "tagExists";
        private static final String UNEXPECTED_CHANGESETS = "unexpectedChangeSets";
        private static final String UPDATE = "update";
        private static final String UPDATE_COUNT = "updateCount";
        private static final String UPDATE_COUNT_SQL = "updateCountSQL";
        private static final String UPDATE_SQL = "updateSQL";
        private static final String UPDATE_TESTING_ROLLBACK = "updateTestingRollback";
        private static final String UPDATE_TO_TAG = "updateToTag";
        private static final String UPDATE_TO_TAG_SQL = "updateToTagSQL";
        private static final String VALIDATE = "validate";
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private enum OPTIONS {
        ;
        private static final String VERBOSE = "verbose";
        private static final String CHANGELOG_FILE = "changeLogFile";
        private static final String DATA_OUTPUT_DIRECTORY = "dataOutputDirectory";
        private static final String DIFF_TYPES = "diffTypes";
        private static final String CHANGE_SET_ID = "changeSetId";
        private static final String CHANGE_SET_AUTHOR = "changeSetAuthor";
        private static final String CHANGE_SET_PATH = "changeSetPath";
        private static final String DEPLOYMENT_ID = "deploymentId";
        private static final String OUTPUT_FILE = "outputFile";
        private static final String FORCE = "force";
        private static final String FORMAT = "format";
        private static final String ROLLBACK_SCRIPT = "rollbackScript";
        private static final String EXCLUDE_OBJECTS = "excludeObjects";
        private static final String INCLUDE_CATALOG = "includeCatalog";
        private static final String INCLUDE_OBJECTS = "includeObjects";
        private static final String INCLUDE_SCHEMA = "includeSchema";
        private static final String INCLUDE_TABLESPACE = "includeTablespace";
        private static final String OUTPUT_SCHEMAS_AS = "outputSchemasAs";
        private static final String REFERENCE_DEFAULT_CATALOG_NAME = "referenceDefaultCatalogName";
        private static final String REFERENCE_DEFAULT_SCHEMA_NAME = "referenceDefaultSchemaName";
        private static final String REFERENCE_DRIVER = "referenceDriver";
        // SONAR confuses this constant name with a hard-coded password:
        @SuppressWarnings("squid:S2068")
        private static final String REFERENCE_PASSWORD = "referencePassword";
        private static final String REFERENCE_SCHEMAS = "referenceSchemas";
        private static final String REFERENCE_URL = "referenceUrl";
        private static final String REFERENCE_USERNAME = "referenceUsername";
        private static final String SCHEMAS = "schemas";
        private static final String URL = "url";
        private static final String HELP = "help";
        private static final String VERSION = "version";
        private static final String SNAPSHOT_FORMAT = "snapshotFormat";
        private static final String LOG_FILE = "logFile";
        private static final String LOG_LEVEL = "logLevel";
    }

    private static class ConsoleFilter extends Filter<ILoggingEvent> {
        private Set<LogType> includeTypes = new HashSet<>();
        private final Level logLevel;


        public ConsoleFilter(Level logLevel, LogType... types) {
            includeTypes.addAll(Arrays.asList(types));
            this.logLevel = logLevel;
        }

        @Override
        public FilterReply decide(ILoggingEvent event) {
            LogType messageType = LogType.valueOf(event.getMarker().getName());

            if (event.getMarker().getName().equals(LogType.USER_MESSAGE.name())) {
                return FilterReply.ACCEPT;
            } else {
                if (!event.getLevel().isGreaterOrEqual(logLevel)) {
                    return FilterReply.DENY;
                }
            }

            if (includeTypes.isEmpty() || includeTypes.contains(messageType)) {
                return FilterReply.ACCEPT;
            }

            return FilterReply.DENY;
        }
    }
}
