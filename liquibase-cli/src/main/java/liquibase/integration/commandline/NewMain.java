package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.LiquibaseConfiguration;

import java.util.SortedSet;

public class NewMain {
    public static void main(String[] args) {
        System.out.println("New CLI!");

        final SortedSet<ConfigurationDefinition> definitions = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getRegisteredDefinitions();
        for (ConfigurationDefinition def : definitions) {
            System.out.println("See " + def.getKey() + " = " + def.getCurrentValue() + " -- " + def.getDescription());
        }
    }
}
