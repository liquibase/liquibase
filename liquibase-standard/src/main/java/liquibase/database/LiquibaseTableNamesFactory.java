package liquibase.database;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.servicelocator.ServiceLocator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LiquibaseTableNamesFactory implements SingletonObject {

    private final List<LiquibaseTableNames> generators;

    private LiquibaseTableNamesFactory() {
        ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
        generators = serviceLocator.findInstances(LiquibaseTableNames.class);
    }

    public List<String> getLiquibaseTableNames(Database database) {
//        return generators.stream().flatMap(f -> f.getLiquibaseGeneratedTableNames(database).stream()).collect(Collectors.toList());
        return Arrays.asList("DATABASECHANGELOG", "DATABASECHANGELOGLOCK", "DATABASECHANGELOGHISTORY");
    }
}
