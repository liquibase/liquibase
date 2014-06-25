package liquibase.statementlogic;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.action.visitor.ActionVisitor;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.servicelocator.ServiceLocator;
import liquibase.statement.Statement;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * StatementLogicFactory is a singleton registry of StatementLogic instances, used to convert {@link liquibase.statement.Statement} instances to {@link liquibase.action.Action} instances.
 * <p>
 * The Statement to Action conversion is designed to be extensible, with StatementLogic implementations being found dynamically and able to control their priority and work together via a {@link liquibase.statementlogic.StatementLogicChain}.
 * <p>
 * Use the {@link #register(StatementLogic)} method to add custom StatementLogic implementations,
 * and the getBestGenerator() method to retrieve the StatementLogic that should be used for a given SqlStatement.
 */
public class StatementLogicFactory {

    private static StatementLogicFactory instance;

    private List<StatementLogic> registry = new ArrayList<StatementLogic>();

    //caches for expensive reflection based calls that slow down Liquibase initialization: CORE-1207
    private final Map<Class<?>, Type[]> genericInterfacesCache = new HashMap<Class<?>, Type[]>();
    private final Map<Class<?>, Type> genericSuperClassCache = new HashMap<Class<?>, Type>();

    private StatementLogicFactory() {
        Class[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(StatementLogic.class);

            for (Class clazz : classes) {
                register((StatementLogic) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Return singleton StatementLogicFactory
     */
    public static StatementLogicFactory getInstance() {
        if (instance == null) {
            instance = new StatementLogicFactory();
        }
        return instance;
    }

    /**
     * Reset the singleton. Mainly used for testing.
     */
    public static void reset() {
        instance = new StatementLogicFactory();
    }


    /**
     * Register a new StatementLogic implementation. Will be included in future calls to {@link #generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment)}
     */
    public void register(StatementLogic logic) {
        registry.add(logic);
    }

    /**
     * Unregister a StatementLogic implementation. Will no longer be included in future calls to {@link #generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment)}
     */
    public void unregister(StatementLogic generator) {
        registry.remove(generator);
    }

    /**
     * Unregister a StatementLogic implementation.
     *
     * @see #unregister(StatementLogic)
     */
    public void unregister(Class generatorClass) {
        StatementLogic toRemove = null;
        for (StatementLogic existingGenerator : registry) {
            if (existingGenerator.getClass().equals(generatorClass)) {
                toRemove = existingGenerator;
            }
        }

        unregister(toRemove);
    }


    /**
     * Returns an unmodifiable collection of all the StatementLogic instances in this factory.
     */
    protected Collection<StatementLogic> getRegistry() {
        return Collections.unmodifiableCollection(registry);
    }

    /**
     * Returns the StatementLogic implementations that support the given statement. SortedSet is ordered by execution order.
     */
    protected SortedSet<StatementLogic> getStatementLogic(Statement statement, ExecutionEnvironment env) {
        SortedSet<StatementLogic> validGenerators = new TreeSet<StatementLogic>(new StatementLogicComparator());

        for (StatementLogic generator : getRegistry()) {
            Class clazz = generator.getClass();
            Type classType = null;
            while (clazz != null) {
                if (classType instanceof ParameterizedType) {
                    checkType(classType, statement, generator, env, validGenerators);
                }

                for (Type type : getGenericInterfaces(clazz)) {
                    if (type instanceof ParameterizedType) {
                        checkType(type, statement, generator, env, validGenerators);
                    } else if (isTypeEqual(type, StatementLogic.class)) {
                        //noinspection unchecked
                        if (generator.supports(statement, env)) {
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

    private void checkType(Type type, Statement statement, StatementLogic generator, ExecutionEnvironment env, Collection<StatementLogic> validGenerators) {
        for (Type typeClass : ((ParameterizedType) type).getActualTypeArguments()) {
            if (typeClass instanceof TypeVariable) {
                typeClass = ((TypeVariable) typeClass).getBounds()[0];
            }

            if (isTypeEqual(typeClass, Statement.class)) {
                return;
            }

            if (((Class) typeClass).isAssignableFrom(statement.getClass())) {
                if (generator.supports(statement, env)) {
                    validGenerators.add(generator);
                }
            }
        }

    }

    /**
     * Create a StatementLogicChain for the given statement. Return null if there are no StatementLogic implementations that support the Statement.
     */
    protected StatementLogicChain createGeneratorChain(Statement statement, ExecutionEnvironment env) {
        SortedSet<StatementLogic> statementLogic = getStatementLogic(statement, env);
        if (statementLogic == null || statementLogic.size() == 0) {
            return null;
        }
        //noinspection unchecked
        return new StatementLogicChain(statementLogic);
    }

    /**
     * Convenience method to generate Actions based on the passed Change.
     */
    public Action[] generateActions(Change change, ExecutionEnvironment env) {
        Statement[] statements = change.generateStatements(env);
        if (statements == null) {
            return new Action[0];
        } else {
            return generateActions(statements, env);
        }
    }

    /**
     * Convenience method to generate a single array of Actions based on the array of Statements passed.
     */
    public Action[] generateActions(Statement[] statements, ExecutionEnvironment env) {
        List<Action> returnList = new ArrayList<Action>();
        for (Statement statement : statements) {
            returnList.addAll(Arrays.asList(StatementLogicFactory.getInstance().generateActions(statement, env)));
        }

        return returnList.toArray(new Action[returnList.size()]);
    }

    /**
     * Converts the given Statement to Action instances based on the StatementLogic implementations configured in this factory.
     */
    public Action[] generateActions(Statement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();
        if (!supports(statement, env)) {
            throw new UnexpectedLiquibaseException("Unsupported database for "+statement.toString()+": "+database.getShortName());
        }
        ValidationErrors validate = validate(statement, env);
        if (validate.hasErrors()) {
            throw new UnexpectedLiquibaseException("Validation failed: "+validate.toString());
        }
        StatementLogicChain generatorChain = createGeneratorChain(statement, env);
        if (generatorChain == null) {
            throw new IllegalStateException("Cannot find "+statement.getClass().getName()+" generators for "+database.toString());
        }
        Action[] actions = generatorChain.generateActions(statement, env);
        if (actions == null) {
            return new Action[0];
        }

        if (env.getCurrentChangeSet() != null) {
            List<ActionVisitor> sqlVisitors = env.getCurrentChangeSet().getActionVisitors();
            if (sqlVisitors != null) {
                for (ActionVisitor visitor : sqlVisitors) {
                    for (Action action : actions) {
                        visitor.visit(action, env);
                    }
                }
            }
        }
        return actions;
    }

    /**
     * Returns true if the given statement can be converted to Actions in the given ExecutionEnvironment.
     */
    public boolean supports(Statement statement, ExecutionEnvironment env) {
        return getStatementLogic(statement, env).size() > 0;
    }

    /**
     * Returns object containing validation errors for the given statement/execution environment combination.
     * Returns empty ValidationErrors if validation passes.
     * Relies on validation logic in the applicable {@link liquibase.statementlogic.StatementLogic} classes.
     */
    public ValidationErrors validate(Statement statement, ExecutionEnvironment env) {
        //noinspection unchecked
        StatementLogicChain generatorChain = createGeneratorChain(statement, env);
        if (generatorChain == null) {
            throw new UnexpectedLiquibaseException("Unable to create generator chain for "+statement.getClass().getName()+" on "+env.getTargetDatabase().getShortName());
        }
        return generatorChain.validate(statement, env);
    }

    /**
     * Returns object containing non-fatal warnings for the given statement/execution environment combination.
     * Returns empty Warnings if validation passes.
     * Relies on warn in the applicable {@link liquibase.statementlogic.StatementLogic} classes.
     */
    public Warnings warn(Statement statement, ExecutionEnvironment env) {
        //noinspection unchecked
        return createGeneratorChain(statement, env).warn(statement, env);
    }

    /**
     * Returns true if the StatementLogic implementations <b>may</b> require interacting with the outside environment in {@link #generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment)}.
     *
     * For example, a logic implementation needs to read all available tables, or query existing data to create the Action instances, this should return true.
     * If a logic implementation returns different Action objects depending on the time of day, this should return true.
     */
    public boolean generateActionsIsVolatile(Statement statement, ExecutionEnvironment env) {
        for (StatementLogic generator : getStatementLogic(statement, env)) {
            if (generator.generateActionsIsVolatile(env)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Standard comparator for sorting StatementLogic. Orders so highest priority is first, lowest priority is last.
     * If two StatementLogic instances have the same priority, they are sorted alphabetically by class name.
     */
    public static class StatementLogicComparator implements Comparator<StatementLogic> {
        @Override
        public int compare(StatementLogic o1, StatementLogic o2) {
            int i = -1 * new Integer(o1.getPriority()).compareTo(o2.getPriority());
            if (i == 0) {
                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }
            return i;
        }
    }
}
