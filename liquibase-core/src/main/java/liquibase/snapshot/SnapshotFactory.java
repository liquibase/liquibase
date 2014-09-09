package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.ExecutionEnvironment;
import liquibase.database.Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnsupportedException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.*;

/**
 * This class is the primary facade for creating database snapshots.
 * There are two major operations: "has(*)" and "createSnapshot(*)". This class is a singleton and accessed via {@link #getInstance()}.
 */
public class SnapshotFactory {

    private static SnapshotFactory instance;

    private List<SnapshotLookupLogic> lookupLogic = new ArrayList<SnapshotLookupLogic>();
    private List<SnapshotRelateLogic> relateLogic = new ArrayList<SnapshotRelateLogic>();
    private List<SnapshotDetailsLogic> detailsLogic = new ArrayList<SnapshotDetailsLogic>();

    protected SnapshotFactory() {
        try {
            for (Class clazz : ServiceLocator.getInstance().findClasses(SnapshotLookupLogic.class)) {
                register((SnapshotLookupLogic) clazz.getConstructor().newInstance());
            }

            for (Class clazz : ServiceLocator.getInstance().findClasses(SnapshotRelateLogic.class)) {
                register((SnapshotRelateLogic) clazz.getConstructor().newInstance());
            }

            for (Class clazz : ServiceLocator.getInstance().findClasses(SnapshotDetailsLogic.class)) {
                register((SnapshotDetailsLogic) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    /**
     * Return singleton SnapshotFactory.
     */
    public static SnapshotFactory getInstance() {
        if (instance == null) {
            instance = new SnapshotFactory();
        }
        return instance;
    }

    /**
     * Resets the singleton. Mainly used for testing.
     */
    public static void reset() {
        instance = new SnapshotFactory();
    }


    /**
     * Registers a new lookup logic instance. Normally not called directly because they should be auto-detected.
     */
    public void register(SnapshotLookupLogic logic) {
        lookupLogic.add(logic);
    }

    /**
     * Removes a SnapshotLookupLogic implementation from the singleton.
     */
    public void unregister(SnapshotLookupLogic logic) {
        lookupLogic.remove(logic);
    }


    /**
     * Registers a new relate logic instance. Normally not called directly because they should be auto-detected.
     */
    public void register(SnapshotRelateLogic logic) {
        relateLogic.add(logic);
    }

    /**
     * Removes a SnapshotRelateLogic implementation from the singleton.
     */
    public void unregister(SnapshotRelateLogic logic) {
        relateLogic.remove(logic);
    }


    /**
     * Registers a new details logic instance. Normally not called directly because they should be auto-detected.
     */
    public void register(SnapshotDetailsLogic logic) {
        detailsLogic.add(logic);
    }

    /**
     * Removes a SnapshotDetailsLogic implementation from the singleton.
     */
    public void unregister(SnapshotDetailsLogic logic) {
        detailsLogic.remove(logic);
    }

    /**
     * Returns the SnapshotLookupLogic for the given databaseObjectType for the given ExecutionEnvironment.
     * If multiple SnapshotLookupLogic are valid, the one with the highest priority is returned.
     * If no instances exist for the given object type/environment combination, null is returned.
     */
    protected SnapshotLookupLogic getLookupLogic(Class<? extends DatabaseObject> databaseObjectType, ExecutionEnvironment environment) {
        SortedSet<SnapshotLookupLogic> valid = new TreeSet<SnapshotLookupLogic>(new SnapshotLookupLogicComparator(databaseObjectType, environment));

        for (SnapshotLookupLogic logic : lookupLogic) {
            if (logic.getPriority(databaseObjectType, environment) > 0) {
                valid.add(logic);
            }
        }
        if (valid.size() == 0) {
            return null;
        }
        return valid.iterator().next();
    }

    /**
     * Returns the SnapshotRelateLogic instances that support the given environment
     */
    protected List<SnapshotRelateLogic> getRelateLogic(ExecutionEnvironment environment) {
        List<SnapshotRelateLogic> returnList = new ArrayList<SnapshotRelateLogic>();
        for (SnapshotRelateLogic logic : relateLogic) {
            if (logic.supports(environment)) {
                returnList.add(logic);
            }
        }
        return returnList;
    }

    /**
     * Returns the SnapshotDetailsLogic instances that support the given environment
     */
    protected List<SnapshotDetailsLogic> getDetailsLogic(ExecutionEnvironment environment) {
        List<SnapshotDetailsLogic> returnList = new ArrayList<SnapshotDetailsLogic>();
        for (SnapshotDetailsLogic logic : detailsLogic) {
            if (logic.supports(environment)) {
                returnList.add(logic);
            }
        }
        return returnList;
    }

    /**
     * Returns DatabaseObject types that can be snapshotted. By default it returns types known by {@link liquibase.structure.core.DatabaseObjectFactory}
     */
    protected Set<Class<? extends DatabaseObject>> getDatabaseObjectTypes() {
        return Collections.unmodifiableSet(DatabaseObjectFactory.getInstance().getAllTypes());
    }


    /**
     * Convenience method to determine if the given database has the given object in it. Lookup may be optimized knowing it is only being checked for existence of a single object.
     */
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
        NewDatabaseSnapshot snapshot = createSnapshot(catalogAndSchema, database, new SnapshotControl(database, example.getClass()));
        for (DatabaseObject obj : snapshot.get(example.getClass())) {
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(example, obj, database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a database snapshot of all objects in the given {@link liquibase.CatalogAndSchema}.
     */
    public NewDatabaseSnapshot createSnapshot(CatalogAndSchema example, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        return createSnapshot(new CatalogAndSchema[] {example}, database, snapshotControl);
    }

    /**
     * Returns a database snapshot of all objects in all the given {@link liquibase.CatalogAndSchema}s.
     */
    public NewDatabaseSnapshot createSnapshot(CatalogAndSchema[] examples, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        if (database == null) {
            return null;
        }
        Schema[] schemas = new Schema[examples.length];
        for (int i = 0; i< schemas.length; i++) {
            examples[i] = examples[i].customize(database);
            schemas[i] = new Schema(examples[i].getCatalogName(), examples[i].getSchemaName());
        }

        ExecutionEnvironment env = new ExecutionEnvironment(database);
        try {
            return createSnapshot(schemas, snapshotControl, env);
        } catch (UnsupportedException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Convenience method for {@link #createSnapshot(liquibase.structure.DatabaseObject, liquibase.database.Database, SnapshotControl)} with a default SnapshotControl.
     */
    public <T extends DatabaseObject> T createSnapshot(T example, Database database) throws DatabaseException, InvalidExampleException {
        return createSnapshot(example, database, new SnapshotControl(database));
    }

    /**
     * Returns a snapshot of a given object.
     * If multiple objects match the passed example, an InvalidExampleException is thrown.
     * If no objects match the passed example, null is returned.
     * Related objects are included based on the settings in the passed {@link liquibase.snapshot.SnapshotControl} object. For example, a table's columns attribute will be populated only if the Column type is set to snapshot.
     */
    public <T extends DatabaseObject> T createSnapshot(T example, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        return null;
    }

    /**
     * Creates a DatabaseSnapshot containing snapshotted instances of the passed examples as well as any related dependent objects.
     * This is the primary createSnapshot(*) method which all others delegate to.
     * The logic in this method is:
     *
     * <ol>
     * <li>Call {@link SnapshotLookupLogic#lookup(Class, liquibase.structure.DatabaseObject, liquibase.ExecutionEnvironment)} for each DatabaseObject type and each found object to the DatabaseSnapshot.</li>
     * <li>For all registered instances of SnapshotRelateLogic, {@link SnapshotRelateLogic#relate(NewDatabaseSnapshot)} is called on the snapshot.</li>
     * <li>For all registered instances of SnapshotDetailsLogic, {@link SnapshotDetailsLogic#addDetails(NewDatabaseSnapshot)} is called on the snapshot.</li>
     * </ol>
     */
    public NewDatabaseSnapshot createSnapshot(DatabaseObject[] examples, SnapshotControl snapshotControl, ExecutionEnvironment env) throws DatabaseException, UnsupportedException {

        NewDatabaseSnapshot snapshot = new NewDatabaseSnapshot(snapshotControl, env);

        for (Class<? extends DatabaseObject> objectType : getDatabaseObjectTypes()) {
            if (!snapshotControl.getTypesToInclude().contains(objectType)) {
                continue;
            }
            SnapshotLookupLogic logic = getLookupLogic(objectType, env);
            if (logic != null) {
                for (DatabaseObject example : examples) {
                    for (DatabaseObject obj : logic.lookup(objectType, example, env)) {
                        snapshot.add(obj);
                    }
                }
            }
        }

        if (snapshotControl.shouldRelateObjects()) {
            for (SnapshotRelateLogic logic : getRelateLogic(env)) {
                logic.relate(snapshot);
            }
        }

        if (snapshotControl.shouldAddDetails()) {
            for (SnapshotDetailsLogic logic : getDetailsLogic(env)) {
                logic.addDetails(snapshot);
            }
        }

        return snapshot;

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
//        SortedSet<SnapshotGenerator> generators = getGenerators(type, database);
//        if (generators != null && generators.size() > 0) {
//            SnapshotGenerator generator = generators.iterator().next();
//            if (generator.addsTo() != null) {
//                for (Class<? extends DatabaseObject> newType : generator.addsTo()) {
//                    returnSet.add(newType);
//                    getContainerTypes(newType, database, returnSet);
//                }
//            }
//        }

    }


    class SnapshotLookupLogicComparator implements Comparator<SnapshotLookupLogic> {

        private Class<? extends DatabaseObject> objectType;
        private ExecutionEnvironment environment;

        public SnapshotLookupLogicComparator(Class<? extends DatabaseObject> objectType, ExecutionEnvironment environment) {
            this.objectType = objectType;
            this.environment = environment;
        }

        @Override
        public int compare(SnapshotLookupLogic o1, SnapshotLookupLogic o2) {
            int result = -1 * new Integer(o1.getPriority(objectType, environment)).compareTo(o2.getPriority(objectType, environment));
            if (result == 0) {
                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }
            return result;
        }
    }
}
