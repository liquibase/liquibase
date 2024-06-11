package liquibase.integration.commandline;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.command.CommandFailedException;
import liquibase.command.core.*;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.core.DefaultsFileValueProvider;
import liquibase.exception.*;
import liquibase.integration.IntegrationDetails;
import liquibase.license.LicenseInfo;
import liquibase.license.LicenseService;
import liquibase.license.LicenseServiceFactory;
import liquibase.logging.LogService;
import liquibase.logging.core.JavaLogService;
import liquibase.logging.core.LogServiceFactory;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcManager;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.customobjects.ChangesetsUpdated;
import liquibase.logging.mdc.customobjects.Version;
import liquibase.resource.*;
import liquibase.ui.CompositeUIService;
import liquibase.ui.ConsoleUIService;
import liquibase.ui.LoggerUIService;
import liquibase.ui.UIService;
import liquibase.util.*;
import picocli.CommandLine;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.ResourceBundle.getBundle;
import static liquibase.configuration.LiquibaseConfiguration.REGISTERED_VALUE_PROVIDERS_KEY;
import static liquibase.integration.commandline.VersionUtils.*;
import static liquibase.util.SystemUtil.isWindows;


public class LiquibaseCommandLine {

    public static final String COMMAND_ARGUMENTS = "commandArguments";

    private final Map<String, String> legacyPositionalArguments;

    /**
     * Arguments that used to be global arguments but are now command-level
     */
    private final Set<String> legacyNoLongerGlobalArguments;

    /**
     * Arguments that used to be command arguments but are now global
     */
    private final Set<String> legacyNoLongerCommandArguments;
    private Level configuredLogLevel;

    private final CommandLine commandLine;
    private Handler fileHandler;

    private final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    /**
     * Pico's defaultFactory does a lot of reflection, checking for classes we know we don't have.
     * That is slow on older JVMs and impact initial startup time, so do our own factory for performance reasons.
     * It is easy to configure pico to it's default factory, when profiling check for `CommandLine$DefaultFactory` usage
     */
    private CommandLine.IFactory defaultFactory = new CommandLine.IFactory() {
        @Override
        public <K> K create(Class<K> cls) throws Exception {
            return cls.newInstance();
        }
    };

    public static void main(String[] args) {
        //we don't ship jansi, so we know we can disable it without having to do the slow class checking
        System.setProperty("org.fusesource.jansi.Ansi.disable", "true");
        final LiquibaseCommandLine cli = new LiquibaseCommandLine();

        int returnCode = cli.execute(args);

        System.exit(returnCode);
    }

    private void cleanup() {
        if (fileHandler != null) {
            fileHandler.flush();
        }
    }

    public LiquibaseCommandLine() {
        this.legacyPositionalArguments = new HashMap<>();
        this.legacyPositionalArguments.put("calculatechecksum", CalculateChecksumCommandStep.CHANGESET_IDENTIFIER_ARG.getName());
        this.legacyPositionalArguments.put("changelogsynctotag", ChangelogSyncToTagCommandStep.TAG_ARG.getName());
        this.legacyPositionalArguments.put("changelogsynctotagsql", ChangelogSyncToTagSqlCommandStep.TAG_ARG.getName());
        this.legacyPositionalArguments.put("dbdoc", DbDocCommandStep.OUTPUT_DIRECTORY_ARG.getName());
        this.legacyPositionalArguments.put("futurerollbackcountsql", FutureRollbackCountSqlCommandStep.COUNT_ARG.getName());
        this.legacyPositionalArguments.put("futurerollbackfromtagsql", FutureRollbackFromTagSqlCommandStep.TAG_ARG.getName());
        this.legacyPositionalArguments.put("tag", TagCommandStep.TAG_ARG.getName());
        this.legacyPositionalArguments.put("tagexists", TagExistsCommandStep.TAG_ARG.getName());
        this.legacyPositionalArguments.put("rollback", RollbackCommandStep.TAG_ARG.getName());
        this.legacyPositionalArguments.put("rollbacksql", RollbackSqlCommandStep.TAG_ARG.getName());
        this.legacyPositionalArguments.put("rollbacktodate", RollbackToDateCommandStep.DATE_ARG.getName());
        this.legacyPositionalArguments.put("rollbacktodatesql", RollbackToDateSqlCommandStep.DATE_ARG.getName());
        this.legacyPositionalArguments.put("rollbackcount", RollbackCountCommandStep.COUNT_ARG.getName());
        this.legacyPositionalArguments.put("rollbackcountsql", RollbackCountSqlCommandStep.COUNT_ARG.getName());
        this.legacyPositionalArguments.put("updatecount", UpdateCountCommandStep.COUNT_ARG.getName());
        this.legacyPositionalArguments.put("updatecountsql", UpdateCountSqlCommandStep.COUNT_ARG.getName());
        this.legacyPositionalArguments.put("updatetotag", UpdateToTagCommandStep.TAG_ARG.getName());
        this.legacyPositionalArguments.put("updatetotagsql", UpdateToTagSqlCommandStep.TAG_ARG.getName());

        this.legacyNoLongerGlobalArguments = Stream.of(
                "username",
                "password",
                "url",
                "outputDefaultSchema",
                "outputDefaultCatalog",
                "changelogFile",
                "contexts",
                "labels",
                "diffTypes",
                "changesetAuthor",
                "changesetContext",
                "dataOutputDirectory",
                "referenceDriver",
                "referenceUrl",
                "referenceUsername",
                "referencePassword",
                "referenceDefaultCatalogName",
                "referenceDefaultSchemaName",
                "excludeObjects",
                "includeCatalog",
                "includeObjects",
                "includeSchema",
                "includeTablespace",
                "outputSchemasAs",
                "referenceSchemas",
                "schemas",
                "snapshotFormat",
                "sqlFile",
                "delimiter",
                "rollbackScript",
                "overwriteOutputFile",
                "changeExecListenerClass",
                "changeExecListenerPropertiesFile",
                "defaultSchemaName",
                "defaultCatalogName"
        ).collect(Collectors.toSet());

        this.legacyNoLongerCommandArguments = Stream.of(
                "databaseClass",
                "liquibaseCatalogName",
                "liquibaseSchemaName",
                "databaseChangeLogTableName",
                "databaseChangeLogLockTableName",
                "classpath",
                "propertyProviderClass",
                "promptForNonLocalDatabase",
                "includeSystemClasspath",
                "defaultsFile",
                "currentDateTimeFunction",
                "logLevel",
                "logFile",
                "outputFile",
                "liquibaseProLicenseKey",
                "outputFileEncoding",
                "outputLineSeparator"
        ).collect(Collectors.toSet());

        this.commandLine = buildPicoCommandLine();
    }

