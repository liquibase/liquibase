package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.configuration.*;
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

import static liquibase.util.SystemUtil.isWindows;


public class LiquibaseCommandLine {

    private Level configuredLogLevel;

    private final CommandLine commandLine;
    private FileHandler fileHandler;

    public static void main(String[] args) {
        final LiquibaseCommandLine cli = new LiquibaseCommandLine();

        try {
            cli.execute(args);
        } catch (Throwable e) {
            cli.handleException(e);
        } finally {
            cli.cleanup();
        }
    }

    private void cleanup() {
        if (fileHandler != null) {
            fileHandler.flush();
            fileHandler.close();
        }
    }

    public LiquibaseCommandLine() {
        this.commandLine = buildPicoCommandLine();
    }

    private CommandLine buildPicoCommandLine() {
        final CommandLine.Model.CommandSpec rootCommandSpec = CommandLine.Model.CommandSpec.create();
        rootCommandSpec.name("liquibase");
        configureHelp(rootCommandSpec);
        rootCommandSpec.subcommandsCaseInsensitive(true);


        rootCommandSpec.usageMessage()
                .customSynopsis("liquibase [GLOBAL OPTIONS] [COMMAND] [COMMAND OPTIONS]\nCommand-specific help: \"liquibase <command-name> --help\"")
                .optionListHeading("\nGlobal Options\n")
                .commandListHeading("\nCommands\n")
        ;


        CommandLine commandLine = new CommandLine(rootCommandSpec)
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

        //clean up message
        bestMessage = bestMessage.replaceFirst("^[\\w.]*exception[\\w.]*: ", "");
        bestMessage = bestMessage.replace("Unexpected error running Liquibase: ", "");

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

        return -1;
    }

    public void execute(String[] args) throws Exception {
        configureLogging(Level.OFF, null);

        Main.runningFromNewCli = true;

        final List<ConfigurationValueProvider> valueProviders = registerValueProviders(args);
        try {
            Scope.child(configureScope(), () -> {
                configureVersionInfo();

                commandLine.execute(args);

                Scope.getCurrentScope().getUI().sendMessage("Liquibase command '" + StringUtil.join(getCommandNames(commandLine.getParseResult()), " ") + "' was executed successfully.");
            });
        } finally {
            final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

            for (ConfigurationValueProvider provider : valueProviders) {
                liquibaseConfiguration.unregisterProvider(provider);
            }
        }
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

        final CommandLineArgumentValueProvider argumentProvider = new CommandLineArgumentValueProvider(commandLine.parseArgs(args));
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
        final CommandRunner commandRunner = new CommandRunner();
        final CommandLine.Model.CommandSpec subCommandSpec = CommandLine.Model.CommandSpec.wrapWithoutInspection(commandRunner);
        commandRunner.setSpec(subCommandSpec);

        configureHelp(subCommandSpec);

        subCommandSpec.usageMessage()
                .header(StringUtil.trimToEmpty(commandDefinition.getShortDescription()) + "\n")
                .description(StringUtil.trimToEmpty(commandDefinition.getLongDescription()));

        subCommandSpec.optionsCaseInsensitive(true);
        subCommandSpec.subcommandsCaseInsensitive(true);

        for (CommandArgumentDefinition<?> def : commandDefinition.getArguments().values()) {
            final String[] argNames = toArgNames(def);
            for (int i = 0; i < argNames.length; i++) {
                final CommandLine.Model.OptionSpec.Builder builder = CommandLine.Model.OptionSpec.builder(argNames[i])
                        .required(false)
                        .type(def.getDataType());


                String description = "(liquibase.command." + def.getName() + " OR liquibase.command." + StringUtil.join(commandDefinition.getName(), ".") + "." + def.getName() + ")\n" +
                        "(" + toEnvVariable("liquibase.command." + def.getName()) + " OR " + toEnvVariable("liquibase.command." + StringUtil.join(commandDefinition.getName(), ".") + "." + def.getName()) + ")";

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

                if (def.isRequired()) {
                    description += "\n[REQUIRED]";
                }
                builder.description(description + "\n");


                if (def.getDataType().equals(Boolean.class)) {
                    builder.arity("1");
                }


                if (i > 0) {
                    builder.hidden(true);
                }

                subCommandSpec.addOption(builder.build());
            }
        }

        commandLine.getCommandSpec().addSubcommand(StringUtil.toKabobCase(commandDefinition.getName()[0]), subCommandSpec);
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
                        .type(def.getDataType());

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

                final ConfigurationValueConverter<?> valueHandler = def.getValueHandler();
                if (valueHandler != null) {
                    optionBuilder.converters(valueHandler::convert);
                }

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
    }

    private void configureHelp(CommandLine.Model.CommandSpec commandSpec) {
        String footer = "Each argument contains the corresponding 'configuration key' in parentheses. " +
                "As an alternative to passing values on the command line, these keys can be used as a basis for configuration settings in other locations.\n\n" +
                "Available configuration locations, in order of priority:\n" +
                "- Command line arguments (argument name in --help)\n" +
                "- Java system properties (configuration key listed above)\n" +
                "- Environment values (env variable listed above)\n" +
                "- Defaults file (configuration key OR argument name)\n\n" +
                "Full documentation is available at\n" +
                "http://www.liquibase.org";


        commandSpec.mixinStandardHelpOptions(true);
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
