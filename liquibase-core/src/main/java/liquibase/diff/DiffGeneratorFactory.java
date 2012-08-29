package liquibase.diff;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.DatabaseSnapshot;

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


    public DiffResult compare(Database referenceDatabase, Database comparisonDatabase, DiffControl diffControl) throws DatabaseException {
        return getGenerator(referenceDatabase, comparisonDatabase).compare(referenceDatabase, comparisonDatabase, diffControl);
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

        DiffGenerator returnDiffGenerator;
        try {
            return foundGenerators.iterator().next().getClass().newInstance();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffControl diffControl) throws DatabaseException {
        return getGenerator(referenceSnapshot.getDatabase(), comparisonSnapshot.getDatabase()).compare(referenceSnapshot, comparisonSnapshot, diffControl);

    }

}
