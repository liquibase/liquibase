package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandFactory;
import liquibase.command.CommandScope;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.DatabaseException;
import liquibase.util.StringUtil;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

public class NewMain {
    public static void main(String[] args) {
        System.out.println("New CLI!");

        final SortedSet<ConfigurationDefinition> definitions = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getRegisteredDefinitions();
        for (ConfigurationDefinition def : definitions) {
            System.out.println("See " + def.getKey() + " = " + def.getCurrentValue() + " -- " + def.getDescription());
        }

        Map<String, String> passedArgs = new HashMap<>();
        passedArgs.put("url", "jdbc:mysql://127.0.0.1:33062/lbcat");
        passedArgs.put("username", "lbuser");
        passedArgs.put("password", "LiquibasePass1");

        passedArgs.put("output", "/tmp/out.txt");


        try {
            CommandScope commandScope = new CommandScope("history");

            for (CommandArgumentDefinition<Database> argument : commandScope.getCommand().getArguments(Database.class)) {
                String prefix = argument.getName().replaceFirst("[dD]atabase", "");

                Database database = createDatabase(passedArgs.get(prefixArg(prefix, "url")), passedArgs.get(prefixArg(prefix, "username")), passedArgs.get(prefixArg(prefix, "password")));

                commandScope.addArgumentValue(argument, database);
            }

            FileOutputStream outputStream = null;
            if (passedArgs.containsKey("output")) {
                outputStream = new FileOutputStream(passedArgs.get("output"));
                commandScope.setOutput(outputStream);
            }

            commandScope.execute();

            if (outputStream != null) {
                outputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Database createDatabase(String url, String username, String password) throws DatabaseException {
        return CommandLineUtils.createDatabaseObject(Scope.getCurrentScope().getResourceAccessor(), url, username, password,
                null, null, null, false, false, null, null, null, null, null, null,null);
    }

    private static String prefixArg(String prefix, String name) {
        if (prefix == null || prefix.equals("")) {
            return name;
        }
        return prefix+ StringUtil.upperCaseFirst(name);
    }
}
