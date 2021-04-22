package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.configuration.*;
import liquibase.configuration.core.DefaultsFileValueProvider;
import liquibase.exception.CommandLineParsingException;
import liquibase.hub.HubConfiguration;
import liquibase.integration.IntegrationConfiguration;
import liquibase.license.LicenseServiceFactory;
import liquibase.logging.LogMessageFilter;
import liquibase.logging.LogService;
import liquibase.logging.core.JavaLogService;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
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

    private final CommandLine commandLine;
    private FileHandler fileHandler;

    public static void main(String[] args) {
        final LiquibaseCommandLine cli = new LiquibaseCommandLine();

        try {
            cli.execute(args);
        } catch (Exception e) {
            e.printStackTrace();
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

        CommandLine commandLine = new CommandLine(rootCommandSpec);

        addGlobalArguments(commandLine);

        for (CommandDefinition commandDefinition : getCommands()) {
            addSubcommand(commandDefinition, commandLine);
        }

        return commandLine;
    }

    public void execute(String[] args) throws Exception {
        configureLogging(Level.OFF, null);

        Main.runningFromNewCli = true;

        final List<ConfigurationValueProvider> valueProviders = registerValueProviders(args);
        try {
            Scope.child(configureScope(), () -> {
                configureVersionInfo();

                commandLine.execute(args);
            });
        } finally {
            final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

            for (ConfigurationValueProvider provider : valueProviders) {
                liquibaseConfiguration.unregisterProvider(provider);
            }
        }
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

    protected ClassLoader configureClassLoader() throws CommandLineParsingException {
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
                    throw new CommandLineParsingException(classPathFile.getAbsolutePath() + " does.not.exist");
                }

                try {
                    URL newUrl = new File(classpathEntry).toURI().toURL();
                    Scope.getCurrentScope().getLog(getClass()).fine(newUrl.toExternalForm() + " added to class loader");
                    urls.add(newUrl);
                } catch (MalformedURLException e) {
                    throw new CommandLineParsingException(e);
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

        for (CommandArgumentDefinition<?> def : commandDefinition.getArguments().values()) {
            final CommandLine.Model.OptionSpec.Builder builder = CommandLine.Model.OptionSpec.builder(toArgName(def))
                    .required(false) //.required(def.isRequired())
                    .type(def.getDataType());

            if (def.getDescription() != null) {
                builder.description(def.getDescription());
            }

            if (def.getDefaultValueDescription() != null) {
                builder.defaultValue(def.getDefaultValueDescription());
            }

            subCommandSpec.addOption(builder.build());
        }

        commandLine.getCommandSpec().addSubcommand(StringUtil.toKabobCase(commandDefinition.getName()[0]), subCommandSpec);
    }

    private SortedSet<CommandDefinition> getCommands() {
        final CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);
        return commandFactory.getCommands();
    }

    private void addGlobalArguments(CommandLine commandLine) {
        final CommandLine.Model.CommandSpec rootCommandSpec = commandLine.getCommandSpec();

        final SortedSet<ConfigurationDefinition<?>> globalConfigurations = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getRegisteredDefinitions();
        for (ConfigurationDefinition<?> def : globalConfigurations) {
            final CommandLine.Model.OptionSpec.Builder optionBuilder = CommandLine.Model.OptionSpec.builder(toArgName(def))
                    .required(false)
                    .defaultValue(def.getDefaultValueDescription())
                    .type(def.getType());

            if (def.getDescription() != null) {
                optionBuilder.description(def.getDescription());
            }

            final ConfigurationValueConverter<?> valueHandler = def.getValueHandler();
            if (valueHandler != null) {
                optionBuilder.converters(valueHandler::convert);
            }

            final CommandLine.Model.OptionSpec optionSpec = optionBuilder.build();
            rootCommandSpec.addOption(optionSpec);
        }
    }

    private void configureHelp(CommandLine.Model.CommandSpec commandSpec) {
        commandSpec.mixinStandardHelpOptions(true);
        commandSpec.usageMessage()
                .showDefaultValues(true)
                .sortOptions(true)
                .abbreviateSynopsis(true)
        ;
    }

    private static String toArgName(CommandArgumentDefinition<?> def) {
        return "--" + StringUtil.toKabobCase(def.getName()).replace(".", "-");
    }

    private static String toArgName(ConfigurationDefinition<?> def) {
        return "--" + StringUtil.toKabobCase(def.getKey()).replace(".", "-");
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
