package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

public class SnapshotGeneratorFactory {

    private static SnapshotGeneratorFactory instance;

    private List<SnapshotGenerator> generators = new ArrayList<SnapshotGenerator>();

    private SnapshotGeneratorFactory() {
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
        if (createSnapshot(example, database) != null) {
            return true;
        }
        CatalogAndSchema catalogAndSchema;
        if (example.getSchema() == null) {
            catalogAndSchema = database.getDefaultSchema();
        } else {
            catalogAndSchema = example.getSchema().toCatalogAndSchema();
        }
        DatabaseSnapshot snapshot = createSnapshot(catalogAndSchema, database, new SnapshotControl(example.getClass()));
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
        Schema[] schemas = new Schema[examples.length];
        for (int i = 0; i< schemas.length; i++) {
            schemas[i] = new Schema(examples[i].getCatalogName(), examples[i].getSchemaName());
        }
        return createSnapshot(schemas, database, snapshotControl);
    }

    public DatabaseSnapshot createSnapshot(DatabaseObject[] examples, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        DatabaseSnapshot snapshot = new DatabaseSnapshot(snapshotControl, database);

        for (DatabaseObject example : examples) {
            snapshot.include(example);
        }

        return snapshot;
    }

    public <T extends DatabaseObject> T createSnapshot(T example, Database database) throws DatabaseException, InvalidExampleException {
        return createSnapshot(example, database, new SnapshotControl());
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
}
