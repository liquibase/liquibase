package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.util.StringUtil;
import picocli.CommandLine;

import java.util.SortedSet;


public class LiquibaseCommandLine {

    private final CommandLine commandLine;

    public static void main(String[] args) {
        final LiquibaseCommandLine cli = new LiquibaseCommandLine();
        cli.execute(args);
    }

    public LiquibaseCommandLine() {

        final CommandLine.Model.CommandSpec rootCommandSpec = CommandLine.Model.CommandSpec.create();
        rootCommandSpec.name("liquibase");
        configureHelp(rootCommandSpec);

        commandLine = new CommandLine(rootCommandSpec);

        addGlobalArguments();

        for (CommandDefinition commandDefinition : getCommands()) {
            addSubcommand(commandDefinition);
        }
    }

    public void execute(String[] args) {
        commandLine.execute(args);
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


    private void addSubcommand(CommandDefinition commandDefinition) {
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

    private void addGlobalArguments() {
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
