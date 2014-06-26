package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.action.MetaDataQueryAction;
import liquibase.action.QueryAction;
import liquibase.exception.UnsupportedException;
import liquibase.statement.Statement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.core.ColumnSnapshotGenerator;
import liquibase.snapshot.core.TableSnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

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
//        QueryResult results = ExecutorService.getInstance().getExecutor(database).query(new FetchObjectsStatement(example), new ExecutionOptions(new RuntimeEnvironment(database, null)));
//        DatabaseObject result = results.toObject(example.getClass());
//
//
//
//        return (T) result;

        AbstractSnapshotGenerator generator = new TableSnapshotGenerator();
        List<Statement> statements = new ArrayList<Statement>();
        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(database);
        statements.add(generator.generateLookupStatement(example, executionEnvironment, new StatementLogicChain(null)));


        Statement[] addToStatements = new TableSnapshotGenerator().generateAddToStatements(example, executionEnvironment, new StatementLogicChain(null));
        if (addToStatements != null) {
            statements.addAll(Arrays.asList(addToStatements));
        }
        addToStatements = new ColumnSnapshotGenerator().generateAddToStatements(example, executionEnvironment, new StatementLogicChain(null));
        if (addToStatements != null) {
            statements.addAll(Arrays.asList(addToStatements));
        }

        DatabaseObjectCollection collection = new DatabaseObjectCollection(database);
        List<Action> actions = new ArrayList<Action>();
        for (Statement statement : statements) {
            if (StatementLogicFactory.getInstance().supports(statement, executionEnvironment)) {
                try {
                    Action[] actionArray = StatementLogicFactory.getInstance().generateActions(statement, new ExecutionEnvironment(database));
                    if (actionArray == null || actionArray.length == 0) {
                        continue;
                    }
                    if (actionArray.length == 1) {
                        actions.add(actionArray[0]);
                    }
                    if (actionArray.length > 1) {
                        throw new UnexpectedLiquibaseException("Too many actions generated from "+statement);
                    }
                } catch (UnsupportedException e) {
                    throw new DatabaseException(e);
                }
            }
        }

        for (Action action : mergeActions(actions)) {
            for (DatabaseObject object : ((QueryAction) action).query(executionEnvironment).toList(DatabaseObject.class)) {
                collection.add(object);
            }
        }

        for (DatabaseObject object : collection.get(Table.class)) {
            new ColumnSnapshotGenerator().addTo((Table) object, collection, executionEnvironment, new StatementLogicChain(null));
        }

        for (DatabaseObject object : collection.get(Schema.class)) {
            new TableSnapshotGenerator().addTo((Table) object, collection, executionEnvironment, new StatementLogicChain(null));
        }

        System.out.println(collection);

        return collection.get(example);
    }

    public List<Action> mergeActions(List<Action> originalList) {
        if (originalList == null || originalList.size() < 2) {
            return originalList;
        }

        List<Action> returnList = new ArrayList<Action>(originalList);

        for (int i=0; i< returnList.size(); i++) {
            Action baseAction = returnList.get(i);
            ListIterator<Action> iterator = returnList.listIterator(i + 1);
            while (iterator.hasNext()) {
                MetaDataQueryAction compareAction = (MetaDataQueryAction) iterator.next();
                if (((MetaDataQueryAction) baseAction).merge(compareAction)) {
                    iterator.remove();
                }
            }
        }

        return returnList;
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
