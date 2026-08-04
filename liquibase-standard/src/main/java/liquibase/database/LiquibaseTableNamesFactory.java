package liquibase.database;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.exception.LiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LiquibaseTableNamesFactory implements SingletonObject {

    private final List<LiquibaseTableNames> generators;

    private LiquibaseTableNamesFactory() {
        ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
        // Sorted by getOrder() ascending, tie-broken by class name for deterministic iteration. Not a
        // comparator-keyed TreeSet: that would silently drop generators that share a getOrder() value
        // (same defect as ConfiguredValueModifierFactory — see INT-2215).
        List<LiquibaseTableNames> found = new ArrayList<>(serviceLocator.findInstances(LiquibaseTableNames.class));
        found.sort(Comparator.comparingInt(LiquibaseTableNames::getOrder)
                .thenComparing(generator -> generator.getClass().getName()));
        generators = Collections.unmodifiableList(found);
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
