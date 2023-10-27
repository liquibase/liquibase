package liquibase.database;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.List;
import java.util.stream.Collectors;

public class LiquibaseTableNamesFactory implements SingletonObject {

    private final List<LiquibaseTableNames> generators;

    private LiquibaseTableNamesFactory() {
        ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
        generators = serviceLocator.findInstances(LiquibaseTableNames.class);
    }

    public List<String> getLiquibaseTableNames(Database database) {
        return generators.stream().flatMap(f -> f.getLiquibaseGeneratedTableNames(database).stream()).collect(Collectors.toList());
    }

    public void destroy(Database abstractJdbcDatabase) throws DatabaseException {
        for (LiquibaseTableNames generator : generators) {
            generator.destroy(abstractJdbcDatabase);
        }
    }
}