    private CommandLine buildPicoCommandLine() {
        final CommandLine.Model.CommandSpec rootCommandSpec = CommandLine.Model.CommandSpec.wrapWithoutInspection(null, defaultFactory);
        rootCommandSpec.name("liquibase");
        configureHelp(rootCommandSpec, true);
        rootCommandSpec.subcommandsCaseInsensitive(true);


        rootCommandSpec.usageMessage()
                .customSynopsis("liquibase [GLOBAL OPTIONS] [COMMAND] [COMMAND OPTIONS]\nCommand-specific help: \"liquibase <command-name> --help\"")
                .optionListHeading("\nGlobal Options\n")
                .commandListHeading("\nCommands\n")
        ;


        CommandLine commandLine = new CommandLine(rootCommandSpec, defaultFactory)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setOptionsCaseInsensitive(true)
                .setUsageHelpAutoWidth(true);

        addGlobalArguments(commandLine);

        for (CommandDefinition commandDefinition : getCommands()) {
            addSubcommand(commandDefinition, commandLine);
        }

        commandLine.setExecutionExceptionHandler((ex, commandLine1, parseResult) -> LiquibaseCommandLine.this.handleException(ex));

        commandLine.setUsageHelpAutoWidth(true);

        return commandLine;
    }

    protected int handleException(Throwable exception) {
        Throwable cause = exception;

        String uiMessage = "";
        while (cause != null) {
            String newMessage = StringUtil.trimToNull(cleanExceptionMessage(cause.getMessage()));
            if (newMessage != null) {
                if (!uiMessage.contains(newMessage)) {
                    if (!uiMessage.equals("")) {
                        uiMessage += System.lineSeparator() + "  - Caused by: ";
                    }
                    uiMessage += newMessage;
                }
            }

            cause = cause.getCause();
        }

        if (StringUtil.isEmpty(uiMessage)) {
            uiMessage = exception.getClass().getName();
        }

        //
        // For LiquibaseException, we can control the logging level
        //
        Level level = determineLogLevel(exception);

        if (showExceptionInLog(exception)) {
            Scope.getCurrentScope().getLog(getClass()).log(level, uiMessage, exception);
        }

        boolean printUsage = false;
        try (final StringWriter suggestionWriter = new StringWriter();
             PrintWriter suggestionsPrintWriter = new PrintWriter(suggestionWriter)) {
            if (exception instanceof CommandLine.ParameterException) {
                if (exception instanceof CommandLine.UnmatchedArgumentException) {
                    System.err.println("Unexpected argument(s): " + StringUtil.join(((CommandLine.UnmatchedArgumentException) exception).getUnmatched(), ", "));
                } else {
                    System.err.println("Error parsing command line: " + uiMessage);
                }
                CommandLine.UnmatchedArgumentException.printSuggestions((CommandLine.ParameterException) exception, suggestionsPrintWriter);

                printUsage = true;
            } else if (exception instanceof IllegalArgumentException
                    || exception instanceof CommandValidationException
                    || exception instanceof CommandLineParsingException) {
                System.err.println("Error parsing command line: " + uiMessage);
                printUsage = true;
            } else if (exception.getCause() instanceof CommandFailedException) {
                System.err.println(uiMessage);
            } else {
                System.err.println("\nUnexpected error running Liquibase: " + uiMessage);
                System.err.println();

                if (Level.OFF.equals(this.configuredLogLevel)) {
                    System.err.println("For more information, please use the --log-level flag");
                } else {
                    if (LiquibaseCommandLineConfiguration.LOG_FILE.getCurrentValue() == null) {
                        exception.printStackTrace(System.err);
                    }
                }
            }

            if (printUsage) {
                System.err.println();
                System.err.println("For detailed help, try 'liquibase --help' or 'liquibase <command-name> --help'");
            }

            suggestionsPrintWriter.flush();
            final String suggestions = suggestionWriter.toString();
            if (!suggestions.isEmpty()) {
                System.err.println();
                System.err.println(suggestions);
            }
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Error closing stream: " + e.getMessage(), e);
        }
        Throwable exitCodeException = ExceptionUtil.findExceptionInCauseChain(exception, ExitCodeException.class);
        if (exitCodeException != null) {
            Integer exitCode = ((ExitCodeException) exitCodeException.getCause()).getExitCode();
            if (exitCode != null) {
                return exitCode;
            }
        }
        return 1;
    }

    //
    // Honor the expected flag on a CommandFailedException
    //
    private boolean showExceptionInLog(Throwable exception) {
        Throwable t = exception;
        while (t != null) {
            if (t instanceof CommandFailedException && ((CommandFailedException) t).isExpected()) {
                return false;
            }
            t = t.getCause();
        }
        return true;
    }

    //
    // Look for a logLevel setting on any LiquibaseException
    // and use that for the Level to pass to the logger.
    // The lowest level of the exception stack will be used.
    //
    private Level determineLogLevel(Throwable throwable) {
        //
        // Default to severe
        //
        if (throwable == null) {
            return Level.SEVERE;
        }
        Level returnLevel = Level.SEVERE;
        Throwable t = throwable;
        while (t != null) {
            if (t instanceof LiquibaseException && ((LiquibaseException)t).getLogLevel() != null) {
                returnLevel = ((LiquibaseException)t).getLogLevel();
            }
            t = t.getCause();
        }
        return returnLevel;
    }

    protected String cleanExceptionMessage(String message) {
        if (message == null) {
            return null;
        }

        String originalMessage;
        do {
            originalMessage = message;
            message = message.replaceFirst("^[\\w.]*Exception: ", "");
            message = message.replaceFirst("^[\\w.]*Error: ", "");
        } while (!originalMessage.equals(message));

        message = message.replace("Unexpected error running Liquibase: ", "");
        return message;
    }

