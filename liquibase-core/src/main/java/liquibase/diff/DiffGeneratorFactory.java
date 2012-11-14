package liquibase.diff;

import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;

import java.util.*;

public class DiffGeneratorFactory {

    private static DiffGeneratorFactory instance;
    private List<DiffGenerator> implementedGenerators = new ArrayList<DiffGenerator>();

    protected DiffGeneratorFactory() {
        try {
            Class[] classes = ServiceLocator.getInstance().findClasses(DiffGenerator.class);

            //noinspection unchecked
            for (Class<? extends DiffGenerator> clazz : classes) {
                register(clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DiffGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new DiffGeneratorFactory();
        }
        return instance;
    }

    public void register(DiffGenerator generator) {
        implementedGenerators.add(0, generator);
    }


    public DiffGenerator getGenerator(Database referenceDatabase, Database comparisonDatabase) {
        SortedSet<DiffGenerator> foundGenerators = new TreeSet<DiffGenerator>(new Comparator<DiffGenerator>() {
            public int compare(DiffGenerator o1, DiffGenerator o2) {
                return -1 * new Integer(o1.getPriority()).compareTo(o2.getPriority());
            }
        });

        for (DiffGenerator diffGenerator : implementedGenerators) {
            if (diffGenerator.supports(referenceDatabase, comparisonDatabase)) {
                foundGenerators.add(diffGenerator);
            }
        }

        if (foundGenerators.size() == 0) {
            throw new UnexpectedLiquibaseException("Cannot find DiffGenerator for "+referenceDatabase.getShortName()+", "+comparisonDatabase.getShortName());
        }

        try {
            return foundGenerators.iterator().next().getClass().newInstance();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public DiffResult compare(Database referenceDatabase, Database comparisonDatabase, CompareControl compareControl) throws LiquibaseException {
        DatabaseSnapshot referenceSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(referenceDatabase.getDefaultSchema(), referenceDatabase, new SnapshotControl());
        DatabaseSnapshot comparisonSnapshot = null;
        if (comparisonDatabase == null) {
            comparisonSnapshot = new DatabaseSnapshot(referenceDatabase);
        } else {
            comparisonSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(comparisonDatabase.getDefaultSchema(), comparisonDatabase, new SnapshotControl());
        }

        return getGenerator(referenceDatabase, comparisonDatabase).compare(referenceSnapshot, comparisonSnapshot, compareControl);
    }


    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, CompareControl compareControl) throws DatabaseException {
        return getGenerator(referenceSnapshot.getDatabase(), comparisonSnapshot.getDatabase()).compare(referenceSnapshot, comparisonSnapshot, compareControl);

    }

}
