package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.util.StringUtil;
import picocli.CommandLine;

import java.util.SortedSet;


public class LiquibaseCommandLine {

    public static void main(String[] args) {
//        commandLine.addSubcommand(CommandLine.HelpCommand.class);
        final CommandLine.Model.CommandSpec rootCommandSpec = CommandLine.Model.CommandSpec.create();
        rootCommandSpec.name("liquibase");
        rootCommandSpec.mixinStandardHelpOptions(true);

        final CommandLine commandLine = new CommandLine(rootCommandSpec);

        final SortedSet<ConfigurationDefinition> globalConfigurations = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getRegisteredDefinitions();
        for (ConfigurationDefinition def : globalConfigurations) {
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

        final CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);
        for (CommandDefinition commandDefinition : commandFactory.getCommands()) {
            final CommandRunner commandRunner = new CommandRunner();
            final CommandLine.Model.CommandSpec subCommandSpec = CommandLine.Model.CommandSpec.wrapWithoutInspection(commandRunner);
            commandRunner.setSpec(subCommandSpec);

            subCommandSpec.mixinStandardHelpOptions(true);

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

            rootCommandSpec.addSubcommand(commandDefinition.getName()[0], subCommandSpec);
        }


        commandLine.execute(args);
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

    }

    private static String toArgName(CommandArgumentDefinition<?> def) {
        return "--" + StringUtil.toKabobCase(def.getName()).replace(".", "-");
    }

    private static String toArgName(ConfigurationDefinition<?> def) {
        return "--" + StringUtil.toKabobCase(def.getKey()).replace(".", "-");
    }

    private static Database createDatabase(String url, String username, String password) throws DatabaseException {
        return CommandLineUtils.createDatabaseObject(Scope.getCurrentScope().getResourceAccessor(), url, username, password,
                null, null, null, false, false, null, null, null, null, null, null, null);
    }

    private static String prefixArg(String prefix, String name) {
        if (prefix == null || prefix.equals("")) {
            return name;
        }
        return prefix + StringUtil.upperCaseFirst(name);
    }
}
