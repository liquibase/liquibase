package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandFactory;
import liquibase.command.CommandScope;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.CommandExecutionException;

import java.util.SortedSet;

public class NewMain {
    public static void main(String[] args) {
        System.out.println("New CLI!");

        final SortedSet<ConfigurationDefinition> definitions = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getRegisteredDefinitions();
        for (ConfigurationDefinition def : definitions) {
            System.out.println("See " + def.getKey() + " = " + def.getCurrentValue() + " -- " + def.getDescription());
        }

        CommandScope commandScope = new CommandScope("history");
        commandScope.addArgument("url", "jdbc:mysql://127.0.0.1:33062/lbcat");
        commandScope.addArgument("username", "lbuser");
        commandScope.addArgument("password", "LiquibasePass1");

        try {
            Scope.getCurrentScope().getSingleton(CommandFactory.class).execute(commandScope);
        } catch (CommandExecutionException e) {
            e.printStackTrace();
        }

    }
}
