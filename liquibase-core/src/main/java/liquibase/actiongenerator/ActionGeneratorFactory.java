package liquibase.actiongenerator;

import liquibase.action.Action;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.servicelocator.ServiceLocator;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * ActionGeneratorFactory is a singleton registry of ActionGenerators.
 * Use the register(ActionGenerator) method to add custom ActionGenerators,
 * and the getBestGenerator() method to retrieve the ActionGenerator that should be used for a given SqlStatement.
 */
public class ActionGeneratorFactory {

    private static ActionGeneratorFactory instance;

    private List<ActionGenerator> generators = new ArrayList<ActionGenerator>();

    //caches for expensive reflection based calls that slow down Liquibase initialization: CORE-1207
    private final Map<Class<?>, Type[]> genericInterfacesCache = new HashMap<Class<?>, Type[]>();
    private final Map<Class<?>, Type> genericSuperClassCache = new HashMap<Class<?>, Type>();
    private Map<String, SortedSet<ActionGenerator>> generatorsByKey = new HashMap<String, SortedSet<ActionGenerator>>();

    private ActionGeneratorFactory() {
        Class[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(ActionGenerator.class);

            for (Class clazz : classes) {
                register((ActionGenerator) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Return singleton ActionGeneratorFactory
     */
    public static ActionGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new ActionGeneratorFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new ActionGeneratorFactory();
    }


    public void register(ActionGenerator generator) {
        generators.add(generator);
    }

    public void unregister(ActionGenerator generator) {
        generators.remove(generator);
    }

    public void unregister(Class generatorClass) {
        ActionGenerator toRemove = null;
        for (ActionGenerator existingGenerator : generators) {
            if (existingGenerator.getClass().equals(generatorClass)) {
                toRemove = existingGenerator;
            }
        }

        unregister(toRemove);
    }


    public Collection<ActionGenerator> getGenerators() {
        return generators;
    }

    public SortedSet<ActionGenerator> getGenerators(SqlStatement statement, Database database) {
        String databaseName = null;
        if (database == null) {
            databaseName = "NULL";
        } else {
            databaseName = database.getShortName();
        }
        String key = statement.getClass().getName()+":"+ databaseName;

        if (generatorsByKey.containsKey(key)) {
            return generatorsByKey.get(key);
        }

        SortedSet<ActionGenerator> validGenerators = new TreeSet<ActionGenerator>(new ActionGeneratorComparator());

        for (ActionGenerator generator : getGenerators()) {
            Class clazz = generator.getClass();
            Type classType = null;
            while (clazz != null) {
                if (classType instanceof ParameterizedType) {
                    checkType(classType, statement, generator, database, validGenerators);
                }

                for (Type type : getGenericInterfaces(clazz)) {
                    if (type instanceof ParameterizedType) {
                        checkType(type, statement, generator, database, validGenerators);
                    } else if (isTypeEqual(type, ActionGenerator.class)) {
                        //noinspection unchecked
                        if (generator.supports(statement, database)) {
                            validGenerators.add(generator);
                        }
                    }
                }
                classType = getGenericSuperclass(clazz);
                clazz = clazz.getSuperclass();
            }
        }

        generatorsByKey.put(key, validGenerators);
        return validGenerators;
    }

    private Type[] getGenericInterfaces(Class<?> clazz) {
        if(genericInterfacesCache.containsKey(clazz)) {
            return genericInterfacesCache.get(clazz);
        }

        Type[] genericInterfaces = clazz.getGenericInterfaces();
        genericInterfacesCache.put(clazz, genericInterfaces);
        return genericInterfaces;
    }

    private Type getGenericSuperclass(Class<?> clazz) {
        if(genericSuperClassCache.containsKey(clazz)) {
            return genericSuperClassCache.get(clazz);
        }

        Type genericSuperclass = clazz.getGenericSuperclass();
        genericSuperClassCache.put(clazz, genericSuperclass);
        return genericSuperclass;
    }

    private boolean isTypeEqual(Type aType, Class aClass) {
        if (aType instanceof Class) {
            return ((Class) aType).getName().equals(aClass.getName());
        }
        return aType.equals(aClass);
    }

    private void checkType(Type type, SqlStatement statement, ActionGenerator generator, Database database, SortedSet<ActionGenerator> validGenerators) {
        for (Type typeClass : ((ParameterizedType) type).getActualTypeArguments()) {
            if (typeClass instanceof TypeVariable) {
                typeClass = ((TypeVariable) typeClass).getBounds()[0];
            }

            if (isTypeEqual(typeClass, SqlStatement.class)) {
                return;
            }

            if (((Class) typeClass).isAssignableFrom(statement.getClass())) {
                if (generator.supports(statement, database)) {
                    validGenerators.add(generator);
                }
            }
        }

    }

    private ActionGeneratorChain createGeneratorChain(SqlStatement statement, Database database) {
        SortedSet<ActionGenerator> ActionGenerators = getGenerators(statement, database);
        if (ActionGenerators == null || ActionGenerators.size() == 0) {
            return null;
        }
        //noinspection unchecked
        return new ActionGeneratorChain(ActionGenerators);
    }

    public Action[] generateActions(Change change, Database database) {
        SqlStatement[] sqlStatements = change.generateStatements(database);
        if (sqlStatements == null) {
            return new Sql[0];
        } else {
            return generateActions(sqlStatements, database);
        }
    }

    public Action[] generateActions(SqlStatement[] statements, Database database) {
        List<Action> returnList = new ArrayList<Action>();
        for (SqlStatement statement : statements) {
            returnList.addAll(Arrays.asList(ActionGeneratorFactory.getInstance().generateActions(statement, database)));
        }

        return returnList.toArray(new Sql[returnList.size()]);
    }

    public Action[] generateActions(SqlStatement statement, Database database) {
        if (!supports(statement, database)) {
            throw new UnexpectedLiquibaseException("Unsupported database for "+statement.toString()+": "+database.getShortName());
        }
        ValidationErrors validate = validate(statement, database);
        if (validate.hasErrors()) {
            throw new UnexpectedLiquibaseException("Validation failed: "+validate.toString());
        }
        ActionGeneratorChain generatorChain = createGeneratorChain(statement, database);
        if (generatorChain == null) {
            throw new IllegalStateException("Cannot find "+statement.getClass().getName()+" generators for "+database.toString());
        }
        Action[] actions = generatorChain.generateActions(statement, database);
        if (actions == null) {
            return new Sql[0];
        }
        return actions;
    }

    public boolean supports(SqlStatement statement, Database database) {
        return getGenerators(statement, database).size() > 0;
    }

    public ValidationErrors validate(SqlStatement statement, Database database) {
        //noinspection unchecked
        ActionGeneratorChain generatorChain = createGeneratorChain(statement, database);
        if (generatorChain == null) {
            throw new UnexpectedLiquibaseException("Unable to create generator chain for "+statement.getClass().getName()+" on "+database.getShortName());
        }
        return generatorChain.validate(statement, database);
    }

    public Warnings warn(SqlStatement statement, Database database) {
        //noinspection unchecked
        return createGeneratorChain(statement, database).warn(statement, database);
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects(SqlStatement statement, Database database) {
        Set<DatabaseObject> affectedObjects = new HashSet<DatabaseObject>();

        ActionGeneratorChain ActionGeneratorChain = createGeneratorChain(statement, database);
        if (ActionGeneratorChain != null) {
            //noinspection unchecked
            Action[] actions = ActionGeneratorChain.generateActions(statement, database);
            if (actions != null) {
                for (Action action : actions) {
                    affectedObjects.addAll(action.getAffectedDatabaseObjects());
                }
            }
        }

        return affectedObjects;

    }

}
