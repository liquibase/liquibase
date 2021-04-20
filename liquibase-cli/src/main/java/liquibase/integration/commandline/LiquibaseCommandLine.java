package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.core.DefaultsFileValueProvider;
import liquibase.license.LicenseServiceFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtil;
import liquibase.util.SystemUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.*;


public class LiquibaseCommandLine {

    private final CommandLine commandLine;

    public static void main(String[] args) {
        final LiquibaseCommandLine cli = new LiquibaseCommandLine();

        try {
            cli.execute(args);
        } catch (Exception e) {
            e.printStackTrace();
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
        final CommandLine.ParseResult parseResult = commandLine.parseArgs(args);

        String classpath = getBootstrapSetting(parseResult, CommandLineConfiguration.CLASSPATH);
        if (StringUtil.trimToNull(classpath) == null) {
            classpath = ".";
        }
        final String bootstrapClasspath = classpath;

        final CompositeResourceAccessor createResourceAccessor = createResourceAccessor(bootstrapClasspath);

        Scope.child(Scope.Attr.resourceAccessor, createResourceAccessor, () -> {
            String defaultsFile = getBootstrapSetting(parseResult, CommandLineConfiguration.DEFAULTS_FILE);

            if (defaultsFile != null) {
                Scope.getCurrentScope().getLog(getClass()).fine("Using defaults file " + defaultsFile);
                Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).registerProvider(new DefaultsFileValueProvider(defaultsFile));
            }

            Map<String, Object> finalScopeValues = new HashMap<>();
            //check if classpath changed from defaultsFile
            String newClasspath = CommandLineConfiguration.CLASSPATH.getCurrentValue();
            if (newClasspath != null && !newClasspath.equals(bootstrapClasspath)) {
                finalScopeValues.put(Scope.Attr.resourceAccessor.name(), createResourceAccessor(newClasspath));
            }

            Scope.child(finalScopeValues, () -> {
                getRootCommand().getCommandSpec().version(
                        CommandLineUtils.getBanner(),
                        String.format("Running Java under %s (Version %s)",
                                System.getProperties().getProperty("java.home"),
                                System.getProperty("java.version")
                        ),
                        "",
                        "Liquibase Version: " + LiquibaseUtil.getBuildVersion(),
                        Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService().getLicenseInfo()
                );

                commandLine.execute(args);
            });
        });
    }

    private CommandLine getRootCommand() {
        CommandLine commandLine = this.commandLine;
        while (commandLine.getParent() != null) {
            commandLine = commandLine.getParent();
        }
        return commandLine;
    }

    private <T> T getBootstrapSetting(CommandLine.ParseResult parseResult, ConfigurationDefinition<T> config) {
        final CommandLine.Model.OptionSpec matchedOption = parseResult.matchedOption(StringUtil.toKabobCase(config.getKey().replace(".", "-")));
        if (matchedOption != null) {
            return matchedOption.getValue();
        }

        return config.getCurrentValue();
    }


    private CompositeResourceAccessor createResourceAccessor(String classpath) {
        List<File> classpathFiles = splitClasspath(classpath);
        return new CompositeResourceAccessor(new FileSystemResourceAccessor(classpathFiles.toArray(new File[0])), new ClassLoaderResourceAccessor());
    }

    private List<File> splitClasspath(String bootstrapClasspath) {
        List<File> classpathFiles = new ArrayList<>();
        if (StringUtil.trimToNull(bootstrapClasspath) != null) {
            String[] splitClasspath;
            if (SystemUtil.isWindows()) {
                splitClasspath = bootstrapClasspath.split(";");
            } else {
                splitClasspath = bootstrapClasspath.split(":");
            }
            for (String entry : splitClasspath) {
                classpathFiles.add(new File(".", entry));
            }
        }
        return classpathFiles;
    }

//
//        Map<String, String> passedArgs = new HashMap<>();
//        passedArgs.put("url", "jdbc:mysql://127.0.0.1:33062/lbcat");
//        passedArgs.put("username", "lbuser");
//        passedArgs.put("password", "LiquibasePass1");
//
//        passedArgs.put("output", "/tmp/out.txt");
//
//
//        try {
//            CommandScope commandScope = new CommandScope("history");
//
//            for (CommandArgumentDefinition<Database> argument : commandScope.getCommand().getArguments(Database.class)) {
//                String prefix = argument.getName().replaceFirst("[dD]atabase", "");
//
//                Database database = createDatabase(passedArgs.get(prefixArg(prefix, "url")), passedArgs.get(prefixArg(prefix, "username")), passedArgs.get(prefixArg(prefix, "password")));
//
//                commandScope.addArgumentValue(argument, database);
//            }
//
//            FileOutputStream outputStream = null;
//            if (passedArgs.containsKey("output")) {
//                outputStream = new FileOutputStream(passedArgs.get("output"));
//                commandScope.setOutput(outputStream);
//            }
//
//            commandScope.execute();
//
//            if (outputStream != null) {
//                outputStream.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


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
}