    public int execute(String[] args) {
        try {
            final String[] finalArgs = adjustLegacyArgs(args);

            configureLogging(Level.OFF, null);

            Main.runningFromNewCli = true;

            final List<ConfigurationValueProvider> valueProviders = registerValueProviders(finalArgs);
            LogService newLogService = Scope.child(Collections.singletonMap(REGISTERED_VALUE_PROVIDERS_KEY, true), () -> {
                // Get a new log service after registering the value providers, since the log service might need to load parameters using newly registered value providers.
                return Scope.getCurrentScope().getSingleton(LogServiceFactory.class).getDefaultLogService();
            });

            return Scope.child(Collections.singletonMap(Scope.Attr.logService.name(), newLogService), () -> {
                addEmptyMdcValues();
                try {
                    return Scope.child(configureScope(args), () -> {

                        if (!LiquibaseCommandLineConfiguration.SHOULD_RUN.getCurrentValue()) {
                            Scope.getCurrentScope().getUI().sendErrorMessage((
                                    String.format(coreBundle.getString("did.not.run.because.param.was.set.to.false"),
                                            LiquibaseCommandLineConfiguration.SHOULD_RUN.getCurrentConfiguredValue().getProvidedValue().getActualKey())));
                            return 0;
                        }

                        configureVersionInfo();

                        if (!wasHelpOrVersionRequested()) {
                            Scope.getCurrentScope().getUI().sendMessage(CommandLineUtils.getBanner());
                            Scope.getCurrentScope().getUI().sendMessage(String.format(coreBundle.getString("version.number"), LiquibaseUtil.getBuildVersionInfo()));

                            final LicenseService licenseService = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService();
                            if (licenseService == null) {
                                Scope.getCurrentScope().getUI().sendMessage("WARNING: License service not loaded, cannot determine Liquibase Pro license status. Please consider re-installing Liquibase to include all dependencies. Continuing operation without Pro license.");
                            } else {
                                Scope.getCurrentScope().getUI().sendMessage(licenseService.getLicenseInfo());
                            }
                        }

                        enableMonitoring();
                        logMdcData();
                        int response = commandLine.execute(finalArgs);

                        if (!wasHelpOrVersionRequested()) {
                            final ConfiguredValue<String> logFile = LiquibaseCommandLineConfiguration.LOG_FILE.getCurrentConfiguredValue();
                            if (logFile.found()) {
                                Scope.getCurrentScope().getUI().sendMessage("Logs saved to " + logFile.getValue());
                            }

                            final ConfiguredValue<String> outputFile = LiquibaseCommandLineConfiguration.OUTPUT_FILE.getCurrentConfiguredValue();
                            if (outputFile.found()) {
                                Scope.getCurrentScope().getUI().sendMessage("Output saved to " + outputFile.getValue());
                            }

                            if (response == 0) {
                                final List<CommandLine> commandList = commandLine.getParseResult().asCommandLineList();
                                final String commandName = StringUtil.join(getCommandNames(commandList.get(commandList.size() - 1)), " ");
                                Scope.getCurrentScope().getUI().sendMessage("Liquibase command '" + commandName + "' was executed successfully.");
                            }
                        }


                        return response;
                    });
                } finally {
                    final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

                    for (ConfigurationValueProvider provider : valueProviders) {
                        liquibaseConfiguration.unregisterProvider(provider);
                    }

                    LogUtil.setPersistedMdcKeysToEmptyString();
                }
            });
        } catch (Throwable e) {
            handleException(e);
            return 1;
        } finally {
            cleanup();
        }
    }

