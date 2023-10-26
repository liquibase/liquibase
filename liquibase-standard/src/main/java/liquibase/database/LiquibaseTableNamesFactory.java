package liquibase.database;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.servicelocator.ServiceLocator;

import java.util.List;
import java.util.stream.Collectors;

public class LiquibaseTableNamesFactory implements SingletonObject {

    private final List<LiquibaseTableNames> generators;
    private List<String> cachedTableNames;


    private LiquibaseTableNamesFactory() {
        ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
        generators = serviceLocator.findInstances(LiquibaseTableNames.class);
    }

    public List<String> getLiquibaseTableNames(Database database) {
        if (cachedTableNames == null) {
            cachedTableNames = generators.stream().flatMap(f -> f.getLiquibaseGeneratedTableNames(database).stream()).collect(Collectors.toList());
        }
        return cachedTableNames;
    }
}
