package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class SnapshotGeneratorFactory {

    private static SnapshotGeneratorFactory instance;

    private List<SnapshotGenerator> generators = new ArrayList<SnapshotGenerator>();

    protected SnapshotGeneratorFactory() {
        Class[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(SnapshotGenerator.class);

            for (Class clazz : classes) {
                register((SnapshotGenerator) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    /**
     * Return singleton SnapshotGeneratorFactory
     */
    public static SnapshotGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new SnapshotGeneratorFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new SnapshotGeneratorFactory();
    }


    public void register(SnapshotGenerator generator) {
        generators.add(generator);
    }

    public void unregister(SnapshotGenerator generator) {
        generators.remove(generator);
    }

    public void unregister(Class generatorClass) {
        SnapshotGenerator toRemove = null;
        for (SnapshotGenerator existingGenerator : generators) {
            if (existingGenerator.getClass().equals(generatorClass)) {
                toRemove = existingGenerator;
            }
        }

        unregister(toRemove);
    }

    protected SortedSet<SnapshotGenerator> getGenerators(Class<? extends DatabaseObject> generatorClass, Database database) {
        SortedSet<SnapshotGenerator> validGenerators = new TreeSet<SnapshotGenerator>(new SnapshotGeneratorComparator(generatorClass, database));

        for (SnapshotGenerator generator : generators) {
            if (generator.getPriority(generatorClass, database) > 0) {
                validGenerators.add(generator);
            }
        }
        return validGenerators;
    }


    public boolean has(DatabaseObject example, Database database) throws DatabaseException, InvalidExampleException {
        List<Class<? extends DatabaseObject>> types = new ArrayList<Class<? extends DatabaseObject>>(getContainerTypes(example.getClass(), database));
        types.add(example.getClass());

        if (createSnapshot(example, database, new SnapshotControl(database,  types.toArray(new Class[types.size()]))) != null) {
            return true;
        }
        CatalogAndSchema catalogAndSchema;
        if (example.getSchema() == null) {
            catalogAndSchema = database.getDefaultSchema();
        } else {
            catalogAndSchema = example.getSchema().toCatalogAndSchema();
        }
        DatabaseSnapshot snapshot = createSnapshot(catalogAndSchema, database, new SnapshotControl(database, example.getClass()));
        for (DatabaseObject obj : snapshot.get(example.getClass())) {
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(example, obj, database)) {
                return true;
            }
        }
        return false;
    }

    public DatabaseSnapshot createSnapshot(CatalogAndSchema example, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        return createSnapshot(new CatalogAndSchema[] {example}, database, snapshotControl);
    }

    public DatabaseSnapshot createSnapshot(CatalogAndSchema[] examples, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        if (database == null) {
            return null;
        }
        Schema[] schemas = new Schema[examples.length];
        for (int i = 0; i< schemas.length; i++) {
            examples[i] = examples[i].customize(database);
            schemas[i] = new Schema(examples[i].getCatalogName(), examples[i].getSchemaName());
        }
        return createSnapshot(schemas, database, snapshotControl);
    }

    public DatabaseSnapshot createSnapshot(DatabaseObject[] examples, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        if (database.getConnection() instanceof OfflineConnection) {
            throw new DatabaseException("Cannot snapshot offline database");
        }
        return new JdbcDatabaseSnapshot(examples, database, snapshotControl);
    }

    public <T extends DatabaseObject> T createSnapshot(T example, Database database) throws DatabaseException, InvalidExampleException {
        return createSnapshot(example, database, new SnapshotControl(database));
    }

    public <T extends DatabaseObject> T createSnapshot(T example, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        DatabaseSnapshot snapshot = createSnapshot(new DatabaseObject[]{example}, database, snapshotControl);
        return snapshot.get(example);
    }

    public Table getDatabaseChangeLogTable(SnapshotControl snapshotControl, Database database) throws DatabaseException {
        try {
            Table liquibaseTable = (Table) new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()));
            return createSnapshot(liquibaseTable, database, snapshotControl);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        try {
            Table example = (Table) new Table().setName(database.getDatabaseChangeLogLockTableName()).setSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()));
            return createSnapshot(example, database);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean hasDatabaseChangeLogTable(Database database) throws DatabaseException {
        try {
            return has(new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean hasDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        try {
            return has(new Table().setName(database.getDatabaseChangeLogLockTableName()).setSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public static void resetAll() {
        instance = null;
    }

    public Set<Class<? extends DatabaseObject>> getContainerTypes(Class<? extends DatabaseObject> type, Database database) {
        Set<Class<? extends DatabaseObject>>  returnSet = new HashSet<Class<? extends DatabaseObject>>();

        getContainerTypes(type, database, returnSet);

        return returnSet;
    }

    private void getContainerTypes(Class<? extends DatabaseObject> type, Database database, Set<Class<? extends DatabaseObject>>  returnSet) {
        if (!returnSet.add(type)) {
            return;
        }
        SortedSet<SnapshotGenerator> generators = getGenerators(type, database);
        if (generators != null && generators.size() > 0) {
            SnapshotGenerator generator = generators.iterator().next();
            if (generator.addsTo() != null) {
                for (Class<? extends DatabaseObject> newType : generator.addsTo()) {
                    returnSet.add(newType);
                    getContainerTypes(newType, database, returnSet);
                }
            }
        }

    }
}
