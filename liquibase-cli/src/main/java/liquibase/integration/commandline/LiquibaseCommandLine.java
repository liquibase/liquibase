package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.core.DefaultsFileValueProvider;
import liquibase.exception.CommandLineParsingException;
import liquibase.exception.CommandValidationException;
import liquibase.hub.HubConfiguration;
import liquibase.integration.IntegrationConfiguration;
import liquibase.license.LicenseServiceFactory;
import liquibase.logging.LogMessageFilter;
import liquibase.logging.LogService;
import liquibase.logging.core.JavaLogService;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.ui.ConsoleUIService;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.ResourceBundle.getBundle;
import static liquibase.util.SystemUtil.isWindows;


public class LiquibaseCommandLine {

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
    private FileHandler fileHandler;

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
            fileHandler.close();
        }
    }

    public LiquibaseCommandLine() {
        this.legacyPositionalArguments = new HashMap<>();
        this.legacyPositionalArguments.put("tag", "tag");
        this.legacyPositionalArguments.put("rollback", "tag");
        this.legacyPositionalArguments.put("rollbacksql", "tag");
        this.legacyPositionalArguments.put("rollbacktodate", "date");
        this.legacyPositionalArguments.put("rollbacktodatesql", "date");
        this.legacyPositionalArguments.put("rollbackcount", "count");
        this.legacyPositionalArguments.put("rollbackcountsql", "count");
        this.legacyPositionalArguments.put("futurerollbackcount", "count");
        this.legacyPositionalArguments.put("futurerollbackcountsql", "count");
        this.legacyPositionalArguments.put("futurerollbackfromtag", "tag");
        this.legacyPositionalArguments.put("futurerollbackfromtagsql", "tag");

        this.legacyNoLongerGlobalArguments = Stream.of(
                "username",
                "password",
                "url",
                "outputDefaultSchema",
                "outputDefaultCatalog",
                "changeLogFile",
                "hubConnectionId",
                "contexts",
                "labels",
                "diffTypes",
                "changeSetAuthor",
                "changeSetContext",
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
                "rollbackScript"
        ).collect(Collectors.toSet());

        this.legacyNoLongerCommandArguments = Stream.of(
                "driver",
                "databaseClass",
                "liquibaseCatalogName",
                "liquibaseSchemaName",
                "databaseChangeLogTableName",
                "databaseChangeLogLockTableName",
                "databaseChangeLogTablespaceName",
                "overwriteOutputFile",
                "classpath",
                "driverPropertiesFile",
                "propertyProviderClass",
                "promptForNonLocalDatabase",
                "includeSystemClasspath",
                "defaultsFile",
                "currentDateTimeFunction",
                "logLevel",
                "logFile",
                "outputFile",
                "liquibaseProLicenseKey",
                "liquibaseHubApiKey"
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

        return commandLine;
    }

    private int handleException(Throwable exception) {
        Throwable cause = exception;

        String bestMessage = exception.getMessage();
        while (cause.getCause() != null) {
            if (StringUtil.trimToNull(cause.getMessage()) != null) {
                bestMessage = cause.getMessage();
            }
            cause = cause.getCause();
        }

        if (bestMessage == null) {
            bestMessage = exception.getClass().getName();
        } else {
            //clean up message
            bestMessage = bestMessage.replaceFirst("^[\\w.]*exception[\\w.]*: ", "");
            bestMessage = bestMessage.replace("Unexpected error running Liquibase: ", "");
        }

        Scope.getCurrentScope().getLog(getClass()).severe(bestMessage, exception);

        boolean printUsage = false;
        if (exception instanceof CommandLine.ParameterException) {
            if (exception instanceof CommandLine.UnmatchedArgumentException) {
                System.err.println("Unexpected argument(s): " + StringUtil.join(((CommandLine.UnmatchedArgumentException) exception).getUnmatched(), ", "));
            } else {
                System.err.println("Error parsing command line: " + bestMessage);
            }
            CommandLine.UnmatchedArgumentException.printSuggestions((CommandLine.ParameterException) exception, System.err);

            printUsage = true;
        } else if (exception instanceof IllegalArgumentException
                || exception instanceof CommandValidationException
                || exception instanceof CommandLineParsingException) {
            System.err.println("Error parsing command line: " + bestMessage);
            printUsage = true;
        } else {
            System.err.println("Unexpected error running Liquibase: " + bestMessage);
            System.err.println();

            if (Level.OFF.equals(this.configuredLogLevel)) {
                System.err.println("For more information, please use the --log-level flag");
            } else {
                if (IntegrationConfiguration.LOG_FILE.getCurrentValue() == null) {
                    exception.printStackTrace(System.err);
                }
            }
        }

        if (printUsage) {
            System.err.println();
            System.err.println("For detailed help, try 'liquibase --help' or 'liquibase <command-name> --help'");
        }

        return 1;
    }

    public int execute(String[] args) {
        try {
            final String[] finalArgs = adjustLegacyArgs(args);

            configureLogging(Level.OFF, null);

            Main.runningFromNewCli = true;

            final List<ConfigurationValueProvider> valueProviders = registerValueProviders(finalArgs);
            try {
                return Scope.child(configureScope(), () -> {

                    if (!IntegrationConfiguration.SHOULD_RUN.getCurrentValue()) {
                        Scope.getCurrentScope().getUI().sendErrorMessage((
                                String.format(coreBundle.getString("did.not.run.because.param.was.set.to.false"),
                                        IntegrationConfiguration.SHOULD_RUN.getCurrentConfiguredValue().getProvidedValue().getActualKey())));
                        return 0;
                    }

                    configureVersionInfo();

                    if (!wasHelpOrVersionRequeted()) {
                        Scope.getCurrentScope().getUI().sendMessage(CommandLineUtils.getBanner());
                        Scope.getCurrentScope().getUI().sendMessage(String.format(coreBundle.getString("version.number"), LiquibaseUtil.getBuildVersion()));
                    }

                    int response = commandLine.execute(finalArgs);

                    if (response == 0) {
                        final String commandName = StringUtil.join(getCommandNames(commandLine.getParseResult()), " ");
                        if (!wasHelpOrVersionRequeted()) {
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
            }
        } catch (Throwable e) {
            handleException(e);
            return 1;
        } finally {
            cleanup();
        }
    }

    private boolean wasHelpOrVersionRequeted() {
        CommandLine.ParseResult parseResult = commandLine.getParseResult();

        while (parseResult != null) {
            if (parseResult.isUsageHelpRequested() || parseResult.isVersionHelpRequested()) {
                return true;
            }
            parseResult = parseResult.subcommand();
        }

        return false;
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

    static String[] getCommandNames(CommandLine.ParseResult parseResult) {
        List<String> returnList = new ArrayList<>();
        for (CommandLine command : parseResult.asCommandLineList()) {
            final String commandName = command.getCommandName();
            if (commandName.equals("liquibase")) {
                continue;
            }
            returnList.add(commandName);
        }
        return returnList.toArray(new String[0]);
    }

    private List<ConfigurationValueProvider> registerValueProviders(String[] args) {
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
        List<ConfigurationValueProvider> returnList = new ArrayList<>();

        final CommandLineArgumentValueProvider argumentProvider = new CommandLineArgumentValueProvider(commandLine.parseArgs(args), legacyNoLongerCommandArguments);
        liquibaseConfiguration.registerProvider(argumentProvider);
        returnList.add(argumentProvider);

        final File defaultsFile = new File(IntegrationConfiguration.DEFAULTS_FILE.getCurrentValue());
        if (defaultsFile.exists()) {
            final DefaultsFileValueProvider fileProvider = new DefaultsFileValueProvider(defaultsFile);
            liquibaseConfiguration.registerProvider(fileProvider);
            returnList.add(argumentProvider);
        }


        return returnList;
    }

    /**
     * Configures the system, and returns values to add to Scope.
     *
     * @return values to set in the scope
     */
    private Map<String, Object> configureScope() throws Exception {
        Map<String, Object> returnMap = new HashMap<>();

        final ClassLoader classLoader = configureClassLoader();

        returnMap.putAll(configureLogging());
        returnMap.putAll(configureResourceAccessor(classLoader));

        ConsoleUIService ui = new ConsoleUIService();
        ui.setAllowPrompt(true);
        ui.setOutputStream(System.err);
        returnMap.put(Scope.Attr.ui.name(), ui);


        return returnMap;
    }

    private void configureVersionInfo() {
        getRootCommand(this.commandLine).getCommandSpec().version(
                CommandLineUtils.getBanner(),
                String.format("Running Java under %s (Version %s)",
                        System.getProperties().getProperty("java.home"),
                        System.getProperty("java.version")
                ),
                "",
                "Liquibase Version: " + LiquibaseUtil.getBuildVersion(),
                Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService().getLicenseInfo()
        );
    }

    protected Map<String, Object> configureLogging() throws IOException {
        Map<String, Object> returnMap = new HashMap<>();
        final ConfiguredValue<Level> currentConfiguredValue = IntegrationConfiguration.LOG_LEVEL.getCurrentConfiguredValue();
        final File logFile = IntegrationConfiguration.LOG_FILE.getCurrentValue();

        Level logLevel = Level.OFF;
        if (!ConfigurationDefinition.wasDefaultValueUsed(currentConfiguredValue)) {
            logLevel = currentConfiguredValue.getValue();
        }

        configureLogging(logLevel, logFile);

        //
        // Set the Liquibase Hub log level if logging is not OFF
        //
        if (logLevel != Level.OFF) {
            returnMap.put(HubConfiguration.LIQUIBASE_HUB_LOGLEVEL.getKey(), logLevel);
        }

        return returnMap;
    }

    private void configureLogging(Level logLevel, File logFile) throws IOException {
        configuredLogLevel = logLevel;

        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] %4$s [%2$s] %5$s%6$s%n");

        java.util.logging.Logger liquibaseLogger = java.util.logging.Logger.getLogger("liquibase");

        final JavaLogService logService = (JavaLogService) Scope.getCurrentScope().get(Scope.Attr.logService, LogService.class);
        logService.setParent(liquibaseLogger);


        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");

        Level cliLogLevel = logLevel;

        if (logFile != null) {
            if (fileHandler == null) {
                fileHandler = new FileHandler(logFile.getAbsolutePath(), true);
                fileHandler.setFormatter(new SimpleFormatter());
                rootLogger.addHandler(fileHandler);
            }

            fileHandler.setLevel(logLevel);
            if (logLevel == Level.OFF) {
                fileHandler.setLevel(Level.FINE);
            }

            cliLogLevel = Level.OFF;
        }

        rootLogger.setLevel(logLevel);
        liquibaseLogger.setLevel(logLevel);

        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setLevel(cliLogLevel);
            }

            handler.setFilter(new SecureLogFilter(logService.getFilter()));
        }
    }

    private CommandLine getRootCommand(CommandLine commandLine) {
        while (commandLine.getParent() != null) {
            commandLine = commandLine.getParent();
        }
        return commandLine;
    }

    private Map<String, Object> configureResourceAccessor(ClassLoader classLoader) {
        Map<String, Object> returnMap = new HashMap<>();

        returnMap.put(Scope.Attr.resourceAccessor.name(), new CompositeResourceAccessor(new FileSystemResourceAccessor(Paths.get(".").toAbsolutePath().toFile()), new CommandLineResourceAccessor(classLoader)));

        return returnMap;
    }

    protected ClassLoader configureClassLoader() throws IllegalArgumentException {
        final String classpath = IntegrationConfiguration.CLASSPATH.getCurrentValue();

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
                    throw new IllegalArgumentException(classPathFile.getAbsolutePath() + " does.not.exist");
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
        if (IntegrationConfiguration.INCLUDE_SYSTEM_CLASSPATH.getCurrentValue()) {
            classLoader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread()
                    .getContextClassLoader()));

        } else {
            classLoader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urls.toArray(new URL[0]), null));
        }

        Thread.currentThread().setContextClassLoader(classLoader);

        return classLoader;
    }

    private void addSubcommand(CommandDefinition commandDefinition, CommandLine commandLine) {
        Set<String> commandNames = new LinkedHashSet<String>();
        commandNames.add(StringUtil.toKabobCase(commandDefinition.getName()[0]));
        commandNames.add(commandDefinition.getName()[0]);

        boolean documentedName = true;
        for (String commandName : commandNames) {

            final CommandRunner commandRunner = new CommandRunner();
            final CommandLine.Model.CommandSpec subCommandSpec = CommandLine.Model.CommandSpec.wrapWithoutInspection(commandRunner, defaultFactory);
            commandRunner.setSpec(subCommandSpec);

            configureHelp(subCommandSpec, false);

            subCommandSpec.usageMessage()
                    .header(StringUtil.trimToEmpty(commandDefinition.getShortDescription()) + "\n")
                    .description(StringUtil.trimToEmpty(commandDefinition.getLongDescription()));

            subCommandSpec.optionsCaseInsensitive(true);
            subCommandSpec.subcommandsCaseInsensitive(true);

            subCommandSpec.usageMessage().hidden(!documentedName);

            for (CommandArgumentDefinition<?> def : commandDefinition.getArguments().values()) {
                final String[] argNames = toArgNames(def);
                for (int i = 0; i < argNames.length; i++) {
                    final CommandLine.Model.OptionSpec.Builder builder = CommandLine.Model.OptionSpec.builder(argNames[i])
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


                    String description = "\n(liquibase.command." + def.getName() + " OR liquibase.command." + StringUtil.join(commandDefinition.getName(), ".") + "." + def.getName() + ")\n" +
                            "(" + toEnvVariable("liquibase.command." + def.getName()) + " OR " + toEnvVariable("liquibase.command." + StringUtil.join(commandDefinition.getName(), ".") + "." + def.getName()) + ")";

                    if (def.getDefaultValue() != null) {
                        if (def.getDefaultValueDescription() == null) {
                            description = "\nDEFAULT: " + def.getDefaultValue() + "\n" + description;
                        } else {
                            description = "\nDEFAULT: " + def.getDefaultValueDescription() + "\n" + description;
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
                    }

                    subCommandSpec.addOption(builder.build());
                }
            }

            for (String legacyArg : legacyNoLongerCommandArguments) {
                final CommandLine.Model.OptionSpec.Builder builder = CommandLine.Model.OptionSpec.builder("--" + legacyArg)
                        .required(false)
                        .type(String.class)
                        .description("Legacy CLI argument")
                        .hidden(true);
                subCommandSpec.addOption(builder.build());
            }

            commandLine.getCommandSpec().addSubcommand(commandName, new CommandLine(subCommandSpec, defaultFactory));

            //rest of the iterations don't document the commandName
            documentedName = false;
        }

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

        final SortedSet<ConfigurationDefinition<?>> globalConfigurations = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getRegisteredDefinitions();
        for (ConfigurationDefinition<?> def : globalConfigurations) {
            final String[] argNames = toArgNames(def);
            for (int i = 0; i < argNames.length; i++) {
                final CommandLine.Model.OptionSpec.Builder optionBuilder = CommandLine.Model.OptionSpec.builder(argNames[i])
                        .required(false)
                        .type(String.class);

                String description = "(" + def.getKey() + ")\n"
                        + "(" + toEnvVariable(def.getKey()) + ")";

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
                if (i > 0) {
                    optionBuilder.hidden(true);
                }

                final CommandLine.Model.OptionSpec optionSpec = optionBuilder.build();
                rootCommandSpec.addOption(optionSpec);
            }
        }

        for (String arg : legacyNoLongerGlobalArguments) {
            final CommandLine.Model.OptionSpec.Builder optionBuilder = CommandLine.Model.OptionSpec.builder("--" + arg)
                    .required(false)
                    .type(String.class)
                    .hidden(true)
                    .description("Legacy global argument");

            rootCommandSpec.addOption(optionBuilder.build());
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
                "http://www.liquibase.org";


        commandSpec.addOption(CommandLine.Model.OptionSpec.builder("--help", "-h")
                .description("Show this help message and exit")
                .usageHelp(true)
                .build());

        if (includeVersion) {
            commandSpec.addOption(CommandLine.Model.OptionSpec.builder("--version", "-v")
                    .description("Print version information and exit")
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
        LinkedHashSet<String> returnList = new LinkedHashSet<>();
        returnList.add("--" + StringUtil.toKabobCase(def.getName()).replace(".", "-"));
        returnList.add("--" + def.getName().replaceAll("\\.", ""));

        return returnList.toArray(new String[0]);
    }

    protected static String[] toArgNames(ConfigurationDefinition<?> def) {
        LinkedHashSet<String> returnList = new LinkedHashSet<>();
        returnList.add("--" + StringUtil.toKabobCase(def.getKey().replaceFirst("^liquibase.", "")).replace(".", "-"));
        returnList.add("--" + StringUtil.toKabobCase(def.getKey()).replace(".", "-"));
        returnList.add("--" + def.getKey().replaceFirst("^liquibase.", "").replaceAll("\\.", ""));

        return returnList.toArray(new String[0]);
    }

    public static class SecureLogFilter implements Filter {

        private LogMessageFilter filter;

        public SecureLogFilter(LogMessageFilter filter) {
            this.filter = filter;
        }

        @Override
        public boolean isLoggable(LogRecord record) {
            final String filteredMessage = filter.filterMessage(record.getMessage());

            final boolean equals = filteredMessage.equals(record.getMessage());
            return equals;
        }
    }
}
