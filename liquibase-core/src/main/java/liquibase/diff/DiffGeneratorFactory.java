package liquibase.diff;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.*;

import java.util.*;

public class DiffGeneratorFactory {

    private static DiffGeneratorFactory instance;
    private List<DiffGenerator> implementedGenerators = new ArrayList<>();

    protected DiffGeneratorFactory() {
        try {
            for (DiffGenerator diffGenerator : Scope.getCurrentScope().getServiceLocator().findInstances(DiffGenerator.class)) {
                register(diffGenerator);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized DiffGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new DiffGeneratorFactory();
        }
        return instance;
    }

    public void register(DiffGenerator generator) {
        implementedGenerators.add(0, generator);
    }


    public DiffGenerator getGenerator(Database referenceDatabase, Database comparisonDatabase) {
        SortedSet<DiffGenerator> foundGenerators = new TreeSet<>(new Comparator<DiffGenerator>() {
            @Override
            public int compare(DiffGenerator o1, DiffGenerator o2) {
                return -1 * Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
            }
        });

        for (DiffGenerator diffGenerator : implementedGenerators) {
            if (diffGenerator.supports(referenceDatabase, comparisonDatabase)) {
                foundGenerators.add(diffGenerator);
            }
        }

        if (foundGenerators.isEmpty()) {
            throw new UnexpectedLiquibaseException("Cannot find DiffGenerator for " + referenceDatabase.getShortName() + ", " + comparisonDatabase.getShortName());
        }

        try {
            return foundGenerators.iterator().next().getClass().newInstance();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public DiffResult compare(Database referenceDatabase, Database comparisonDatabase, CompareControl compareControl) throws LiquibaseException {
        return compare(referenceDatabase, comparisonDatabase, new SnapshotControl(referenceDatabase), new SnapshotControl(comparisonDatabase), compareControl);
    }

    public DiffResult compare(Database referenceDatabase, Database comparisonDatabase, SnapshotControl referenceSnapshotControl, SnapshotControl comparisonSnapshotControl, CompareControl compareControl) throws LiquibaseException {
        DatabaseSnapshot referenceSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(referenceDatabase.getDefaultSchema(), referenceDatabase, referenceSnapshotControl);
        DatabaseSnapshot comparisonSnapshot = null;
        if (comparisonDatabase == null) {
            comparisonSnapshot = new EmptyDatabaseSnapshot(referenceDatabase);
        } else {
            comparisonSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(comparisonDatabase.getDefaultSchema(), comparisonDatabase, comparisonSnapshotControl);
        }

        return getGenerator(referenceDatabase, comparisonDatabase).compare(referenceSnapshot, comparisonSnapshot, compareControl);
    }


    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, CompareControl compareControl) throws DatabaseException {
        Database referenceDatabase = referenceSnapshot.getDatabase();
        Database comparisonDatabase;
        if (comparisonSnapshot == null) {
            comparisonDatabase = referenceSnapshot.getDatabase();
            try {
                comparisonSnapshot = new EmptyDatabaseSnapshot(referenceDatabase, referenceSnapshot.getSnapshotControl());
            } catch (InvalidExampleException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        } else {
            comparisonDatabase = comparisonSnapshot.getDatabase();
        }
        return getGenerator(referenceDatabase, comparisonDatabase).compare(referenceSnapshot, comparisonSnapshot, compareControl);

    }

}
