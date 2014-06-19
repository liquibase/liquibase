package liquibase.actiongenerator;

import liquibase.action.Action;
import liquibase.action.visitor.ActionVisitor;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutionOptions;
import liquibase.servicelocator.ServiceLocator;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
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

    public SortedSet<ActionGenerator> getGenerators(SqlStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        String databaseName = null;
        if (database == null) {
            databaseName = "NULL";
        } else {
            databaseName = database.getShortName();
        }
        String key = statement.getClass().getName()+":"+ databaseName;

        SortedSet<ActionGenerator> validGenerators = new TreeSet<ActionGenerator>(new ActionGeneratorComparator());

        for (ActionGenerator generator : getGenerators()) {
            Class clazz = generator.getClass();
            Type classType = null;
            while (clazz != null) {
                if (classType instanceof ParameterizedType) {
                    checkType(classType, statement, generator, options, validGenerators);
                }

                for (Type type : getGenericInterfaces(clazz)) {
                    if (type instanceof ParameterizedType) {
                        checkType(type, statement, generator, options, validGenerators);
                    } else if (isTypeEqual(type, ActionGenerator.class)) {
                        //noinspection unchecked
                        if (generator.supports(statement, options)) {
                            validGenerators.add(generator);
                        }
                    }
                }
                classType = getGenericSuperclass(clazz);
                clazz = clazz.getSuperclass();
            }
        }

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

    private void checkType(Type type, SqlStatement statement, ActionGenerator generator, ExecutionOptions options, SortedSet<ActionGenerator> validGenerators) {
        for (Type typeClass : ((ParameterizedType) type).getActualTypeArguments()) {
            if (typeClass instanceof TypeVariable) {
                typeClass = ((TypeVariable) typeClass).getBounds()[0];
            }

            if (isTypeEqual(typeClass, SqlStatement.class)) {
                return;
            }

            if (((Class) typeClass).isAssignableFrom(statement.getClass())) {
                if (generator.supports(statement, options)) {
                    validGenerators.add(generator);
                }
            }
        }

    }

    private ActionGeneratorChain createGeneratorChain(SqlStatement statement, ExecutionOptions options) {
        SortedSet<ActionGenerator> ActionGenerators = getGenerators(statement, options);
        if (ActionGenerators == null || ActionGenerators.size() == 0) {
            return null;
        }
        //noinspection unchecked
        return new ActionGeneratorChain(ActionGenerators);
    }

    public Action[] generateActions(Change change, ExecutionOptions options) {
        SqlStatement[] sqlStatements = change.generateStatements(options);
        if (sqlStatements == null) {
            return new Sql[0];
        } else {
            return generateActions(sqlStatements, options);
        }
    }

    public Action[] generateActions(SqlStatement[] statements, ExecutionOptions options) {
        List<Action> returnList = new ArrayList<Action>();
        for (SqlStatement statement : statements) {
            returnList.addAll(Arrays.asList(ActionGeneratorFactory.getInstance().generateActions(statement, options)));
        }

        return returnList.toArray(new Sql[returnList.size()]);
    }

    public Action[] generateActions(SqlStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        if (!supports(statement, options)) {
            throw new UnexpectedLiquibaseException("Unsupported database for "+statement.toString()+": "+database.getShortName());
        }
        ValidationErrors validate = validate(statement, options);
        if (validate.hasErrors()) {
            throw new UnexpectedLiquibaseException("Validation failed: "+validate.toString());
        }
        ActionGeneratorChain generatorChain = createGeneratorChain(statement, options);
        if (generatorChain == null) {
            throw new IllegalStateException("Cannot find "+statement.getClass().getName()+" generators for "+database.toString());
        }
        Action[] actions = generatorChain.generateActions(statement, options);
        if (actions == null) {
            return new Sql[0];
        }

        List<SqlVisitor> sqlVisitors = options.getSqlVisitors();
        if (sqlVisitors != null) {
            for (ActionVisitor visitor : sqlVisitors) {
                for (Action action : actions) {
                    visitor.visit(action, options);
                }
            }
        }
        return actions;
    }

    public boolean supports(SqlStatement statement, ExecutionOptions options) {
        return getGenerators(statement, options).size() > 0;
    }

    public ValidationErrors validate(SqlStatement statement, ExecutionOptions options) {
        //noinspection unchecked
        ActionGeneratorChain generatorChain = createGeneratorChain(statement, options);
        if (generatorChain == null) {
            throw new UnexpectedLiquibaseException("Unable to create generator chain for "+statement.getClass().getName()+" on "+options.getRuntimeEnvironment().getTargetDatabase().getShortName());
        }
        return generatorChain.validate(statement, options);
    }

    public Warnings warn(SqlStatement statement, ExecutionOptions options) {
        //noinspection unchecked
        return createGeneratorChain(statement, options).warn(statement, options);
    }
}
