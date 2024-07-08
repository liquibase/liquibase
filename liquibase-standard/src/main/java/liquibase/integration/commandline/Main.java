package liquibase.integration.commandline;

import liquibase.*;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.visitor.DefaultChangeExecListener;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.command.core.*;
import liquibase.command.core.helpers.*;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.core.DeprecatedConfigurationValueProvider;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.*;
import liquibase.integration.IntegrationDetails;
import liquibase.license.LicenseInstallResult;
import liquibase.license.LicenseService;
import liquibase.license.LicenseServiceFactory;
import liquibase.license.Location;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.logging.core.JavaLogService;
import liquibase.logging.mdc.MdcKey;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.ui.CompositeUIService;
import liquibase.ui.ConsoleUIService;
import liquibase.ui.LoggerUIService;
import liquibase.ui.UIService;
import liquibase.util.ISODateFormat;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtil;
import liquibase.util.SystemUtil;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.*;

import static java.util.ResourceBundle.getBundle;

/**
 * Class for executing Liquibase via the command line.
 *
 * @deprecated use liquibase.integration.commandline.LiquibaseCommandLine.
 */
public class Main {

    //set by new CLI to signify it is handling some of the configuration
    public static boolean runningFromNewCli;

    //temporary work-around to pass -D changelog parameters from new CLI to here
    public static Map<String, String> newCliChangelogParameters;

    private static PrintStream outputStream = System.out;

    private static final String ERRORMSG_UNEXPECTED_PARAMETERS = "unexpected.command.parameters";
    private static final Logger LOG = Scope.getCurrentScope().getLog(Main.class);
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

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
    protected String labelFilter;
    protected String driverPropertiesFile;
    protected String propertyProviderClass;
    protected String changeExecListenerClass;
    protected String changeExecListenerPropertiesFile;
    protected Boolean promptForNonLocalDatabase;
    protected Boolean includeSystemClasspath;
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
    protected Boolean deactivate;
    protected String outputSchemasAs;
    protected String referenceSchemas;
    protected String schemas;
    protected String snapshotFormat;
    protected String liquibaseProLicenseKey;
    private static final Boolean managingLogConfig = null;
    private static final boolean outputsLogMessages = false;
    protected String sqlFile;
    protected String delimiter;
    protected String rollbackScript;
    protected Boolean rollbackOnError = false;
    protected List<CatalogAndSchema> schemaList = new ArrayList<>();
    protected String format;
    protected String showSummary;

