package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.SupportsMethodValidationLevelsEnum;
import liquibase.database.*;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MariaDBDatabase;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.LiquibaseUtil;

import java.util.*;

import static liquibase.snapshot.SnapshotGenerator.PRIORITY_NONE;

public class SnapshotGeneratorFactory {

    private static SnapshotGeneratorFactory instance;

    private final List<SnapshotGenerator> generators = new ArrayList<>();
    protected static final String SUPPORTS_METHOD_REQUIRED_MESSAGE = "%s class does not properly implement the 'getPriority(Class<? extends DatabaseObject>, Database)' method and may incorrectly override other snapshot generators causing unexpected behavior. Please report this to the Liquibase developers or if you are developing this change please fix it ;)";

    protected SnapshotGeneratorFactory() {
        try {
            for (SnapshotGenerator generator : Scope.getCurrentScope().getServiceLocator().findInstances(SnapshotGenerator.class)) {
                verifyPriorityMethodImplementedCorrectly(generator);
                register(generator);
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    /**
     * Ensure that the getPriority method returns PRIORITY_NONE when an unexpected database is supplied to the method.
     * This ensures that no snapshot generator accidentally returns PRIORITY_SPECIALIZED for all databases, instead of
     * only for the database that it is supposed to work with.
     * @param generator
     */
    private void verifyPriorityMethodImplementedCorrectly(SnapshotGenerator generator) {
        if (GlobalConfiguration.SUPPORTS_METHOD_VALIDATION_LEVEL.getCurrentValue().equals(SupportsMethodValidationLevelsEnum.OFF)) {
            return;
        }

        try {
            int priority = generator.getPriority(null, new MockDatabase());
            if (priority != PRIORITY_NONE) {
                if (LiquibaseUtil.isDevVersion()) {
                    throw new UnexpectedLiquibaseException(String.format(SUPPORTS_METHOD_REQUIRED_MESSAGE, generator.getClass().getName()));
                }
                switch (GlobalConfiguration.SUPPORTS_METHOD_VALIDATION_LEVEL.getCurrentValue()) {
                    case WARN:
                        Scope.getCurrentScope().getLog(getClass()).warning(String.format(SUPPORTS_METHOD_REQUIRED_MESSAGE, generator.getClass().getName()));
                        break;
                    case FAIL:
                        throw new UnexpectedLiquibaseException(String.format(SUPPORTS_METHOD_REQUIRED_MESSAGE, generator.getClass().getName()));
                    default:
                        break;
                }
            }
        } catch (UnexpectedLiquibaseException ue) {
            throw ue;
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Failed to check validity of getPriority method in " + generator.getClass().getSimpleName() + " snapshot generator.", e);
        }
    }

    /**
     * Return singleton SnapshotGeneratorFactory
     */
    public static synchronized SnapshotGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new SnapshotGeneratorFactory();
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = new SnapshotGeneratorFactory();
    }

    public static synchronized void resetAll() {
        instance = null;
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
        SortedSet<SnapshotGenerator> validGenerators = new TreeSet<>(new SnapshotGeneratorComparator(generatorClass, database));

        /*
         * Query all SnapshotGenerators if they consider themselves applicable for the generatorClass (e.g. a Table)
         * for a specific Database (e.g. MSSQL, Oracle, Postgres...)
         */
        for (SnapshotGenerator generator : generators) {
            if (generator.getPriority(generatorClass, database) > 0) {
                validGenerators.add(generator);
            }
        }
        return validGenerators;
    }

    /**
     * Initialize the types to use for the snapshot
     *
     * @param example the example object to search for
     * @param database the database to snapshot
     * @return the Database Object types to search for
     */
    private Set<Class<? extends DatabaseObject>> initializeSnapshotTypes(DatabaseObject example, Database database) {
        Set<Class<? extends DatabaseObject>> types = new HashSet<>(getContainerTypes(example.getClass(), database));
        types.add(example.getClass());
        return types;
    }

    /**
     * Check if the example object is a liquibase managed table
     *
     * @param example the database object to search for
     * @param database the database to search
     * @param liquibaseTableNames the list of liquibase table names
     * @return true if the table exists, false otherwise
     * @throws DatabaseException if there was an exception encountered when rolling back the postgres database
     */
    private boolean checkLiquibaseTablesExistence(DatabaseObject example, Database database, List<String> liquibaseTableNames) throws DatabaseException {
        if (example instanceof Table && liquibaseTableNames.contains(example.getName())) {
            try {
                if (database instanceof MariaDBDatabase) {
                    // Handle MariaDB differently to avoid the
                    // Error: 1146-42S02: Table 'intuser_db.DATABASECHANGELOGLOCK' doesn't exist
                    String sql = "select table_name from information_schema.TABLES where TABLE_SCHEMA = ? and TABLE_NAME = ?;";
                    List<Map<String, ?>> res = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                            .getExecutor("jdbc", database)
                            .queryForList(new RawParameterizedSqlStatement(sql, database.getLiquibaseCatalogName(), example.getName()));
                    return !res.isEmpty();
                }
                // Does the query concern the DATABASECHANGELOG / DATABASECHANGELOGLOCK table? If so, we do a quick & dirty
                // SELECT COUNT(*) on that table. If that works, we count that as confirmation of existence.
                // @todo Actually, there may be extreme cases (distorted table statistics etc.) where a COUNT(*) might not be so cheap. Maybe SELECT a dummy constant is the better way?
                Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForInt(
                        new RawParameterizedSqlStatement(String.format("SELECT COUNT(*) FROM %s",
                                database.escapeObjectName(database.getLiquibaseCatalogName(),
                                        database.getLiquibaseSchemaName(), example.getName(), Table.class))));
                return true;
            } catch (DatabaseException e) {
                if (database instanceof PostgresDatabase) {
                    database.rollback(); // throws "current transaction is aborted" unless we roll back the connection
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Create and search the database snapshot for the example object.
     *
     * @param example The DatabaseObject to check for existence
     * @param database The DBMS in which the object might exist
     * @param snapshotControl the snapshot configuration to use
     * @return true if the example object exists, false otherwise
     * @throws DatabaseException if there was a problem searching the DBMS
     * @throws InvalidExampleException if provided an invalid example DatabaseObject
     */
    private boolean createAndCheckSnapshot(DatabaseObject example, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        if (createSnapshot(example, database, snapshotControl) != null) {
            return true;
        }
        CatalogAndSchema catalogAndSchema = example.getSchema() == null ? database.getDefaultSchema() : example.getSchema().toCatalogAndSchema();
        SnapshotControl replacedSnapshotControl = new SnapshotControl(database, false, example.getClass());
        replacedSnapshotControl.setWarnIfObjectNotFound(false);
        replacedSnapshotControl.setSearchNestedObjects(snapshotControl.shouldSearchNestedObjects());
        DatabaseSnapshot snapshot = createSnapshot(catalogAndSchema, database, replacedSnapshotControl);
        for (DatabaseObject obj : snapshot.get(example.getClass())) {
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(example, obj, null, database)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a specific object is present in a database
     *
     * @param example  The DatabaseObject to check for existence
     * @param database The DBMS in which the object might exist
     * @return true if object exists, false otherwise
     * @throws DatabaseException       If a problem occurs in the DBMS-specific code
     * @throws InvalidExampleException If the object cannot be checked properly, e.g. if the object name is ambiguous
     */
    public boolean has(DatabaseObject example, Database database) throws DatabaseException, InvalidExampleException {
        return checkExistence(example, database, true);
    }

    /**
     * Checks if a specific object is present in a database. When using this method the database snapshot will
     * NOT search through nested objects.
     *
     * @param example  The DatabaseObject to check for existence
     * @param database The DBMS in which the object might exist
     * @return true if object exists, false otherwise
     * @throws DatabaseException       If a problem occurs in the DBMS-specific code
     * @throws InvalidExampleException If the object cannot be checked properly, e.g. if the object name is ambiguous
     */
    public boolean hasIgnoreNested(DatabaseObject example, Database database) throws DatabaseException, InvalidExampleException {
        return checkExistence(example, database, false);
    }

    /**
     * Common method to check if a specific object is present in a database
     *
     * @param example   The DatabaseObject to check for existence
     * @param database  The DBMS in which the object might exist
     * @param searchNestedObjects Whether to use fast check
     * @return true if object existence can be confirmed, false otherwise
     * @throws DatabaseException       If a problem occurs in the DBMS-specific code
     * @throws InvalidExampleException If the object cannot be checked properly, e.g. if the object name is ambiguous
     */
    private boolean checkExistence(DatabaseObject example, Database database, boolean searchNestedObjects) throws DatabaseException, InvalidExampleException {
        Set<Class<? extends DatabaseObject>> types = initializeSnapshotTypes(example, database);
        LiquibaseTableNamesFactory liquibaseTableNamesFactory = Scope.getCurrentScope().getSingleton(LiquibaseTableNamesFactory.class);
        List<String> liquibaseTableNames = liquibaseTableNamesFactory.getLiquibaseTableNames(database);

        if (checkLiquibaseTablesExistence(example, database, liquibaseTableNames)) {
            return true;
        }

        SnapshotControl snapshotControl = new SnapshotControl(database, false, types.toArray(new Class[0]));
        snapshotControl.setWarnIfObjectNotFound(false);
        snapshotControl.setSearchNestedObjects(searchNestedObjects);

        return createAndCheckSnapshot(example, database, snapshotControl);
    }

    public DatabaseSnapshot createSnapshot(CatalogAndSchema example, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        return createSnapshot(new CatalogAndSchema[] {example}, database, snapshotControl);
    }

    /**
     * Creates a database snapshot for a given array of catalog/schema combinations.
     *
     * @param examples        an array of CatalogAndSchema objects
     * @param database        the database to work on
     * @param snapshotControl the options/settings for snapshot generation
     * @return a database snapshot that includes all objects matching the specification
     * @throws DatabaseException       if a problem occurs during snapshotting
     * @throws InvalidExampleException if the given catalog/schema combinations are invalid (e.g. duplicates)
     */
    public DatabaseSnapshot createSnapshot(CatalogAndSchema[] examples, Database database,
                                           SnapshotControl snapshotControl)
            throws DatabaseException, InvalidExampleException {
        if (database == null) {
            return null;
        }

        /*
         * Translate the given examples into "real" Schema objects according to the database's abilities (e.g.
         * does the DB support catalogs? schemas? are the given schema/catalog names the default ones for the
         * DB connection?)
          */
        Schema[] schemas = new Schema[examples.length];
        for (int i = 0; i< schemas.length; i++) {
            examples[i] = examples[i].customize(database);
            schemas[i] = new Schema(examples[i].getCatalogName(), examples[i].getSchemaName());
        }

        Scope.getCurrentScope().getLog(SnapshotGeneratorFactory.class).info("Creating snapshot");
        DatabaseSnapshot snapshot = createSnapshot(schemas, database, snapshotControl);

        //
        // For SQL Server, try to set the backing index for primary key and
        // unique constraints to the index that is created automatically
        //
        if (database instanceof MSSQLDatabase) {
            Set<PrimaryKey> primaryKeys = snapshot.get(PrimaryKey.class);
            primaryKeys.forEach(pk -> {
                if (pk != null) {
                    syncBackingIndex(pk, Index.class, snapshot);
                }
            });
            Set<UniqueConstraint> uniqueConstraints = snapshot.get(UniqueConstraint.class);
            uniqueConstraints.forEach(uc -> {
                if (uc != null) {
                    syncBackingIndex(uc, Index.class, snapshot);
                }
            });
        }
        return snapshot;
    }

    /**
     * Creates a database snapshot for a given array of DatabaseObjects
     *
     * @param examples        an array of DatabaseObjects objects
     * @param database        the database to work on
     * @param snapshotControl the options/settings for snapshot generation
     * @return a database snapshot that includes all objects matching the specification
     * @throws DatabaseException       if a problem occurs during snapshotting
     * @throws InvalidExampleException if the given catalog/schema combinations are invalid (e.g. duplicates)
     */
    public DatabaseSnapshot createSnapshot(DatabaseObject[] examples, Database database,
                                           SnapshotControl snapshotControl)
            throws DatabaseException, InvalidExampleException {
        DatabaseConnection conn = database.getConnection();
        if (conn == null) {
            return new EmptyDatabaseSnapshot(database, snapshotControl);
        }
        if (conn instanceof OfflineConnection) {
            DatabaseSnapshot snapshot = ((OfflineConnection) conn).getSnapshot(examples);
            if (snapshot == null) {
                throw new DatabaseException("No snapshotFile parameter specified for offline database");
            }
            return snapshot;
        }
        return new JdbcDatabaseSnapshot(examples, database, snapshotControl);
    }

    /**
     * Creates a DatabaseSnapshot for a single DatabaseObject.
     * @param example the object to snapshot
     * @param database the database to work on
     * @param <T> the type of the object (must extend DatabaseObject)
     * @return the snapshot of the desired object
     * @throws DatabaseException if a problem occurs during snapshotting
     * @throws InvalidExampleException if the given catalog/schema combinations are invalid (e.g. duplicates)
     */
    public <T extends DatabaseObject> T createSnapshot(T example, Database database) throws DatabaseException, InvalidExampleException {
        return createSnapshot(example, database, new SnapshotControl(database));
    }

    /**
     *
     * Creates a DatabaseSnapshot for a single DatabaseObject.
     * @param example the object to snapshot
     * @param database the database to work on
     * @param snapshotControl the options/settings for snapshot generation
     * @param <T> the type of the object (must extend DatabaseObject)
     * @return the snapshot of the desired object
     * @throws DatabaseException if a problem occurs during snapshotting
     * @throws InvalidExampleException if the given catalog/schema combinations are invalid (e.g. duplicates)
     */
    public <T extends DatabaseObject> T createSnapshot(T example, Database database, SnapshotControl snapshotControl)
            throws DatabaseException, InvalidExampleException {
        DatabaseSnapshot snapshot = createSnapshot(new DatabaseObject[]{example}, database, snapshotControl);
        return snapshot.get(example);
    }

    public Table getDatabaseChangeLogTable(SnapshotControl snapshotControl, Database database) throws DatabaseException {
        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            Table liquibaseTable = (Table) new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(
                    new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()));
            return createSnapshot(liquibaseTable, database, snapshotControl);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    public Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            Table example = (Table) new Table().setName(database.getDatabaseChangeLogLockTableName()).setSchema(
                    new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()));
            return createSnapshot(example, database);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    public boolean hasDatabaseChangeLogTable(Database database) throws DatabaseException {
        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            return has(new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(new Schema(
                    database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    public boolean hasDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            return has(new Table().setName(database.getDatabaseChangeLogLockTableName()).setSchema(
                    new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    public Set<Class<? extends DatabaseObject>> getContainerTypes(Class<? extends DatabaseObject> type,
                                                                  Database database) {
        Set<Class<? extends DatabaseObject>>  returnSet = new HashSet<>();

        getContainerTypes(type, database, returnSet);

        return returnSet;
    }

    private void getContainerTypes(Class<? extends DatabaseObject> type, Database database,
                                   Set<Class<? extends DatabaseObject>>  returnSet) {
        Class<? extends DatabaseObject>[] addsTo;

        // Have we already seen this type?
        if (!returnSet.add(type)) {
            return;
        }

        // Get a list of the SnapshotGenerators that are in charge of snapshotting
        // object type "type" in the DBMS "database"
        SortedSet<SnapshotGenerator> generators = getGenerators(type, database);

        if ((generators != null) && !generators.isEmpty()) {
            SnapshotGenerator generator = generators.iterator().next();
            addsTo = generator.addsTo();
            if (addsTo != null) {
                for (Class<? extends DatabaseObject> newType : addsTo) {
                    returnSet.add(newType);
                    getContainerTypes(newType, database, returnSet);
                }
            }
        }

    }

    private void syncBackingIndex(DatabaseObject databaseObject, Class<? extends DatabaseObject> clazz, DatabaseSnapshot snapshot) {
        if (!(databaseObject instanceof PrimaryKey) && !(databaseObject instanceof UniqueConstraint)) {
            return;
        }
        Set<Index> indices = (Set<Index>)snapshot.get(clazz);
        indices.forEach(index -> {
            final Index backingIndex = databaseObject.getAttribute("backingIndex", Index.class);
            if (backingIndex.getName().equals(index.getName()) && index != backingIndex) {
                databaseObject.setAttribute("backingIndex", index);
                List<Column> columns = (List<Column>)databaseObject.getAttribute("columns", List.class);
                columns.clear();
                columns.addAll(index.getColumns());
            }
        });
    }
}