    private void addEmptyMdcValues() {
        if (LiquibaseCommandLineConfiguration.ADD_EMPTY_MDC_VALUES.getCurrentValue()) {
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_ID, "");
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, "NOOP");
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME_COUNT, "0");
            Scope.getCurrentScope().addMdcValue(MdcKey.ROWS_AFFECTED, "0");
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, "");
            LogUtil.setPersistedMdcKeysToEmptyString();
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OUTCOME, "NOOP");
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESETS_UPDATED, new ChangesetsUpdated());
            Scope.getCurrentScope().addMdcValue(MdcKey.OPERATION_START_TIME, "");
            Scope.getCurrentScope().addMdcValue(MdcKey.OPERATION_STOP_TIME, "");
            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_SYSTEM_NAME, "");
            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_SYSTEM_USER, "", false);
            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_TARGET_URL, "");
            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_VERSION, "");
            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_SCHEMA_NAME, "");
            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_CATALOG_NAME, "");
        }
    }

    /**
     * Log MDC data related to Liquibase system information.
     */
    private void logMdcData() throws IOException {
        MdcManager mdcManager = Scope.getCurrentScope().getMdcManager();
        String localHostName = NetUtil.getLocalHostName();
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_SYSTEM_USER, System.getProperty("user.name"), false);
        try (MdcObject version = mdcManager.put(MdcKey.LIQUIBASE_VERSION, LiquibaseUtil.getBuildVersion());
             MdcObject systemName = mdcManager.put(MdcKey.LIQUIBASE_SYSTEM_NAME, localHostName);
             // The host name here is purposefully the same as the system name. The system name is retained for backwards compatibility.
             MdcObject hostName = mdcManager.put(MdcKey.LIQUIBASE_HOST_NAME, localHostName)) {
            Scope.getCurrentScope().getLog(getClass()).info("Starting command execution.");
        }
    }

    protected void enableMonitoring() {
        final liquibase.logging.Logger log = Scope.getCurrentScope().getLog(getClass());

        try {
            final String monitorPerformanceValue = LiquibaseCommandLineConfiguration.MONITOR_PERFORMANCE.getCurrentValue();
            if (monitorPerformanceValue == null || monitorPerformanceValue.equalsIgnoreCase("false")) {
                log.fine("Performance monitoring disabled");
                return;
            }

            if (!SystemUtil.isAtLeastJava11()) {
                Scope.getCurrentScope().getUI().sendMessage("Performance monitoring requires Java 11 or greater. Version " + SystemUtil.getJavaVersion() + " is not supported.");
                return;
            }


            String filename = monitorPerformanceValue;
            if (filename.equalsIgnoreCase("true")) {
                filename = "liquibase-" + new ISODateFormat().format(new Date()).replaceAll("\\W", "_") + ".jfr";
            }
            if (!filename.endsWith(".jfr")) {
                filename = filename + ".jfr";
            }

            final Class<?> configurationClass = Class.forName("jdk.jfr.Configuration");
            final Class<?> recordingClass = Class.forName("jdk.jfr.Recording");
            Object configuration = configurationClass.getMethod("getConfiguration", String.class).invoke(null, "profile");
            Object recording = recordingClass.getConstructor(configurationClass).newInstance(configuration);
            recordingClass.getMethod("setMaxSize", long.class).invoke(recording, 0L);
            recordingClass.getMethod("setMaxAge", Duration.class).invoke(recording, (Duration) null);
            recordingClass.getMethod("setDumpOnExit", boolean.class).invoke(recording, true);
            recordingClass.getMethod("setToDisk", boolean.class).invoke(recording, true);

            recordingClass.getMethod("disable", String.class).invoke(recording, "jdk.InitialEnvironmentVariable");
            recordingClass.getMethod("disable", String.class).invoke(recording, "jdk.InitialSystemProperty");
            recordingClass.getMethod("disable", String.class).invoke(recording, "jdk.SystemProcess");
            recordingClass.getMethod("disable", String.class).invoke(recording, "jdk.JVMInformation");

            final File filePath = new File(filename).getAbsoluteFile();
            filePath.getParentFile().mkdirs();

            recordingClass.getMethod("setDestination", Path.class).invoke(recording, filePath.toPath());
            recordingClass.getMethod("start").invoke(recording);

            Scope.getCurrentScope().getUI().sendMessage("Saving performance data to " + filePath.getAbsolutePath());
        } catch (Throwable e) {
            final String message = "Error enabling performance monitoring: " + e.getMessage();
            Scope.getCurrentScope().getUI().sendMessage(message);
            log.warning(message, e);
        }
    }

    private boolean wasHelpOrVersionRequested() {
        CommandLine.ParseResult parseResult = commandLine.getParseResult();

        while (parseResult != null) {
            if (parseResult.isUsageHelpRequested() || parseResult.isVersionHelpRequested()) {
                return true;
            }
            parseResult = parseResult.subcommand();
        }

        return false;
    }

    private Map<String, String> addJavaPropertiesToChangelogParameters() {
        CommandLine.ParseResult subcommandParseResult = commandLine.getParseResult();
        while (subcommandParseResult.hasSubcommand()) {
            subcommandParseResult = subcommandParseResult.subcommand();
        }

        Map<String, String> changelogParameters = subcommandParseResult.matchedOptionValue("-D", new HashMap<>());
        if (changelogParameters.size() != 0) {
            Main.newCliChangelogParameters = changelogParameters;
        }
        return changelogParameters;
    }

    protected String[] adjustLegacyArgs(String[] args) {
        List<String> returnArgs = new ArrayList<>();


        final ListIterator<String> iterator = Arrays.asList(args).listIterator();
        while (iterator.hasNext()) {
            String arg = iterator.next();
            String argAsKey = arg.replace("-", "").toLowerCase();

            if (arg.startsWith("-")) {
                returnArgs.add(arg);
            } else {
                final String legacyTag = this.legacyPositionalArguments.get(argAsKey);
                if (legacyTag == null) {
                    returnArgs.add(arg);
                } else {
                    returnArgs.add(arg);

                    String value = " ";
                    while (iterator.hasNext()) {
                        arg = iterator.next();
                        if (arg.startsWith("-")) {
                            iterator.previous();
                            break;
                        } else {
                            value += arg + " ";
                        }
                    }

                    value = StringUtil.trimToNull(value);
                    if (value != null) {
                        returnArgs.add("--" + legacyTag);
                        returnArgs.add(value);
                    }
                }
            }
        }

        return returnArgs.toArray(new String[0]);
    }

    static String[] getCommandNames(CommandLine parseResult) {
        List<String> returnList = new ArrayList<>();
        while (!parseResult.getCommandName().equals("liquibase")) {
            returnList.add(0, parseResult.getCommandName());
            parseResult = parseResult.getParent();
        }

        return returnList.toArray(new String[0]);
    }

    private List<ConfigurationValueProvider> registerValueProviders(String[] args) throws IOException {
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
        List<ConfigurationValueProvider> returnList = new ArrayList<>();

        final CommandLineArgumentValueProvider argumentProvider = new CommandLineArgumentValueProvider(commandLine.parseArgs(args));
        liquibaseConfiguration.registerProvider(argumentProvider);
        returnList.add(argumentProvider);

        final ConfiguredValue<String> defaultsFileConfig = LiquibaseCommandLineConfiguration.DEFAULTS_FILE.getCurrentConfiguredValue();
        /*
         * The installed licenses are cleared from the license service. Clearing the licenses
         * forces the license service to reinstall the licenses.
         *
         * The call to LiquibaseCommandLineConfiguration.DEFAULTS_FILE.getCurrentConfiguredValue() above will check
         * the environment variables for a value, and that requires a valid license. Thus, if the user has a license
         * key specified in both an environment variable and in their defaults file (using different property names),
         * the value in the defaults file will not be seen, despite it possibly being more preferred than the value
         * in the environment variable, because the DefaultsFileValueProvider hasn't been registered yet.
         */
        LicenseServiceFactory licenseServiceFactory = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class);
        if (licenseServiceFactory != null) {
            LicenseService licenseService = licenseServiceFactory.getLicenseService();
            if (licenseService != null) {
                licenseService.reset();
            }
        }

        final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
        String defaultsFileConfigValue = defaultsFileConfig.getValue();
        Resource resource = pathHandlerFactory.getResource(defaultsFileConfigValue);
        if (resource.exists()) {
            try (InputStream defaultsStream = resource.openInputStream()) {
                if (defaultsStream != null) {
                    final DefaultsFileValueProvider fileProvider = new DefaultsFileValueProvider(defaultsStream, "File exists at path " + defaultsFileConfigValue);
                    liquibaseConfiguration.registerProvider(fileProvider);
                    returnList.add(fileProvider);
                }
            }
        } else {
            InputStream inputStreamOnClasspath = Thread.currentThread().getContextClassLoader().getResourceAsStream(defaultsFileConfigValue);
            if (inputStreamOnClasspath == null) {
                Scope.getCurrentScope().getLog(getClass()).fine("Cannot find defaultsFile " + defaultsFileConfigValue);
                if (!defaultsFileConfig.wasDefaultValueUsed()) {
                            //can't use UI since it's not configured correctly yet
                    if (GlobalConfiguration.STRICT.getCurrentValue()) {
                        throw new UnexpectedLiquibaseException("ERROR: The file '"+defaultsFileConfigValue+"' was not found. The global argument 'strict' is enabled, which validates the existence of files specified in liquibase files, such as changelogs, flowfiles, checks packages files, and more. To prevent this message, check your configurations, or disable the 'strict' setting.");
                    } else {
                        System.err.println("Could not find defaults file " + defaultsFileConfigValue);
                    }
                }
            } else {
                final DefaultsFileValueProvider fileProvider = new DefaultsFileValueProvider(inputStreamOnClasspath, "File in classpath " + defaultsFileConfigValue);
                liquibaseConfiguration.registerProvider(fileProvider);
                returnList.add(fileProvider);
            }
        }

        final File defaultsFile = new File(defaultsFileConfigValue);
        File localDefaultsFile = new File(defaultsFile.getAbsolutePath().replaceFirst(".properties$", ".local.properties"));
        if (localDefaultsFile.exists()) {
            final DefaultsFileValueProvider fileProvider = new DefaultsFileValueProvider(localDefaultsFile) {
                @Override
                public int getPrecedence() {
                    return super.getPrecedence() + 1;
                }
            };
            liquibaseConfiguration.registerProvider(fileProvider);
            returnList.add(fileProvider);
        } else {
            Scope.getCurrentScope().getLog(getClass()).fine("Cannot find local defaultsFile " + defaultsFile.getAbsolutePath());
        }

        return returnList;
    }

    /**
     * Configures the system, and returns values to add to Scope.
     *
     * @return values to set in the scope
     */
    private Map<String, Object> configureScope(String[] args) throws Exception {
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put(COMMAND_ARGUMENTS, args);

        final IntegrationDetails integrationDetails = new IntegrationDetails();
        integrationDetails.setName("cli");
        returnMap.put("integrationDetails", integrationDetails);

        final ClassLoader classLoader = configureClassLoader();

        returnMap.putAll(configureLogging());

        //
        // Pass any -D properties in the scope so that they will be available to
        // any logging that might happen
        //
        Map<String, String> javaProperties = addJavaPropertiesToChangelogParameters();
        Scope.child(new HashMap<>(javaProperties), () -> returnMap.putAll(configureResourceAccessor(classLoader)));

        UIService defaultUiService = getDefaultUiService();

        if (defaultUiService instanceof ConsoleUIService) {
            defaultUiService.setAllowPrompt(true);
            ((ConsoleUIService) defaultUiService).setOutputStream(System.err); //NOSONAR
            List<UIService> outputServices = new ArrayList<>();
            outputServices.add(defaultUiService);
            if (BooleanUtil.isTrue(LiquibaseCommandLineConfiguration.MIRROR_CONSOLE_MESSAGES_TO_LOG.getCurrentValue())) {
                outputServices.add(new LoggerUIService());
            }
            CompositeUIService compositeUIService = new CompositeUIService(defaultUiService, outputServices);
            returnMap.put(Scope.Attr.ui.name(), compositeUIService);
        } else {
            returnMap.put(Scope.Attr.ui.name(), defaultUiService);
        }

        returnMap.put(LiquibaseCommandLineConfiguration.ARGUMENT_CONVERTER.getKey(),
                (LiquibaseCommandLineConfiguration.ArgumentConverter) argument -> "--" + StringUtil.toKabobCase(argument));

        returnMap.put(Scope.JAVA_PROPERTIES, javaProperties);

        return returnMap;
    }

    private static UIService getDefaultUiService() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<UIService> uiServices = Scope.getCurrentScope().getServiceLocator().findInstances(UIService.class);
        Class<? extends UIService> configuredUiServiceClass = GlobalConfiguration.UI_SERVICE.getCurrentValue().getUiServiceClass();
        Optional<UIService> optionalDefaultUiService =
                uiServices.stream().filter(uiService -> configuredUiServiceClass.isAssignableFrom(uiService.getClass())).findFirst();

        if (optionalDefaultUiService.isPresent()) {
            return optionalDefaultUiService.get();
        } else {
            return GlobalConfiguration.UI_SERVICE.getCurrentValue().getUiServiceClass().getDeclaredConstructor().newInstance();
        }
    }

    private void configureVersionInfo() {
        getRootCommand(this.commandLine).getCommandSpec().versionProvider(new LiquibaseVersionProvider());
    }

    protected Map<String, Object> configureLogging() throws IOException {
        Map<String, Object> returnMap = new HashMap<>(1);
        final ConfiguredValue<Level> currentConfiguredValue = LiquibaseCommandLineConfiguration.LOG_LEVEL.getCurrentConfiguredValue();
        final String logFile = LiquibaseCommandLineConfiguration.LOG_FILE.getCurrentValue();

        Level logLevel = Level.OFF;
        if (!currentConfiguredValue.wasDefaultValueUsed()) {
            logLevel = currentConfiguredValue.getValue();
        }

        configureLogging(logLevel, logFile, currentConfiguredValue.wasDefaultValueUsed());

        return returnMap;
    }

    private void configureLogging(Level logLevel, String logFile) throws IOException {
        configureLogging(logLevel, logFile, false);
    }

    /**
     * @param wasDefaultLogLevelUsed indicate whether the user selected the default value for log-level, or whether they
     *                               provided a value
     */
    private void configureLogging(Level logLevel, String logFile, boolean wasDefaultLogLevelUsed) throws IOException {
        configuredLogLevel = logLevel;

        final LogService logService = Scope.getCurrentScope().get(Scope.Attr.logService, LogService.class);
        java.util.logging.Logger liquibaseLogger = java.util.logging.Logger.getLogger("liquibase");
        if (logService instanceof JavaLogService) {
            ((JavaLogService) logService).setParent(liquibaseLogger);
        }

        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] %4$s [%2$s] %5$s%6$s%n");

        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        Level cliLogLevel = logLevel;
        // If a log file is set, but no log level is set, this variable represents the log level that should be used.
        Level fileLogLevelOverride = null;

        if (logFile != null) {
            if (fileHandler == null) {
                final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
                OutputStream outputStream = pathHandlerFactory.openResourceOutputStream(logFile, new OpenOptions().setAppend(true));
                fileHandler = new StreamHandler(outputStream, new SimpleFormatter());

                JavaLogService.setFormatterOnHandler(logService, fileHandler);

                rootLogger.addHandler(fileHandler);
            }

            fileHandler.setLevel(logLevel);
            if (logLevel == Level.OFF && wasDefaultLogLevelUsed) {
                fileLogLevelOverride = Level.SEVERE;
                fileHandler.setLevel(fileLogLevelOverride);
            }

            cliLogLevel = Level.OFF;
        }

        final String configuredChannels = LiquibaseCommandLineConfiguration.LOG_CHANNELS.getCurrentValue();
        List<String> channels;
        if (configuredChannels.equalsIgnoreCase("all")) {
            channels = new ArrayList<>(Arrays.asList("", "liquibase"));
        } else {
            channels = StringUtil.splitAndTrim(configuredChannels, ",");

            if (logLevel == Level.OFF) {
                channels.add("");
            }
        }

        for (String channel : channels) {
            if (channel.equalsIgnoreCase("all")) {
                channel = "";
            }
            if (fileLogLevelOverride != null) {
                java.util.logging.Logger.getLogger(channel).setLevel(fileLogLevelOverride);
            } else {
                java.util.logging.Logger.getLogger(channel).setLevel(logLevel);
            }
        }

        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setLevel(cliLogLevel);
            }
            JavaLogService.setFormatterOnHandler(logService, handler);
        }
    }

    private CommandLine getRootCommand(CommandLine commandLine) {
        while (commandLine.getParent() != null) {
            commandLine = commandLine.getParent();
        }
        return commandLine;
    }

    private Map<String, Object> configureResourceAccessor(ClassLoader classLoader) throws IOException {
        Map<String, Object> returnMap = new HashMap<>();

        returnMap.put(Scope.Attr.resourceAccessor.name(), new SearchPathResourceAccessor(
                new DirectoryResourceAccessor(Paths.get(".").toAbsolutePath().toFile()),
                new ClassLoaderResourceAccessor(classLoader)));

        return returnMap;
    }

    protected ClassLoader configureClassLoader() throws IllegalArgumentException {
        final String classpath = LiquibaseCommandLineConfiguration.CLASSPATH.getCurrentValue();

        final List<URL> urls = new ArrayList<>();
        if (classpath != null) {
            String[] classpathSoFar;
            if (isWindows()) {
                classpathSoFar = classpath.split(";");
            } else {
                classpathSoFar = classpath.split(":");
            }

            for (String classpathEntry : classpathSoFar) {
                File classPathFile = new File(classpathEntry);
                if (!classPathFile.exists()) {
                    throw new IllegalArgumentException(classPathFile.getAbsolutePath() + " does not exist");
                }

                try {
                    URL newUrl = new File(classpathEntry).toURI().toURL();
                    Scope.getCurrentScope().getLog(getClass()).fine(newUrl.toExternalForm() + " added to class loader");
                    urls.add(newUrl);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        final ClassLoader classLoader;
        if (LiquibaseCommandLineConfiguration.INCLUDE_SYSTEM_CLASSPATH.getCurrentValue()) {
            classLoader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread()
                    .getContextClassLoader()));

        } else {
            classLoader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urls.toArray(new URL[0]), null));
        }

        Thread.currentThread().setContextClassLoader(classLoader);

        return classLoader;
    }

    private void addSubcommand(CommandDefinition commandDefinition, CommandLine rootCommand) {
        List<String[]> commandNames = expandCommandNames(commandDefinition);

        boolean showCommand = true;
        for (String[] commandName : commandNames) {

            final CommandRunner commandRunner = new CommandRunner();
            final CommandLine.Model.CommandSpec subCommandSpec = CommandLine.Model.CommandSpec.wrapWithoutInspection(commandRunner, defaultFactory);
            commandRunner.setSpec(subCommandSpec);

            configureHelp(subCommandSpec, false);

            //
            // Add to the usageMessage footer if the CommandDefinition has a footer
            //
            if (commandDefinition.getHelpFooter() != null) {
                String[] usageMessageFooter = subCommandSpec.usageMessage().footer();
                List<String> list = new ArrayList<>(Arrays.asList(usageMessageFooter));
                list.add(commandDefinition.getHelpFooter());
                subCommandSpec.usageMessage().footer(list.toArray(new String[0]));
            }

            String shortDescription = commandDefinition.getShortDescription();

            subCommandSpec.usageMessage()
                    .header(StringUtil.trimToEmpty(shortDescription) + "\n")
                    .description(StringUtil.trimToEmpty(commandDefinition.getLongDescription()));

            subCommandSpec.optionsCaseInsensitive(true);
            subCommandSpec.subcommandsCaseInsensitive(true);

            if (!showCommand) {
                subCommandSpec.usageMessage().hidden(true);
            } else {
                subCommandSpec.usageMessage().hidden(commandDefinition.getHidden());
            }
            showCommand = false;


            for (CommandArgumentDefinition<?> def : commandDefinition.getArguments().values()) {
                final String[] argNames = toArgNames(def);
                for (int i = 0; i < argNames.length; i++) {
                    final CommandLine.Model.OptionSpec.Builder builder = createArgBuilder(def, argNames[i]);

                    String argDisplaySuffix = "";
                    String argName = argNames[i];

                    //
                    // Determine if this is a group command and set the property/environment display strings accordingly
                    //
                    String description;
                    if (commandDefinition.getName().length > 1) {
                        String propertyStringToPresent = "\n(defaults file: 'liquibase.command." +
                                StringUtil.join(commandDefinition.getName(), ".") + "." + def.getName() + "'";
                        String envStringToPresent =
                                toEnvVariable("environment variable: 'liquibase.command." + StringUtil.join(commandDefinition.getName(), ".") +
                                        "." + def.getName()) + "')" + argDisplaySuffix;
                        description = propertyStringToPresent + " " + envStringToPresent;
                    } else {
                        String propertyStringToPresent = "\n(defaults file: 'liquibase.command." + def.getName() + "' OR 'liquibase.command." +
                                StringUtil.join(commandDefinition.getName(), ".") + "." + def.getName() + "'";
                        String envStringToPresent = ", environment variable: '" + toEnvVariable("liquibase.command." + def.getName()) + "' OR '" +
                                toEnvVariable("liquibase.command." + StringUtil.join(commandDefinition.getName(), ".") +
                                        "." + def.getName()) + "')" + argDisplaySuffix;
                        description = propertyStringToPresent + " " + envStringToPresent;
                    }

                    if (def.getDefaultValue() != null) {
                        if (def.getDefaultValueDescription() == null) {
                            description = "\nDEFAULT: " + def.getDefaultValue() + description;
                        } else {
                            description = "\nDEFAULT: " + def.getDefaultValueDescription() + description;
                        }
                    }

                    if (def.getDescription() != null) {
                        description = def.getDescription() + description;
                    }
                    if (def.isRequired()) {
                        description = "[REQUIRED] " + description;
                    }

                    builder.description(description + "\n");

                    if (def.getDataType().equals(Boolean.class)) {
                        builder.arity("0..1");
                    }


                    if (i > 0) {
                        builder.hidden(true);
                    } else {
                        builder.hidden(def.getHidden() && !LiquibaseCommandLineConfiguration.SHOW_HIDDEN_ARGS.getCurrentValue());
                    }

                    subCommandSpec.addOption(builder.build());

                    if (argName.equals("--changelog-file")) {
                        final CommandLine.Model.OptionSpec.Builder paramBuilder = (CommandLine.Model.OptionSpec.Builder) CommandLine.Model.OptionSpec.builder("-D")
                                .required(false)
                                .type(HashMap.class)
                                .description("Pass a name/value pair for substitution in the changelog(s)\nPass as -D<property.name>=<property.value>\n[deprecated: set changelog properties in defaults file or environment variables]")
                                .mapFallbackValue("");
                        subCommandSpec.add(paramBuilder.build());
                    }
                }
            }

            for (String legacyArg : legacyNoLongerCommandArguments) {
                final CommandLine.Model.OptionSpec.Builder builder = CommandLine.Model.OptionSpec.builder("--" + legacyArg)
                        .required(false)
                        .type(String.class)
                        .description("Legacy CLI argument")
                        .hidden(true);
                subCommandSpec.addOption(builder.build());
                String kabobArg = StringUtil.toKabobCase(legacyArg);
                if (!kabobArg.equals(legacyArg)) {
                    final CommandLine.Model.OptionSpec.Builder kabobOptionBuilder =
                            CommandLine.Model.OptionSpec.builder("--" + kabobArg)
                                    .required(false)
                                    .type(String.class)
                                    .hidden(true)
                                    .description("Legacy CLI argument");
                    subCommandSpec.addOption(kabobOptionBuilder.build());
                }
            }

            subCommandSpec.aliases(commandDefinition.getAliases().stream().map(cn -> cn[cn.length - 1]).collect(Collectors.toList()).toArray(new String[]{}));

            getParentCommandSpec(commandDefinition, rootCommand).addSubcommand(commandName[commandName.length - 1], new CommandLine(subCommandSpec, defaultFactory));
        }

    }

    private CommandLine.Model.OptionSpec.Builder createArgBuilder(CommandArgumentDefinition<?> def, String argName) {
        return CommandLine.Model.OptionSpec.builder(argName)
                .required(false)
                .converters(value -> {
                    if (def.getDataType().equals(Boolean.class)) {
                        if (value.equals("")) {
                            return "true";
                        }
                    }
                    return value;
                })
                .type(String.class);
    }

    private List<String[]> expandCommandNames(CommandDefinition commandDefinition) {
        List<String[]> returnList = new ArrayList<>();

        //create standard version first
        final String[] standardName = commandDefinition.getName().clone();
        for (int i = 0; i < standardName.length; i++) {
            standardName[i] = StringUtil.toKabobCase(commandDefinition.getName()[i]);
        }
        returnList.add(standardName);

        if (!StringUtil.join(standardName, " ").equals(StringUtil.join(commandDefinition.getName(), " "))) {
            returnList.add(commandDefinition.getName());
        }

        return returnList;
    }

    private CommandLine.Model.CommandSpec getParentCommandSpec(CommandDefinition commandDefinition, CommandLine rootCommand) {
        final String[] commandName = commandDefinition.getName();

        CommandLine.Model.CommandSpec parent = rootCommand.getCommandSpec();

        //length-1 to not include the actual command name
        for (int i = 0; i < commandName.length - 1; i++) {
            final CommandLine commandGroup = parent.subcommands().get(commandName[i]);
            final String[] groupName = Arrays.copyOfRange(commandName, 0, i + 1);

            if (commandGroup == null) {
                parent = addSubcommandGroup(groupName, commandDefinition, parent);
            } else {
                parent = commandGroup.getCommandSpec();
                if (commandDefinition.getGroupHelpFooter() != null) {
                    List<String> list = new ArrayList<>();
                    list.add(commandDefinition.getHelpFooter());
                    parent.usageMessage().footer(list.toArray(new String[0]));
                }
            }
            configureSubcommandGroup(parent, groupName, commandDefinition);
        }


        return parent;
    }

    private void configureSubcommandGroup(CommandLine.Model.CommandSpec groupSpec, String[] groupName, CommandDefinition commandDefinition) {
        final String header = StringUtil.trimToEmpty(commandDefinition.getGroupShortDescription(groupName));
        final String description = StringUtil.trimToEmpty(commandDefinition.getGroupLongDescription(groupName));

        if (!header.equals("")) {
            groupSpec.usageMessage().header("< " + header + " >\n");
        }

        if (!description.equals("")) {
            groupSpec.usageMessage().description(description + "\n");
        }
    }

    private CommandLine.Model.CommandSpec addSubcommandGroup(String[] groupName, CommandDefinition commandDefinition, CommandLine.Model.CommandSpec parent) {
        final CommandLine.Model.CommandSpec groupSpec = CommandLine.Model.CommandSpec.wrapWithoutInspection(null, defaultFactory);

        configureHelp(groupSpec, false);
        if (commandDefinition.getHelpFooter() != null) {
            String[] usageMessageFooter = groupSpec.usageMessage().footer();
            List<String> list = new ArrayList<>(Arrays.asList(usageMessageFooter));
            list.add(commandDefinition.getHelpFooter());
            groupSpec.usageMessage().footer(list.toArray(new String[0]));
        }

        groupSpec.optionsCaseInsensitive(true);
        groupSpec.subcommandsCaseInsensitive(true);

        parent.addSubcommand(groupName[groupName.length - 1], groupSpec);

        return groupSpec;
    }

    private String toEnvVariable(String property) {
        return StringUtil.toKabobCase(property).replace(".", "_").replace("-", "_").toUpperCase();
    }

    private SortedSet<CommandDefinition> getCommands() {
        final CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);
        return commandFactory.getCommands(false);
    }

    private void addGlobalArguments(CommandLine commandLine) {
        final CommandLine.Model.CommandSpec rootCommandSpec = commandLine.getCommandSpec();

        final SortedSet<ConfigurationDefinition<?>> globalConfigurations = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getRegisteredDefinitions(false);
        for (ConfigurationDefinition<?> def : globalConfigurations) {
            final String[] argNames = toArgNames(def);
            for (int i = 0; i < argNames.length; i++) {
                final CommandLine.Model.OptionSpec.Builder optionBuilder = CommandLine.Model.OptionSpec.builder(argNames[i])
                        .required(false)
                        .type(String.class);
                String description = "(defaults file: '" + def.getKey() + "', environment variable: '"
                        + toEnvVariable(def.getKey()) + "')";

                if (def.getDefaultValue() != null) {
                    if (def.getDefaultValueDescription() == null) {
                        description = "DEFAULT: " + def.getDefaultValue() + "\n" + description;
                    } else {
                        description = "DEFAULT: " + def.getDefaultValueDescription() + "\n" + description;
                    }
                }

                if (def.getDescription() != null) {
                    description = def.getDescription() + "\n" + description;
                }

                optionBuilder.description(description + "\n");

                if (def.getDataType().equals(Boolean.class)) {
                    optionBuilder.arity("1");
                }

                //only show the first/standard variation of a name
                if (i > 0 || (def.isHidden() && !LiquibaseCommandLineConfiguration.SHOW_HIDDEN_ARGS.getCurrentValue())) {
                    optionBuilder.hidden(true);
                }

                final CommandLine.Model.OptionSpec optionSpec = optionBuilder.build();
                rootCommandSpec.addOption(optionSpec);
            }
        }

        //
        // We add both camel and Kabob case style arguments
        //
        for (String arg : legacyNoLongerGlobalArguments) {
            final CommandLine.Model.OptionSpec.Builder optionBuilder =
                    CommandLine.Model.OptionSpec.builder("--" + arg)
                            .required(false)
                            .type(String.class)
                            .hidden(true)
                            .description("Legacy global argument");
            rootCommandSpec.addOption(optionBuilder.build());
            String kabobArg = StringUtil.toKabobCase(arg);
            if (!kabobArg.equals(arg)) {
                final CommandLine.Model.OptionSpec.Builder kabobOptionBuilder =
                        CommandLine.Model.OptionSpec.builder("--" + kabobArg)
                                .required(false)
                                .type(String.class)
                                .hidden(true)
                                .description("Legacy global argument");
                rootCommandSpec.addOption(kabobOptionBuilder.build());
            }
        }
    }

    private void configureHelp(CommandLine.Model.CommandSpec commandSpec, boolean includeVersion) {
        String footer = "Each argument contains the corresponding 'configuration key' in parentheses. " +
                "As an alternative to passing values on the command line, these keys can be used as a basis for configuration settings in other locations.\n\n" +
                "Available configuration locations, in order of priority:\n" +
                "- Command line arguments (argument name in --help)\n" +
                "- Java system properties (configuration key listed above)\n" +
                "- Environment values (env variable listed above)\n" +
                "- Defaults file (configuration key OR argument name)\n\n" +
                "Full documentation is available at\n" +
                "https://docs.liquibase.com";


        commandSpec.addOption(CommandLine.Model.OptionSpec.builder("--help", "-h")
                .description("Show this help message and exit\n")
                .usageHelp(true)
                .build());

        if (includeVersion) {
            commandSpec.addOption(CommandLine.Model.OptionSpec.builder("--version", "-v")
                    .description("Print version information and exit\n")
                    .versionHelp(true)
                    .build());
        }


        commandSpec.usageMessage()
                .showDefaultValues(false)
                .sortOptions(true)
                .abbreviateSynopsis(true)
                .footer("\n" + footer)
        ;

    }

    protected static String[] toArgNames(CommandArgumentDefinition<?> def) {
        //
        // Use an ordered Set so that the command name shows up first
        // and is listed as the argument in the help
        //
        LinkedHashSet<String> returnList = new LinkedHashSet<>();
        Set<String> baseNames = new LinkedHashSet<>();
        baseNames.add(def.getName());
        baseNames.addAll(def.getAliases());

        for (String baseName : baseNames) {
            returnList.add("--" + StringUtil.toKabobCase(baseName).replace(".", "-"));
            returnList.add("--" + baseName.replace("\\.", ""));
        }

        return returnList.toArray(new String[0]);
    }

    protected static String[] toArgNames(ConfigurationDefinition<?> def) {
        List<String> keys = new ArrayList<>();
        keys.add(def.getKey());
        keys.addAll(def.getAliasKeys());

        List<String> returns = new CaseInsensitiveList();
        for (String key : keys) {
            insertWithoutDuplicates(returns, "--" + StringUtil.toKabobCase(key.replaceFirst("^liquibase.", "")).replace(".", "-"));
            insertWithoutDuplicates(returns, "--" + StringUtil.toKabobCase(key.replace(".", "-")));
            insertWithoutDuplicates(returns, "--" + key.replaceFirst("^liquibase.", "").replaceAll("\\.", ""));
            insertWithoutDuplicates(returns, "--" + key.replaceAll("\\.", ""));
        }

        return returns.toArray(new String[0]);
    }

    private static class CaseInsensitiveList extends ArrayList<String> {
        @Override
        public boolean contains(Object o) {
            String paramStr = (String) o;
            for (String s : this) {
                if (paramStr.equalsIgnoreCase(s)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static void insertWithoutDuplicates(List<String> returnList, String key) {
        if (returnList.contains(key)) {
            return;
        }
        returnList.add(key);
    }

    public static class LiquibaseVersionProvider implements CommandLine.IVersionProvider {

        @Override
        public String[] getVersion() throws Exception {
            Version mdcVersion = new Version();
            final LicenseService licenseService = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService();
            String licenseInfo = "";
            if (licenseService == null) {
                licenseInfo = "WARNING: License service not loaded, cannot determine Liquibase Pro license status. Please consider re-installing Liquibase to include all dependencies. Continuing operation without Pro license.";
            } else {
                licenseInfo = licenseService.getLicenseInfo();
            }

            final Path workingDirectory = Paths.get(".").toAbsolutePath();

            String liquibaseHome;
            Path liquibaseHomePath = null;
            try {
                liquibaseHomePath = getLiquibaseHomePath(workingDirectory);
                liquibaseHome = liquibaseHomePath.toString();
            } catch (IOException e) {
                liquibaseHome = "Cannot resolve LIQUIBASE_HOME: " + e.getMessage();
            }

            Map<String, LibraryInfo> libraryInfo = getLibraryInfoMap();

            final StringBuilder libraryDescription = new StringBuilder("Libraries:\n");
            if (libraryInfo.size() == 0) {
                libraryDescription.append("- UNKNOWN");
            } else {
                List<String> libraries = listLibraries(libraryInfo, liquibaseHomePath, workingDirectory, mdcVersion);

                for (String library : libraries) {
                    libraryDescription.append("- ")
                            .append(library)
                            .append("\n");
                }
            }
            String javaHome = System.getProperties().getProperty("java.home");
            String javaVersion = System.getProperty("java.version");
            addMdc(mdcVersion, licenseService, liquibaseHome, javaHome, javaVersion);

            return new String[]{
                    CommandLineUtils.getBanner(),
                    String.format("Liquibase Home: %s", liquibaseHome),
                    String.format("Java Home %s (Version %s)",
                            javaHome,
                            javaVersion
                    ),
                    libraryDescription.toString(),
                    "",
                    "Liquibase Version: " + LiquibaseUtil.getBuildVersionInfo(),
                    licenseInfo,
            };
        }

        private void addMdc(Version mdcVersion, LicenseService licenseService, String liquibaseHome, String javaHome, String javaVersion) {
            mdcVersion.setJavaHome(new Version.JavaHome(javaHome, javaVersion));
            Banner banner = new Banner();
            LicenseInfo licenseInfoObject = null;
            if (licenseService != null) {
                licenseInfoObject = licenseService.getLicenseInfoObject();
            }
            if (licenseInfoObject != null) {
                banner.setLicensee(licenseInfoObject.getIssuedTo());
                banner.setLicenseEndDate(licenseInfoObject.formatExpirationDate());
            }
            banner.setPath(liquibaseHome);
            mdcVersion.setLiquibaseVersion(banner);
            try (MdcObject version = Scope.getCurrentScope().addMdcValue(MdcKey.VERSION, mdcVersion)) {
                Scope.getCurrentScope().getLog(getClass()).info("Generated version info");
            }
        }
    }

}