    private static final int[] suspiciousCodePoints = {160, 225, 226, 227, 228, 229, 230, 198, 200, 201, 202, 203,
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
    public static int run(String[] args) throws Exception {
        Map<String, Object> scopeObjects = new HashMap<>();
        final IntegrationDetails integrationDetails = new IntegrationDetails();
        integrationDetails.setName("cli");
        final ListIterator<String> argIterator = Arrays.asList(args).listIterator();
        while (argIterator.hasNext()) {
            final String arg = argIterator.next();
            if (arg.startsWith("--")) {
                if (arg.contains("=")) {
                    String[] splitArg = arg.split("=", 2);
                    String argKey = "argument__" + splitArg[0].replaceFirst("^--", "");
                    if (splitArg.length == 2) {
                        integrationDetails.setParameter(argKey, splitArg[1]);
                    } else {
                        integrationDetails.setParameter(argKey, "true");
                    }
                } else {
                    String argKey = "argument__" + arg.replaceFirst("^--", "");
                    if (argIterator.hasNext()) {
                        final String next = argIterator.next();
                        if (next.startsWith("--") || isCommand(next)) {
                            integrationDetails.setParameter(argKey, "true");
                            argIterator.previous(); //put value back
                        } else {
                            integrationDetails.setParameter(argKey, next);
                        }
                    } else {
                        integrationDetails.setParameter(argKey, "true");
                    }
                }
            }
        }

        scopeObjects.put("integrationDetails", integrationDetails);

        if (!Main.runningFromNewCli) {
            List<UIService> uiOutputServices = new ArrayList<>();
            ConsoleUIService console = new ConsoleUIService();
            console.setAllowPrompt(true);
            uiOutputServices.add(console);
            if (LiquibaseCommandLineConfiguration.MIRROR_CONSOLE_MESSAGES_TO_LOG.getCurrentValue()) {
                uiOutputServices.add(new LoggerUIService());
            }
            CompositeUIService compositeUIService = new CompositeUIService(console, uiOutputServices);
            scopeObjects.put(Scope.Attr.ui.name(), compositeUIService);
        }

        //TODO: Reformat
            return Scope.child(scopeObjects, new Scope.ScopedRunnerWithReturn<Integer>() {
                @Override
                public Integer run() throws Exception {
                    Main main = new Main();

                    try {
                        if ((args.length == 0) || ((args.length == 1) && ("--" + OPTIONS.HELP).equals(args[0]))) {
                            main.printHelp(outputStream);
                            return 0;
                        } else if (("--" + OPTIONS.VERSION).equals(args[0])) {
                            main.command = "";
                            main.parseDefaultPropertyFiles();
                            Scope.getCurrentScope().getUI().sendMessage(CommandLineUtils.getBanner());
                            Scope.getCurrentScope().getUI().sendMessage(String.format(coreBundle.getString("version.number"), LiquibaseUtil.getBuildVersionInfo()));

                            LicenseService licenseService = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService();
                            if (licenseService != null && main.liquibaseProLicenseKey != null) {
                                Location licenseKeyLocation =
                                        new Location("property liquibaseProLicenseKey", main.liquibaseProLicenseKey);
                                LicenseInstallResult result = licenseService.installLicense(licenseKeyLocation);
                                if (result.code != 0) {
                                    String allMessages = String.join("\n", result.messages);
                                    Scope.getCurrentScope().getUI().sendErrorMessage(allMessages);
                                }
                            }
                            if (licenseService != null) {
                                Scope.getCurrentScope().getUI().sendMessage(licenseService.getLicenseInfo());
                            }


                            Scope.getCurrentScope().getUI().sendMessage(String.format("Running Java under %s (Version %s)",
                                    System.getProperties().getProperty("java.home"),
                                    SystemUtil.getJavaVersion()
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
                                main.printHelp(outputStream);
                                return 0;
                            }
                            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_COMMAND_NAME, main.command);
                        } catch (CommandLineParsingException e) {
                            Scope.getCurrentScope().getUI().sendMessage(CommandLineUtils.getBanner());
                            Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("how.to.display.help"));
                            throw e;
                        }

                        if (!Main.runningFromNewCli) {
                            final UIService ui = Scope.getCurrentScope().getUI();
                            System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] %4$s [%2$s] %5$s%6$s%n");

                            java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
                            java.util.logging.Logger liquibaseLogger = java.util.logging.Logger.getLogger("liquibase");
                            liquibaseLogger.setParent(rootLogger);

                            LogService logService = Scope.getCurrentScope().get(Scope.Attr.logService, LogService.class);
                            if (logService instanceof JavaLogService) {
                                ((JavaLogService) logService).setParent(liquibaseLogger);
                            }

                            if (main.logLevel == null) {
                                String defaultLogLevel = System.getProperty("liquibase.log.level");
                                if (defaultLogLevel == null) {
                                    setLogLevel(logService, rootLogger, liquibaseLogger, Level.OFF);
                                } else {
                                    setLogLevel(logService, rootLogger, liquibaseLogger, parseLogLevel(defaultLogLevel, ui));
                                }
                            } else {
                                setLogLevel(logService, rootLogger, liquibaseLogger, parseLogLevel(main.logLevel, ui));
                            }

                            if (main.logFile != null) {
                                FileHandler fileHandler = new FileHandler(main.logFile, true);
                                fileHandler.setFormatter(new SimpleFormatter());
                                if (liquibaseLogger.getLevel() == Level.OFF) {
                                    fileHandler.setLevel(Level.FINE);
                                }

                                rootLogger.addHandler(fileHandler);
                                for (Handler handler : rootLogger.getHandlers()) {
                                    if (handler instanceof ConsoleHandler) {
                                        handler.setLevel(Level.OFF);
                                    }
                                }
                            }

                            if (main.command != null && main.command.toLowerCase().endsWith("sql")) {
                                if (ui instanceof CompositeUIService) {
                                    ((CompositeUIService) ui).getOutputServices().stream()
                                            .filter(service -> service instanceof ConsoleUIService)
                                            .forEach(console -> ((ConsoleUIService) console).setOutputStream(System.err));
                                } else if (ui instanceof ConsoleUIService) {
                                    ((ConsoleUIService) ui).setOutputStream(System.err);
                                }
                            }
                        }


                        LicenseService licenseService = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService();
                        if (licenseService != null) {
                            if (main.liquibaseProLicenseKey == null) {
                                if (!Main.runningFromNewCli) {
                                    Scope.getCurrentScope().getLog(getClass()).info("No Liquibase Pro license key supplied. Please set liquibaseProLicenseKey on command line or in liquibase.properties to use Liquibase Pro features.");
                                }
                            } else {
                                Location licenseKeyLocation = new Location("property liquibaseProLicenseKey", main.liquibaseProLicenseKey);
                                LicenseInstallResult result = licenseService.installLicense(licenseKeyLocation);
                                if (result.code != 0) {
                                    String allMessages = String.join("\n", result.messages);
                                    if (!Main.runningFromNewCli) {
                                        Scope.getCurrentScope().getUI().sendMessage(allMessages);
                                    }
                                }
                            }

                            if (!Main.runningFromNewCli) {
                                Scope.getCurrentScope().getUI().sendMessage(licenseService.getLicenseInfo());
                            }
                        }

                        if (!Main.runningFromNewCli) {
                            Scope.getCurrentScope().getUI().sendMessage(CommandLineUtils.getBanner());
                        }

                        if (!LiquibaseCommandLineConfiguration.SHOULD_RUN.getCurrentValue()) {
                            Scope.getCurrentScope().getUI().sendErrorMessage((
                                    String.format(coreBundle.getString("did.not.run.because.param.was.set.to.false"),
                                            LiquibaseCommandLineConfiguration.SHOULD_RUN.getCurrentConfiguredValue().getProvidedValue().getActualKey())));
                            return 0;
                        }

                        if (setupNeeded(main)) {
                            List<String> setupMessages = main.checkSetup();
                            if (!setupMessages.isEmpty()) {
                                main.printHelp(setupMessages, isStandardOutputRequired(main.command) ? System.err : outputStream);
                                return 1;
                            }
                        }

                        main.applyDefaults();
                        Map<String, Object> innerScopeObjects = new HashMap<>();
                        innerScopeObjects.put("defaultsFile", LiquibaseCommandLineConfiguration.DEFAULTS_FILE.getCurrentValue());
                        if (!Main.runningFromNewCli) {
                            innerScopeObjects.put(Scope.Attr.resourceAccessor.name(), new CompositeResourceAccessor(
                                    new DirectoryResourceAccessor(Paths.get(".").toAbsolutePath().toFile()),
                                    new ClassLoaderResourceAccessor(main.configureClassLoader())
                            ));
                        }

                        Scope.child(innerScopeObjects, () -> {
                            main.doMigration();
                            if (!Main.runningFromNewCli) {
                                if (COMMANDS.UPDATE.equals(main.command)) {
                                    Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("update.successful"));
                                } else if (main.command.startsWith(COMMANDS.ROLLBACK)) {
                                    Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("rollback.successful"));
                                } else {
                                    Scope.getCurrentScope().getUI().sendMessage(String.format(coreBundle.getString("command.successful"), main.command));
                                }
                            }
                        });
                        Scope.getCurrentScope().getMdcManager().clear();
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
                                ((ValidationFailedException) e.getCause()).printDescriptiveError(outputStream);
                            } else {
                                if (!Main.runningFromNewCli) {
                                    if (main.outputsLogMessages) {
                                        Scope.getCurrentScope().getUI().sendErrorMessage((String.format(coreBundle.getString("unexpected.error"), message)), e);
                                    } else {
                                        Scope.getCurrentScope().getUI().sendMessage((String.format(coreBundle.getString("unexpected.error"), message)));
                                        Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("for.more.information.use.loglevel.flag"));

                                        //send it to the LOG in case we're using logFile
                                        Scope.getCurrentScope().getLog(getClass()).severe((String.format(coreBundle.getString("unexpected.error"), message)), e);
                                    }
                                }
                            }
                        } catch (IllegalFormatException e1) {
                            if (Main.runningFromNewCli) {
                                throw e1;
                            }

                            e1.printStackTrace();
                        }
                        if (runningFromNewCli) {
                            throw e;
                        } else {
                            throw new LiquibaseException(String.format(coreBundle.getString("unexpected.error"), message), e);
                        }
                    }

                    return 0;
                }
            });
    }

    private static boolean setupNeeded(Main main) throws CommandLineParsingException {
        if (!main.commandParams.contains("--help")) {
            return true;
        }
        return !main.command.toLowerCase().startsWith(COMMANDS.ROLLBACK_ONE_CHANGE_SET.toLowerCase()) &&
                !main.command.toLowerCase().startsWith(COMMANDS.ROLLBACK_ONE_UPDATE.toLowerCase()) &&
                (!main.command.toLowerCase().startsWith(COMMANDS.DIFF.toLowerCase()) || !main.isFormattedDiff());
    }

    protected static void setLogLevel(LogService logService, java.util.logging.Logger rootLogger, java.util.logging.Logger liquibaseLogger, Level level) {
        if (Main.runningFromNewCli) {
            //new CLI configures logging
            return;
        }

        if (level.intValue() < Level.INFO.intValue()) {
            //limit non-liquibase logging to INFO at a minimum to avoid too much logs
            rootLogger.setLevel(Level.INFO);
        } else {
            rootLogger.setLevel(level);
        }
        liquibaseLogger.setLevel(level);

        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(level);
        }
    }

    private static Level parseLogLevel(String logLevelName, UIService ui) {
        logLevelName = logLevelName.toUpperCase();
        Level logLevel;
        switch (logLevelName) {
            case "DEBUG":
                logLevel = Level.FINE;
                break;
            case "WARN":
                logLevel = Level.WARNING;
                break;
            case "ERROR":
                logLevel = Level.SEVERE;
                break;
            default:
                try {
                    logLevel = Level.parse(logLevelName);
                } catch (IllegalArgumentException e) {
                    ui.sendErrorMessage("Unknown log level " + logLevelName);
                    logLevel = Level.OFF;
                }
                break;
        }
        return logLevel;
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
                || COMMANDS.CHANGELOG_SYNC_TO_TAG_SQL.equalsIgnoreCase(command)
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
                || COMMANDS.CHANGELOG_SYNC_TO_TAG.equalsIgnoreCase(command)
                || COMMANDS.CHANGELOG_SYNC_TO_TAG_SQL.equalsIgnoreCase(command)
                || COMMANDS.GENERATE_CHANGELOG.equalsIgnoreCase(command)
                || COMMANDS.UNEXPECTED_CHANGESETS.equalsIgnoreCase(command)
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
                || COMMANDS.FUTURE_ROLLBACK_FROM_TAG_SQL.equalsIgnoreCase(arg)
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
                || COMMANDS.CHANGELOG_SYNC_TO_TAG.equalsIgnoreCase(arg)
                || COMMANDS.CHANGELOG_SYNC_TO_TAG_SQL.equalsIgnoreCase(arg)
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
                || COMMANDS.UPDATE_TESTING_ROLLBACK.equalsIgnoreCase(arg)
                || COMMANDS.LIST_LOCKS.equalsIgnoreCase(arg)
                || COMMANDS.RELEASE_LOCKS.equalsIgnoreCase(arg)
                || COMMANDS.VALIDATE.equalsIgnoreCase(arg)
                || COMMANDS.HELP.equalsIgnoreCase(arg)
                || COMMANDS.CLEAR_CHECKSUMS.equalsIgnoreCase(arg)
                || COMMANDS.CHANGELOG_SYNC.equalsIgnoreCase(arg)
                || COMMANDS.CHANGELOG_SYNC_SQL.equalsIgnoreCase(arg);
    }

    private static void addWarFileClasspathEntries(File classPathFile, List<URL> urls) throws IOException {
        URL jarUrl = new URL("jar:" + classPathFile.toURI().toURL() + "!/WEB-INF/classes/");
        LOG.info("adding '" + jarUrl + "' to classpath");
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
                    LOG.info("adding '" + newUrl + "' to classpath");
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
                BufferedOutputStream outStream = new BufferedOutputStream(Files.newOutputStream(tempFile.toPath()))
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
        LinkedHashSet<File> potentialPropertyFiles = new LinkedHashSet<>();

        potentialPropertyFiles.add(new File(defaultsFile));
        String localDefaultsPathName = defaultsFile.replaceFirst("(\\.[^\\.]+)$", ".local$1");
        potentialPropertyFiles.add(new File(localDefaultsPathName));

        final ConfiguredValue<String> currentConfiguredValue = LiquibaseCommandLineConfiguration.DEFAULTS_FILE.getCurrentConfiguredValue();
        if (currentConfiguredValue.found()) {
            potentialPropertyFiles.add(new File(currentConfiguredValue.getValue()));
        }

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
                (potentialPropertyFile.getPath())) {
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
    private void parseDefaultPropertyFileFromFile(final File potentialPropertyFile) throws IOException,
            CommandLineParsingException {
        try (InputStream stream = Files.newInputStream(potentialPropertyFile.toPath())) {
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

        return fixedArgs.toArray(new String[0]);
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
            if (StringUtil.trimToNull(url) == null && StringUtil.trimToNull(referenceUrl) == null) {
                messages.add(String.format(coreBundle.getString("option.required"), "--" + OPTIONS.URL));
            }

            if (isChangeLogRequired(command) && (StringUtil.trimToNull(changeLogFile) == null)) {
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
                || COMMANDS.TAG_EXISTS.equalsIgnoreCase(command)
                || COMMANDS.CHANGELOG_SYNC_TO_TAG.equalsIgnoreCase(command)
                || COMMANDS.CHANGELOG_SYNC_TO_TAG_SQL.equalsIgnoreCase(command)) {

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
                    String caseInsensitiveCommandParam = cmdParm.toLowerCase();
                    if (!caseInsensitiveCommandParam.startsWith("--" + OPTIONS.REFERENCE_USERNAME.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.REFERENCE_PASSWORD.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.REFERENCE_DRIVER.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.REFERENCE_DEFAULT_CATALOG_NAME.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.REFERENCE_DEFAULT_SCHEMA_NAME.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.INCLUDE_SCHEMA.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.INCLUDE_CATALOG.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.INCLUDE_TABLESPACE.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.SCHEMAS.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.OUTPUT_SCHEMAS_AS.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.REFERENCE_SCHEMAS.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.REFERENCE_URL.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.EXCLUDE_OBJECTS.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.INCLUDE_OBJECTS.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.DIFF_TYPES.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.FORMAT.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.HELP.toLowerCase())
                            && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.SNAPSHOT_FORMAT.toLowerCase())) {
                        messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                    }
                    if (COMMANDS.DIFF_CHANGELOG.equalsIgnoreCase(command) && cmdParm.toLowerCase().startsWith("--" + OPTIONS.FORMAT.toLowerCase())) {
                        messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                    }
                }
            }
        } else if ((COMMANDS.SNAPSHOT.equalsIgnoreCase(command)
                || COMMANDS.GENERATE_CHANGELOG.equalsIgnoreCase(command))
                && (!commandParams.isEmpty())) {
            for (String cmdParm : commandParams) {
                String caseInsensitiveCommandParam = cmdParm.toLowerCase();
                if (!caseInsensitiveCommandParam.startsWith("--" + OPTIONS.INCLUDE_SCHEMA.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.INCLUDE_CATALOG.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.INCLUDE_TABLESPACE.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.SCHEMAS.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.SNAPSHOT_FORMAT.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.DATA_OUTPUT_DIRECTORY.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.OUTPUT_SCHEMAS_AS.toLowerCase())) {
                    messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                }
            }
        } else if (COMMANDS.ROLLBACK_ONE_CHANGE_SET.equalsIgnoreCase(command)) {
            for (String cmdParm : commandParams) {
                String caseInsensitiveCommandParam = cmdParm.toLowerCase();
                if (!caseInsensitiveCommandParam.startsWith("--" + OPTIONS.CHANGE_SET_ID.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.HELP.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.FORCE.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.CHANGE_SET_PATH.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.CHANGE_SET_AUTHOR.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.ROLLBACK_SCRIPT.toLowerCase())) {
                    messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                }
            }
        } else if (COMMANDS.ROLLBACK_ONE_CHANGE_SET_SQL.equalsIgnoreCase(command)) {
            for (String cmdParm : commandParams) {
                String caseInsensitiveCommandParam = cmdParm.toLowerCase();
                if (!caseInsensitiveCommandParam.startsWith("--" + OPTIONS.CHANGE_SET_ID.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.HELP.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.FORCE.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.CHANGE_SET_PATH.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.CHANGE_SET_AUTHOR.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.ROLLBACK_SCRIPT.toLowerCase())) {
                    messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                }
            }
        } else if (COMMANDS.ROLLBACK_ONE_UPDATE.equalsIgnoreCase(command)) {
            for (String cmdParm : commandParams) {
                String caseInsensitiveCommandParam = cmdParm.toLowerCase();
                if (!caseInsensitiveCommandParam.startsWith("--" + OPTIONS.DEPLOYMENT_ID.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.HELP.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.FORCE.toLowerCase())) {
                    messages.add(String.format(coreBundle.getString("unexpected.command.parameter"), cmdParm));
                }
            }
        } else if (COMMANDS.ROLLBACK_ONE_UPDATE_SQL.equalsIgnoreCase(command)) {
            for (String cmdParm : commandParams) {
                String caseInsensitiveCommandParam = cmdParm.toLowerCase();

                if (!caseInsensitiveCommandParam.startsWith("--" + OPTIONS.DEPLOYMENT_ID.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.HELP.toLowerCase())
                        && !caseInsensitiveCommandParam.startsWith("--" + OPTIONS.FORCE.toLowerCase())) {
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

       if (COMMANDS.DIFF_CHANGELOG.equalsIgnoreCase(command) && (diffTypes != null) && diffTypes.toLowerCase
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
        final IntegrationDetails integrationDetails = Scope.getCurrentScope().get("integrationDetails", IntegrationDetails.class);

        Properties props = new Properties();
        props.load(propertiesInputStream);

        if (Main.runningFromNewCli) {
            parsePropertiesFileForNewCli(props);
            return;
        }

        boolean strict = GlobalConfiguration.STRICT.getCurrentValue();

        //
        // Load property values into
        //   changeLogParameters
        //   ConfigurationContainer
        //   local member variable
        //
        for (Map.Entry entry : props.entrySet()) {
            String entryValue = null;
            if (entry.getValue() != null) {
                entryValue = String.valueOf(entry.getValue());
            }
            if (integrationDetails != null) {
                integrationDetails.setParameter("defaultsFile__" + entry.getKey(), entryValue);
            }

            try {
                if ("promptOnNonLocalDatabase".equals(entry.getKey())) {
                    continue;
                }
                if (((String) entry.getKey()).startsWith("parameter.")) {
                    changeLogParameters.put(((String) entry.getKey()).replaceFirst("^parameter.", ""), entry.getValue());
                } else if (((String) entry.getKey()).contains(".")) {
                    if (Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getRegisteredDefinition((String) entry.getKey()) == null) {
                        if (strict) {
                            throw new CommandLineParsingException(
                                    String.format(coreBundle.getString("parameter.unknown"), entry.getKey())
                            );
                        } else {
                            Scope.getCurrentScope().getLog(getClass()).warning(
                                    String.format(coreBundle.getString("parameter.ignored"), entry.getKey())
                            );
                        }
                    }
                    if (System.getProperty((String) entry.getKey()) == null) {
                        DeprecatedConfigurationValueProvider.setData((String) entry.getKey(), entry.getValue());
                    }
                } else {
                    Field field = getDeclaredField((String) entry.getKey());
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
                    Scope.getCurrentScope().getLog(getClass()).warning(
                            String.format(coreBundle.getString("parameter.ignored"), entry.getKey())
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
     * Most of the properties file is handled by the new CLI. But, for now we have to handle changelog parameter values still
     */
    private void parsePropertiesFileForNewCli(Properties props) {
        for (Map.Entry entry : props.entrySet()) {
            if (((String) entry.getKey()).startsWith("parameter.")) {
                changeLogParameters.put(((String) entry.getKey()).replaceFirst("^parameter.", ""), entry.getValue());
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
        this.logLevel = Level.WARNING.toString();

        String helpText = "Help not available when running liquibase.integration.commandline.Main directly. Use liquibase.integration.commandline.LiquibaseCommandLine";
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
            char ch = chars[i];
            for (int suspiciousCodePoint : suspiciousCodePoints) {
                if (suspiciousCodePoint == ch) {
                    CodePointCheck codePointCheck = new CodePointCheck();
                    codePointCheck.position = i;
                    codePointCheck.ch = ch;
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

        //
        // Check the licensing keys to see if they are being set from properties
        //
        if (liquibaseProLicenseKey == null) {
            liquibaseProLicenseKey = (String) Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentConfiguredValue(null, null, "liquibase.licenseKey").getValue();
        }

        //
        // Property provider class
        //
        if (propertyProviderClass == null) {
            Class clazz = LiquibaseCommandLineConfiguration.PROPERTY_PROVIDER_CLASS.getCurrentValue();
            if (clazz != null) {
                propertyProviderClass = clazz.getName();
            }
        }

        //
        // Database class
        //
        if (databaseClass == null) {
            Class clazz = LiquibaseCommandLineConfiguration.DATABASE_CLASS.getCurrentValue();
            if (clazz != null) {
                databaseClass = clazz.getName();
            }
        }
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

        if (PROMPT_FOR_VALUE.equalsIgnoreCase(StringUtil.trimToEmpty(value))) {
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
            Field field = getDeclaredField(attributeName);
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

    private Field getDeclaredField(String attributeName) throws NoSuchFieldException {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase(attributeName)) {
                return field;
            }
        }
        throw new NoSuchFieldException();
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
            this.includeSchema = Boolean.FALSE;
        }
        if (this.includeCatalog == null) {
            this.includeCatalog = Boolean.FALSE;
        }
        if (this.includeTablespace == null) {
            this.includeTablespace = Boolean.FALSE;
        }

    }

    protected ClassLoader configureClassLoader() throws CommandLineParsingException {
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
                                LOG.fine(String.format(coreBundle.getString("adding.to.classpath"), newUrl));
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
                    LOG.fine(String.format(coreBundle.getString("adding.to.classpath"), newUrl));
                    urls.add(newUrl);
                }
            }
        }
        if (includeSystemClasspath) {
            classLoader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread()
                    .getContextClassLoader()));

        } else {
            classLoader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urls.toArray(new URL[0]), null));
        }

        Thread.currentThread().setContextClassLoader(classLoader);

        return classLoader;
    }


    /**
     * Do the actual database migration, i.e. apply the ChangeSets.
     *
     * @throws Exception
     */
    @SuppressWarnings("java:S2095")
    protected void doMigration() throws Exception {
        if (COMMANDS.HELP.equalsIgnoreCase(command)) {
            printHelp(System.err);
            return;
        }

        final ResourceAccessor fileOpener = this.getFileOpenerResourceAccessor();

        if (COMMANDS.DIFF.equalsIgnoreCase(command) || COMMANDS.DIFF_CHANGELOG.equalsIgnoreCase(command)
            || COMMANDS.GENERATE_CHANGELOG.equalsIgnoreCase(command)
            || COMMANDS.RELEASE_LOCKS.equalsIgnoreCase(command)
            || COMMANDS.ROLLBACK.equalsIgnoreCase(command) || COMMANDS.ROLLBACK_SQL.equalsIgnoreCase(command)) {
            this.runUsingCommandFramework();
            return;
        }

        Database database = null;
        if (this.url != null) {
            database = CommandLineUtils.createDatabaseObject(fileOpener, this.url,
                    this.username, this.password, this.driver, this.defaultCatalogName, this.defaultSchemaName,
                    Boolean.parseBoolean(outputDefaultCatalog), Boolean.parseBoolean(outputDefaultSchema),
                    this.databaseClass, this.driverPropertiesFile, this.propertyProviderClass,
                    this.liquibaseCatalogName, this.liquibaseSchemaName, this.databaseChangeLogTableName,
                    this.databaseChangeLogLockTableName);
            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_TARGET_URL, JdbcConnection.sanitizeUrl(this.url));
            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_CATALOG_NAME, database.getLiquibaseCatalogName());
            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_SCHEMA_NAME, database.getLiquibaseSchemaName());
            if (this.databaseChangeLogTablespaceName != null) {
                database.setLiquibaseTablespaceName(this.databaseChangeLogTablespaceName);
            } else {
                database.setLiquibaseTablespaceName(GlobalConfiguration.LIQUIBASE_TABLESPACE_NAME.getCurrentConfiguredValue().getValue());
            }
        }

        if (GlobalConfiguration.SHOULD_SNAPSHOT_DATA.getCurrentValue().equals(false) && dataOutputDirectory != null) {
            // If we are not otherwise going to snapshot data, still snapshot data if dataOutputDirectory is set
            DeprecatedConfigurationValueProvider.setData(GlobalConfiguration.SHOULD_SNAPSHOT_DATA, true);
        }

        try {
            if (COMMANDS.SNAPSHOT.equalsIgnoreCase(command)) {
                CommandScope snapshotCommand = new CommandScope("internalSnapshot");
                snapshotCommand
                        .addArgumentValue(InternalSnapshotCommandStep.DATABASE_ARG, database)
                        .addArgumentValue(InternalSnapshotCommandStep.SCHEMAS_ARG, InternalSnapshotCommandStep.parseSchemas(database, getSchemaParams(database)))
                        .addArgumentValue(InternalSnapshotCommandStep.SERIALIZER_FORMAT_ARG, getCommandParam(OPTIONS.SNAPSHOT_FORMAT, null));

                //
                // If we find a ResultsBuilder in the current scope then
                // we will add the snapshot object to it
                // otherwise, we will print the output
                //
                Writer outputWriter = getOutputWriter();
                CommandResults commandResults = snapshotCommand.execute();
                String result = InternalSnapshotCommandStep.printSnapshot(snapshotCommand, commandResults);
                outputWriter.write(result);
                outputWriter.flush();
                return;
            }

            Liquibase liquibase = new Liquibase(changeLogFile, fileOpener, database);
            if (Main.newCliChangelogParameters != null) {
                for (Map.Entry<String, String> param : Main.newCliChangelogParameters.entrySet()) {
                    liquibase.setChangeLogParameter(param.getKey(), param.getValue());
                }
            }
            ChangeExecListener listener = ChangeExecListenerUtils.getChangeExecListener(
                    liquibase.getDatabase(), liquibase.getResourceAccessor(),
                    changeExecListenerClass, changeExecListenerPropertiesFile);
            DefaultChangeExecListener defaultChangeExecListener = liquibase.getDefaultChangeExecListener();
            defaultChangeExecListener.addListener(listener);
            liquibase.setChangeExecListener(defaultChangeExecListener);
            if (database != null) {
                database.setCurrentDateTimeFunction(currentDateTimeFunction);
            }
            for (Map.Entry<String, Object> entry : changeLogParameters.entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }

            if (COMMANDS.LIST_LOCKS.equalsIgnoreCase(command)) {
                liquibase.reportLocks(System.err);
                return;
            } else if (COMMANDS.TAG.equalsIgnoreCase(command)) {
                liquibase.tag(getCommandArgument());
                return;
            } else if (COMMANDS.TAG_EXISTS.equalsIgnoreCase(command)) {
                String tag = commandParams.iterator().next();
                liquibase.tagExists(tag);
                return;
            } else if (COMMANDS.DROP_ALL.equalsIgnoreCase(command)) {
                CommandScope dropAllCommand = new CommandScope("dropAll");
                dropAllCommand
                        .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, liquibase.getDatabase())
                        .addArgumentValue(DropAllCommandStep.CATALOG_AND_SCHEMAS_ARG, InternalSnapshotCommandStep.parseSchemas(database, getSchemaParams(database)))
                        .addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile);

                dropAllCommand.execute();
                return;
            } else if (COMMANDS.STATUS.equalsIgnoreCase(command)) {
                boolean runVerbose = false;

                if (commandParams.contains("--" + OPTIONS.VERBOSE)) {
                    runVerbose = true;
                }
                liquibase.reportStatus(runVerbose, new Contexts(contexts), new LabelExpression(getLabelFilter()),
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
                liquibase.validate();
                Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("no.validation.errors.found"));
                return;
            } else if (COMMANDS.CLEAR_CHECKSUMS.equalsIgnoreCase(command)) {
                liquibase.clearCheckSums();
                return;
            } else if (COMMANDS.CALCULATE_CHECKSUM.equalsIgnoreCase(command)) {
                CommandScope calculateChecksumCommand = new CommandScope("calculateChecksum");

                calculateChecksumCommand
                        .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
                        .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_PATH_ARG, getCommandParam(OPTIONS.CHANGE_SET_PATH, null))
                        .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_ID_ARG, getCommandParam(OPTIONS.CHANGE_SET_ID, null))
                        .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_AUTHOR_ARG, getCommandParam(OPTIONS.CHANGE_SET_AUTHOR, null))
                        .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_IDENTIFIER_ARG, getCommandParam(OPTIONS.CHANGE_SET_IDENTIFIER, null))
                        .addArgumentValue(CalculateChecksumCommandStep.CHANGELOG_FILE_ARG, this.changeLogFile);

                calculateChecksumCommand.execute();
                return;
            } else if (COMMANDS.DB_DOC.equalsIgnoreCase(command)) {
                if (commandParams.isEmpty()) {
                    throw new CommandLineParsingException(coreBundle.getString("dbdoc.requires.output.directory"));
                }
                if (changeLogFile == null) {
                    throw new CommandLineParsingException(coreBundle.getString("dbdoc.requires.changelog.parameter"));
                }

                if (schemas != null) {
                    for (String schema : schemas.split(",")) {
                        schemaList.add(new CatalogAndSchema(null, schema).customize(database));
                    }

                    CatalogAndSchema[] schemaArr = schemaList.stream().toArray(CatalogAndSchema[]::new);

                    liquibase.generateDocumentation(commandParams.iterator().next(), contexts, schemaArr);
                }
                else {
                    liquibase.generateDocumentation(commandParams.iterator().next(), contexts);
                }

                return;
            }

            try {
                if (COMMANDS.CHANGELOG_SYNC.equalsIgnoreCase(command)) {
                    liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(getLabelFilter()));
                } else if (COMMANDS.CHANGELOG_SYNC_SQL.equalsIgnoreCase(command)) {
                    liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(getLabelFilter()), getOutputWriter());
                } else if (COMMANDS.CHANGELOG_SYNC_TO_TAG.equalsIgnoreCase(command)) {
                    liquibase.changeLogSync(commandParams.iterator().next(), new Contexts(contexts), new LabelExpression(getLabelFilter()));
                } else if (COMMANDS.CHANGELOG_SYNC_TO_TAG_SQL.equalsIgnoreCase(command)) {
                    liquibase.changeLogSync(commandParams.iterator().next(), new Contexts(contexts), new LabelExpression(getLabelFilter()), getOutputWriter());
                } else if (COMMANDS.MARK_NEXT_CHANGESET_RAN.equalsIgnoreCase(command)) {
                    liquibase.markNextChangeSetRan(new Contexts(contexts), new LabelExpression(getLabelFilter()));
                } else if (COMMANDS.MARK_NEXT_CHANGESET_RAN_SQL.equalsIgnoreCase(command)) {
                    liquibase.markNextChangeSetRan(new Contexts(contexts), new LabelExpression(getLabelFilter()),
                            getOutputWriter());
                } else if (COMMANDS.UPDATE_COUNT.equalsIgnoreCase(command)) {
                    try {
                        Map<String, Object> updateScopedObjects = new HashMap<>();
                        updateScopedObjects.put("showSummary", showSummary);
                        updateScopedObjects.put("outputStream", outputStream);
                        Scope.child(updateScopedObjects, () -> liquibase.update(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts),
                                new LabelExpression(getLabelFilter())));
                    } catch (LiquibaseException updateException) {
                        handleUpdateException(database, updateException, defaultChangeExecListener, rollbackOnError);
                    }
                } else if (COMMANDS.UPDATE.equalsIgnoreCase(command)) {
                    liquibase.update(new Contexts(contexts), new LabelExpression(getLabelFilter()));
                } else if (COMMANDS.UPDATE_COUNT_SQL.equalsIgnoreCase(command)) {
                    liquibase.update(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts), new
                            LabelExpression(getLabelFilter()), getOutputWriter());
                } else if (COMMANDS.UPDATE_TO_TAG.equalsIgnoreCase(command)) {
                    if ((commandParams == null) || commandParams.isEmpty()) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.tag"), COMMANDS.UPDATE_TO_TAG));
                    }
                    try {
                        Map<String, Object> updateScopedObjects = new HashMap<>();
                        updateScopedObjects.put("showSummary", showSummary);
                        updateScopedObjects.put("outputStream", outputStream);
                        Scope.child(updateScopedObjects, () -> liquibase.update(commandParams.iterator().next(), new Contexts(contexts), new LabelExpression(getLabelFilter())));
                    } catch (LiquibaseException updateException) {
                        handleUpdateException(database, updateException, defaultChangeExecListener, rollbackOnError);
                    }
                } else if (COMMANDS.UPDATE_TO_TAG_SQL.equalsIgnoreCase(command)) {
                    if ((commandParams == null) || commandParams.isEmpty()) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.tag"),
                                        COMMANDS.UPDATE_TO_TAG_SQL));
                    }

                    liquibase.update(commandParams.iterator().next(), new Contexts(contexts), new LabelExpression
                            (getLabelFilter()), getOutputWriter());
                } else if (COMMANDS.UPDATE_SQL.equalsIgnoreCase(command)) {
                    liquibase.update(new Contexts(contexts), new LabelExpression(getLabelFilter()), getOutputWriter());
                } else if (COMMANDS.ROLLBACK_TO_DATE.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.timestamp"),
                                        COMMANDS.ROLLBACK_TO_DATE));
                    }
                    liquibase.rollback(new ISODateFormat().parse(getCommandArgument()), getCommandParam
                            (COMMANDS.ROLLBACK_SCRIPT, null), new Contexts(contexts), new LabelExpression(getLabelFilter()));
                } else if (COMMANDS.ROLLBACK_COUNT.equalsIgnoreCase(command)) {
                    Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_INTERNAL_COMMAND, COMMANDS.ROLLBACK_COUNT);
                    liquibase.rollback(Integer.parseInt(getCommandArgument()), getCommandParam
                            (COMMANDS.ROLLBACK_SCRIPT, null), new Contexts(contexts), new LabelExpression(getLabelFilter()));

                } else if (COMMANDS.ROLLBACK_TO_DATE_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.timestamp"),
                                        COMMANDS.ROLLBACK_TO_DATE_SQL));
                    }
                    liquibase.rollback(new ISODateFormat().parse(getCommandArgument()), getCommandParam
                                    (COMMANDS.ROLLBACK_SCRIPT, null), new Contexts(contexts), new LabelExpression
                                    (getLabelFilter()),
                            getOutputWriter());
                } else if (COMMANDS.ROLLBACK_COUNT_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.count"),
                                        COMMANDS.ROLLBACK_COUNT_SQL));
                    }

                    liquibase.rollback(Integer.parseInt(getCommandArgument()), getCommandParam
                                    (COMMANDS.ROLLBACK_SCRIPT, null), new Contexts(contexts),
                            new LabelExpression(getLabelFilter()),
                            getOutputWriter()
                    );
                } else if (COMMANDS.FUTURE_ROLLBACK_SQL.equalsIgnoreCase(command)) {
                    liquibase.futureRollbackSQL(new Contexts(contexts), new LabelExpression(getLabelFilter()), getOutputWriter());
                } else if (COMMANDS.FUTURE_ROLLBACK_COUNT_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.count"),
                                        COMMANDS.FUTURE_ROLLBACK_COUNT_SQL));
                    }

                    liquibase.futureRollbackSQL(Integer.valueOf(getCommandArgument()), new Contexts(contexts), new
                            LabelExpression(getLabelFilter()), getOutputWriter());
                } else if (COMMANDS.FUTURE_ROLLBACK_FROM_TAG_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException(
                                String.format(coreBundle.getString("command.requires.tag"),
                                        COMMANDS.FUTURE_ROLLBACK_FROM_TAG_SQL));
                    }

                    liquibase.futureRollbackSQL(getCommandArgument(), new Contexts(contexts), new LabelExpression
                            (getLabelFilter()), getOutputWriter());
                } else if (COMMANDS.UPDATE_TESTING_ROLLBACK.equalsIgnoreCase(command)) {
                    try {
                        liquibase.updateTestingRollback(new Contexts(contexts), new LabelExpression(getLabelFilter()));
                    } catch (LiquibaseException updateException) {
                        handleUpdateException(database, updateException, defaultChangeExecListener, rollbackOnError);
                    }
                } else if (COMMANDS.HISTORY.equalsIgnoreCase(command)) {
                    CommandScope historyCommand = new CommandScope("internalHistory");
                    historyCommand.addArgumentValue(InternalHistoryCommandStep.DATABASE_ARG, database);
                    historyCommand.addArgumentValue(InternalHistoryCommandStep.FORMAT_ARG, HistoryFormat.valueOf(format));
                    historyCommand.setOutput(getOutputStream());

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
                Scope.getCurrentScope().getLog(getClass()).warning(
                    coreBundle.getString("problem.closing.connection"), e);
            }
        }
    }

    /**
     * Run commands using the CommandFramework instead of directly setting up and calling other classes
     */
    private void runUsingCommandFramework() throws CommandLineParsingException, LiquibaseException, IOException {
        if (COMMANDS.DIFF.equalsIgnoreCase(command)) {
            runDiffCommandStep();
        } else if (COMMANDS.DIFF_CHANGELOG.equalsIgnoreCase(command)) {
            runDiffChangelogCommandStep();
        } else if (COMMANDS.GENERATE_CHANGELOG.equalsIgnoreCase(command)) {
            runGenerateChangelogCommandStep();
        } else if (COMMANDS.UPDATE.equalsIgnoreCase(command)) {
            runUpdateCommandStep();
        } else if (COMMANDS.RELEASE_LOCKS.equalsIgnoreCase(command)) {
            runReleaseLocksCommand();
        } else if (COMMANDS.ROLLBACK.equalsIgnoreCase(command)) {
            runRollbackCommand(null);
        } else if (COMMANDS.ROLLBACK_SQL.equalsIgnoreCase(command)) {
            runRollbackSqlCommand();
        } else if (COMMANDS.EXECUTE_SQL.equalsIgnoreCase(command)) {
            runExecuteSqlCommand();
        } else if (COMMANDS.ROLLBACK_ONE_CHANGE_SET.equalsIgnoreCase(command)) {
            runRollbackOneChangeSetCommandStep();
        } else if (COMMANDS.ROLLBACK_ONE_CHANGE_SET_SQL.equalsIgnoreCase(command)) {
            runRollbackOneChangeSetSqlCommandStep();
        } else if (COMMANDS.ROLLBACK_ONE_UPDATE.equalsIgnoreCase(command)) {
            runRollbackOneUpdateCommandStep();
        } else if (COMMANDS.ROLLBACK_ONE_UPDATE_SQL.equalsIgnoreCase(command)) {
            runRollbackOneUpdateSqlCommandStep();
        }
    }

    private void runRollbackSqlCommand() throws CommandLineParsingException, IOException, CommandExecutionException {
        this.runRollbackCommand(getOutputWriter());
    }

    private void runRollbackCommand(Writer outputWriter) throws CommandLineParsingException, CommandExecutionException {
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_INTERNAL_COMMAND, COMMANDS.ROLLBACK);
        if (getCommandArgument() == null) {
            throw new CommandLineParsingException(
                    String.format(coreBundle.getString("command.requires.tag"), COMMANDS.ROLLBACK));
        }

        String[] commandToRun = outputWriter == null ? RollbackCommandStep.COMMAND_NAME : RollbackSqlCommandStep.COMMAND_NAME;

        CommandScope commandScope = new CommandScope(commandToRun)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, contexts)
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, getLabelFilter())
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_CLASS_ARG, changeExecListenerClass)
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG, changeExecListenerPropertiesFile)
                .addArgumentValue(RollbackCommandStep.TAG_ARG, getCommandArgument())
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, getCommandParam(COMMANDS.ROLLBACK_SCRIPT, null));
        this.setDatabaseArgumentsToCommand(commandScope);
        if (outputWriter != null) {
            commandScope.setOutput(new WriterOutputStream(outputWriter, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
        }
        commandScope.execute();
    }

    private void runReleaseLocksCommand() throws CommandExecutionException {
        CommandScope commandScope = new CommandScope(ReleaseLocksCommandStep.COMMAND_NAME[0]);
        this.setDatabaseArgumentsToCommand(commandScope);
        commandScope.execute();
    }

    private void runGenerateChangelogCommandStep() throws LiquibaseException, IOException, CommandLineParsingException {
        final boolean shouldOverwriteOutputFile = Boolean.parseBoolean(overwriteOutputFile);

        CommandScope generateChangelogCommand = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME[0])
                .addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_CATALOG_ARG, includeCatalog)
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, includeSchema)
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_TABLESPACE_ARG, includeTablespace)
                .addArgumentValue(GenerateChangelogCommandStep.AUTHOR_ARG, StringUtil.trimToNull(changeSetAuthor))
                .addArgumentValue(GenerateChangelogCommandStep.CONTEXT_ARG, StringUtil.trimToNull(changeSetContext))
                .addArgumentValue(GenerateChangelogCommandStep.DATA_OUTPUT_DIR_ARG, StringUtil.trimToNull(dataOutputDirectory))
                .addArgumentValue(GenerateChangelogCommandStep.OVERWRITE_OUTPUT_FILE_ARG, shouldOverwriteOutputFile)
                .setOutput(System.out);

        this.setDatabaseArgumentsToCommand(generateChangelogCommand);
        this.setPreCompareArgumentsToCommand(generateChangelogCommand);
    }

    private void runDiffChangelogCommandStep() throws CommandExecutionException, CommandLineParsingException, IOException {
        CommandScope diffChangelogCommand = new CommandScope("diffChangelog")
                .addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_CATALOG_ARG, includeCatalog)
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, includeSchema)
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_TABLESPACE_ARG, includeTablespace)
                .addArgumentValue(DiffChangelogCommandStep.AUTHOR_ARG, StringUtil.trimToNull(changeSetAuthor))
                .setOutput(getOutputStream());

        this.setPreCompareArgumentsToCommand(diffChangelogCommand);
        this.setDatabaseArgumentsToCommand(diffChangelogCommand);
        this.setReferenceDatabaseArgumentsToCommand(diffChangelogCommand);

        diffChangelogCommand.execute();
    }

    private void runDiffCommandStep() throws CommandLineParsingException, CommandExecutionException, IOException {
        CommandScope diffCommand = new CommandScope("diff")
            .addArgumentValue(DiffCommandStep.FORMAT_ARG, getCommandParam(OPTIONS.FORMAT, "TXT"))
            .setOutput(getOutputStream());

        this.setPreCompareArgumentsToCommand(diffCommand);
        this.setDatabaseArgumentsToCommand(diffCommand);
        this.setReferenceDatabaseArgumentsToCommand(diffCommand);

        diffCommand.execute();
    }

    private void runUpdateCommandStep() throws CommandLineParsingException, CommandExecutionException, IOException {
        CommandScope updateCommand = new CommandScope("update");
        updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
        updateCommand.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, contexts);
        updateCommand.addArgumentValue(UpdateCommandStep.LABEL_FILTER_ARG, labelFilter);
        updateCommand.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_CLASS_ARG, changeExecListenerClass);
        updateCommand.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG, changeExecListenerPropertiesFile);
        setDatabaseArgumentsToCommand(updateCommand);
        updateCommand.execute();
    }

    private void runRollbackOneChangeSetCommandStep() throws CommandExecutionException, CommandLineParsingException {
        CommandScope rollbackOneChangeSet = new CommandScope("rollbackOneChangeset");
        setDatabaseArgumentsToCommand(rollbackOneChangeSet);
        rollbackOneChangeSet.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_CLASS_ARG, changeExecListenerClass)
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG, changeExecListenerPropertiesFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue("changesetId", getCommandParam(OPTIONS.CHANGE_SET_ID, null))
                .addArgumentValue("changesetAuthor", getCommandParam(OPTIONS.CHANGE_SET_AUTHOR, null))
                .addArgumentValue("changesetPath", getCommandParam(OPTIONS.CHANGE_SET_PATH, null))
                .addArgumentValue("force", getCommandParam(OPTIONS.FORCE, null));
        String internalCommand = "rollbackOneChangeset";
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_INTERNAL_COMMAND, internalCommand);
        rollbackOneChangeSet.execute();
    }

    private void runRollbackOneChangeSetSqlCommandStep() throws CommandExecutionException, CommandLineParsingException, IOException {
        CommandScope rollbackOneChangeSet = new CommandScope("rollbackOneChangesetSql");
        setDatabaseArgumentsToCommand(rollbackOneChangeSet);
        rollbackOneChangeSet.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_CLASS_ARG, changeExecListenerClass)
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG, changeExecListenerPropertiesFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue("changesetId", getCommandParam(OPTIONS.CHANGE_SET_ID, null))
                .addArgumentValue("changesetAuthor", getCommandParam(OPTIONS.CHANGE_SET_AUTHOR, null))
                .addArgumentValue("changesetPath", getCommandParam(OPTIONS.CHANGE_SET_PATH, null))
                .addArgumentValue("force", getCommandParam(OPTIONS.FORCE, null));
        String internalCommand = "rollbackOneChangeset";
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_INTERNAL_COMMAND, internalCommand);
        rollbackOneChangeSet.setOutput(new WriterOutputStream(getOutputWriter(), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
        rollbackOneChangeSet.execute();
    }

    private void runRollbackOneUpdateCommandStep() throws CommandExecutionException, CommandLineParsingException {
        CommandScope rollbackOneUpdate = new CommandScope("rollbackOneUpdate");
        setDatabaseArgumentsToCommand(rollbackOneUpdate);
        rollbackOneUpdate.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_CLASS_ARG, changeExecListenerClass)
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG, changeExecListenerPropertiesFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue("deploymentId", getCommandParam(OPTIONS.DEPLOYMENT_ID, null))
                .addArgumentValue("force", getCommandParam(OPTIONS.FORCE, null));
        String internalCommand = "rollbackOneUpdate";
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_INTERNAL_COMMAND, internalCommand);
        rollbackOneUpdate.execute();
    }

    private void runRollbackOneUpdateSqlCommandStep() throws CommandExecutionException, CommandLineParsingException, IOException {
        CommandScope rollbackOneUpdate = new CommandScope("rollbackOneUpdateSql");
        setDatabaseArgumentsToCommand(rollbackOneUpdate);
        rollbackOneUpdate.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_CLASS_ARG, changeExecListenerClass)
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG, changeExecListenerPropertiesFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue("deploymentId", getCommandParam(OPTIONS.DEPLOYMENT_ID, null))
                .addArgumentValue("force", getCommandParam(OPTIONS.FORCE, null));
        String internalCommand = "rollbackOneUpdate";
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_INTERNAL_COMMAND, internalCommand);
        rollbackOneUpdate.setOutput(new WriterOutputStream(getOutputWriter(), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
        rollbackOneUpdate.execute();
    }

    /**
     * Set database arguments values received by Main class to the provided command scope.
     */
    private void setDatabaseArgumentsToCommand(CommandScope command) {
        command.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG, defaultSchemaName)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_CATALOG_NAME_ARG, defaultCatalogName)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DRIVER_ARG, driver)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DRIVER_PROPERTIES_FILE_ARG, driverPropertiesFile)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, username)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, password)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, url);
    }

    /**
     * Set database compare arguments values received by Main class to the provided command scope.
     */
    private void setPreCompareArgumentsToCommand(CommandScope command) {
        command.addArgumentValue(PreCompareCommandStep.EXCLUDE_OBJECTS_ARG, excludeObjects)
                .addArgumentValue(PreCompareCommandStep.INCLUDE_OBJECTS_ARG, includeObjects)
                .addArgumentValue(PreCompareCommandStep.DIFF_TYPES_ARG, diffTypes)
                .addArgumentValue(PreCompareCommandStep.SCHEMAS_ARG, schemas)
                .addArgumentValue(PreCompareCommandStep.OUTPUT_SCHEMAS_ARG, outputSchemasAs)
                .addArgumentValue(PreCompareCommandStep.REFERENCE_SCHEMAS_ARG, referenceSchemas);
    }

    /**
     * Set reference database arguments values received by Main class to the provided command scope.
     */
    private void setReferenceDatabaseArgumentsToCommand(CommandScope command) throws CommandLineParsingException {
        String refDriver = referenceDriver;
        String refUrl = referenceUrl;
        String refUsername = referenceUsername;
        String refPassword = referencePassword;
        String refSchemaName = this.referenceDefaultSchemaName;
        String refCatalogName = this.referenceDefaultCatalogName;

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
                refCatalogName = value;
            } else if (OPTIONS.REFERENCE_DEFAULT_SCHEMA_NAME.equalsIgnoreCase(attributeName)) {
                refSchemaName = value;
            } else if (OPTIONS.DATA_OUTPUT_DIRECTORY.equalsIgnoreCase(attributeName)) {
                dataOutputDirectory = value;
            }
        }

        if (refUrl == null) {
            throw new CommandLineParsingException(
                    String.format(coreBundle.getString("option.required"), "--referenceUrl"));
        }

        command.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DEFAULT_SCHEMA_NAME_ARG, refSchemaName)
                .addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_URL_ARG, refUrl)
                .addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DEFAULT_CATALOG_NAME_ARG, refCatalogName)
                .addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DRIVER_ARG, refDriver)
                .addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DRIVER_PROPERTIES_FILE_ARG, null)
                .addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_USERNAME_ARG, refUsername)
                .addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_PASSWORD_ARG, refPassword);
    }

    private void runExecuteSqlCommand() throws CommandExecutionException, CommandLineParsingException {
        CommandScope executeSql = new CommandScope(ExecuteSqlCommandStep.COMMAND_NAME[0]);
        executeSql.addArgumentValue(ExecuteSqlCommandStep.SQL_ARG, getCommandParam("sql", null));
        executeSql.addArgumentValue(ExecuteSqlCommandStep.SQLFILE_ARG, getCommandParam("sqlFile", null));
        executeSql.addArgumentValue(ExecuteSqlCommandStep.DELIMITER_ARG, getCommandParam("delimiter", ";"));
        setDatabaseArgumentsToCommand(executeSql);
        executeSql.execute();
    }

    /**
     * Encapsulate code used to load the correct resource accessor providing legacy Cli compatibility
     */
    private ResourceAccessor getFileOpenerResourceAccessor() throws FileNotFoundException {
        ResourceAccessor fileOpener;
        if (Main.runningFromNewCli) {
            fileOpener = Scope.getCurrentScope().getResourceAccessor();
        } else {
            fileOpener = new CompositeResourceAccessor(
                    new DirectoryResourceAccessor(Paths.get(".").toAbsolutePath().toFile()),
                    new ClassLoaderResourceAccessor(classLoader)
            );
        }
        return fileOpener;
    }

    private String getLabelFilter() {
        if (labelFilter == null) {
            return labels;
        }
        return labelFilter;
    }

    private void loadChangeSetInfoToMap(Map<String, Object> argsMap) throws CommandLineParsingException {
        argsMap.put("changeSetId", getCommandParam(OPTIONS.CHANGE_SET_ID, null));
        argsMap.put("changeSetAuthor", getCommandParam(OPTIONS.CHANGE_SET_AUTHOR, null));
        argsMap.put("changeSetPath", getCommandParam(OPTIONS.CHANGE_SET_PATH, null));
    }

    private boolean isFormattedDiff() throws CommandLineParsingException {
        String formatValue = getCommandParam(OPTIONS.FORMAT, "txt");
        return !formatValue.equalsIgnoreCase("txt") && !formatValue.isEmpty();
    }

    private String getSchemaParams(Database database) throws CommandLineParsingException {
        String schemaParams = getCommandParam(OPTIONS.SCHEMAS, schemas);
        if (schemaParams == null || schemaParams.isEmpty()) {
            return database.getDefaultSchema().getSchemaName();
        }
        return schemaParams;
    }

    private CommandScope createLiquibaseCommand(Database database, Liquibase liquibase, String commandName, Map<String, Object> argsMap)
            throws LiquibaseException {
        argsMap.put("rollbackScript", rollbackScript);
        argsMap.put("changeLogFile", changeLogFile);
        argsMap.put("database", database);
        argsMap.put("liquibase", liquibase);
        if (!commandParams.contains("--help")) {
            argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        }
        ChangeLogParameters clp = new ChangeLogParameters(database);
        for (Map.Entry<String, Object> entry : changeLogParameters.entrySet()) {
            clp.set(entry.getKey(), entry.getValue());
        }
        argsMap.put("changeLogParameters", clp);

        if (this.commandParams.contains("--force") || this.commandParams.contains("--force=true")) {
            argsMap.put("force", Boolean.TRUE);
        }
        if (this.commandParams.contains("--help")) {
            argsMap.put("help", Boolean.TRUE);
        }
        if (liquibaseProLicenseKey != null) {
            argsMap.put("liquibaseProLicenseKey", liquibaseProLicenseKey);
        }
        CommandScope liquibaseCommand = new CommandScope(commandName);
        for (Map.Entry<String, Object> entry : argsMap.entrySet()) {
            liquibaseCommand.addArgumentValue(entry.getKey(), entry.getValue());
        }

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

    private OutputStream getOutputStream() throws IOException {
        if (outputStream != null) {
            return outputStream;
        }

        if (outputFile != null) {
            FileOutputStream fileOut;
            try {
                fileOut = new FileOutputStream(outputFile, false);
                return fileOut;
            } catch (IOException e) {
                Scope.getCurrentScope().getLog(getClass()).severe(String.format(
                        coreBundle.getString("could.not.create.output.file"),
                        outputFile));
                throw e;
            }
        } else {
            return outputStream;
        }
    }

    private void handleUpdateException(Database database, LiquibaseException exception, DefaultChangeExecListener defaultChangeExecListener, boolean rollbackOnError) throws LiquibaseException {
        try {
            CommandScope commandScope = new CommandScope("internalRollbackOnError");
            commandScope.addArgumentValue("database", database);
            commandScope.addArgumentValue("exception", exception);
            commandScope.addArgumentValue("listener", defaultChangeExecListener);
            commandScope.addArgumentValue("rollbackOnError", rollbackOnError);
            commandScope.execute();
        } catch (IllegalArgumentException ignoredCommandNotFound) {
            throw exception;
        }
    }

    /**
     * Sets the default outputstream to use. Mainly useful for testing and the Command wrappers.
     */
    public static PrintStream setOutputStream(PrintStream outputStream) {
        Main.outputStream = outputStream;

        return outputStream;
    }

    private Writer getOutputWriter() throws IOException {
        String charsetName = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();

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
        private static final String CHANGELOG_SYNC_TO_TAG = "changelogSyncToTag";
        private static final String CHANGELOG_SYNC_TO_TAG_SQL = "changelogSyncToTagSQL";
        private static final String CLEAR_CHECKSUMS = "clearCheckSums";
        private static final String DB_DOC = "dbDoc";
        private static final String DIFF = "diff";
        private static final String DIFF_CHANGELOG = "diffChangeLog";
        private static final String DROP_ALL = "dropAll";
        private static final String EXECUTE_SQL = "executeSql";
        private static final String FUTURE_ROLLBACK_COUNT_SQL = "futureRollbackCountSQL";
        private static final String FUTURE_ROLLBACK_FROM_TAG_SQL = "futureRollbackFromTagSQL";
        private static final String FUTURE_ROLLBACK_SQL = "futureRollbackSQL";
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

        public static final String CHANGE_SET_IDENTIFIER = "changeSetIdentifier";
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
}
