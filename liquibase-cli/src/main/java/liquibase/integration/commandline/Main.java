package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.LiquibaseConfiguration;

import java.util.SortedSet;

public class Main {
    public static void main(String[] args) {
        System.out.println("New CLI!");

        final SortedSet<ConfigurationDefinition> definitions = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getDefinitions();
        for (ConfigurationDefinition def : definitions) {
            System.out.println("See " + def.getProperty() + " = " + def.getCurrentValue() + " -- " + def.getDescription());
        }
    }
}
