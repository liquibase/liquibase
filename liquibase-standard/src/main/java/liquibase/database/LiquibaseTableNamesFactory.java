package liquibase.database;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.exception.LiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LiquibaseTableNamesFactory implements SingletonObject {

    private final SortedSet<LiquibaseTableNames> generators;

    private LiquibaseTableNamesFactory() {
        ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
        generators = new TreeSet<>(Comparator.comparingInt(LiquibaseTableNames::getOrder));
        generators.addAll(serviceLocator.findInstances(LiquibaseTableNames.class));
    }

    public List<String> getLiquibaseTableNames(Database database) {
        return generators.stream().flatMap(f -> f.getLiquibaseGeneratedTableNames(database).stream()).collect(Collectors.toList());
    }

    // ADR-0005 (INT-2205 phase 2): widened to LiquibaseException in lockstep with the
    // LiquibaseTableNames.destroy SPI it fans out to; Database.dropDatabaseObjects already
    // declares throws LiquibaseException, so the widening terminates one level up.
    public void destroy(Database abstractJdbcDatabase) throws LiquibaseException {
        for (LiquibaseTableNames generator : generators) {
            generator.destroy(abstractJdbcDatabase);
            abstractJdbcDatabase.commit();
        }
    }
}
