package liquibase.statementlogic;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.action.visitor.ActionVisitor;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.exception.UnsupportedException;
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
        if (logic == null) {
            return;
        }
        registry.add(logic);
    }

    /**
     * Unregister a StatementLogic implementation. Will no longer be included in future calls to {@link #generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment)}
     */
    public void unregister(StatementLogic logic) {
        if (logic == null) {
            return;
        }
        registry.remove(logic);
    }

    /**
     * Unregister a StatementLogic implementation.
     *
     * @see #unregister(StatementLogic)
     */
    public void unregister(Class<? extends StatementLogic> logicClass) {
        if (logicClass == null) {
            return;
        }

        for (StatementLogic existingLogic : registry) {
            if (existingLogic.getClass().equals(logicClass)) {
                unregister(existingLogic);
                return;
            }
        }
    }

    /**
     * Returns an unmodifiable collection of all the StatementLogic instances in this factory. Mainly used for testing.
     */
    protected Collection<StatementLogic> getRegistry() {
        return Collections.unmodifiableCollection(registry);
    }

    /**
     *  Clears all entries from the registry. Mainly used for testing.
     */
    public void clearRegistry() {
        registry.clear();
    }

    /**
     * Returns the StatementLogic implementations that support the given statement. SortedSet is ordered by execution order.
     */
    protected SortedSet<StatementLogic> getStatementLogic(Statement statement, ExecutionEnvironment env) {
        SortedSet<StatementLogic> validLogic = new TreeSet<StatementLogic>(new StatementLogicComparator());

        for (StatementLogic logic : getRegistry()) {
            Class clazz = logic.getClass();
            Type classType = null;
            while (clazz != null) {
                if (classType instanceof ParameterizedType) {
                    checkType(classType, statement, logic, env, validLogic);
                }

                for (Type type : getGenericInterfaces(clazz)) {
                    if (type instanceof ParameterizedType) {
                        checkType(type, statement, logic, env, validLogic);
                    } else if (isTypeEqual(type, StatementLogic.class)) {
                        //noinspection unchecked
                        if (logic.supports(statement, env)) {
                            validLogic.add(logic);
                        }
                    }
                }
                classType = getGenericSuperclass(clazz);
                clazz = clazz.getSuperclass();
            }
        }

        return validLogic;
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

    private void checkType(Type type, Statement statement, StatementLogic logic, ExecutionEnvironment env, Collection<StatementLogic> validLogic) {
        for (Type typeClass : ((ParameterizedType) type).getActualTypeArguments()) {
            if (typeClass instanceof TypeVariable) {
                typeClass = ((TypeVariable) typeClass).getBounds()[0];
            }

            if (isTypeEqual(typeClass, Statement.class)) {
                return;
            }

            if (((Class) typeClass).isAssignableFrom(statement.getClass())) {
                if (logic.supports(statement, env)) {
                    validLogic.add(logic);
                }
            }
        }

    }

    /**
     * Create a StatementLogicChain for the given statement. Return null if there are no StatementLogic implementations that support the Statement.
     */
    protected StatementLogicChain createStatementLogicChain(Statement statement, ExecutionEnvironment env) {
        SortedSet<StatementLogic> statementLogic = getStatementLogic(statement, env);
        if (statementLogic == null || statementLogic.size() == 0) {
            return null;
        }
        //noinspection unchecked
        return new StatementLogicChain(statementLogic);
    }

    /**
     * Convenience method to generate Actions based on the passed Change.
     * @throws liquibase.exception.UnsupportedException if the change is not supported for the ExecutionEnvironment
     */
    public Action[] generateActions(Change change, ExecutionEnvironment env) throws UnsupportedException {
        Statement[] statements = change.generateStatements(env);
        if (statements == null) {
            return new Action[0];
        } else {
            return generateActions(statements, env);
        }
    }

    /**
     * Convenience method to generate a single array of Actions based on the array of Statements passed.
     *
     * @throws liquibase.exception.UnsupportedException if any of the statements are not supported for the ExecutionEnvironment
     */
    public Action[] generateActions(Statement[] statements, ExecutionEnvironment env) throws UnsupportedException {
        List<Action> returnList = new ArrayList<Action>();
        if (statements != null) {
            for (Statement statement : statements) {
                returnList.addAll(Arrays.asList(StatementLogicFactory.getInstance().generateActions(statement, env)));
            }
        }

        return returnList.toArray(new Action[returnList.size()]);
    }

    /**
     * Converts the given Statement to Action instances based on the StatementLogic implementations configured in this factory.
     *
     * @throws liquibase.exception.UnsupportedException if the statement is not supported for the ExecutionEnvironment
     */
    public Action[] generateActions(Statement statement, ExecutionEnvironment env) throws UnsupportedException {
        if (statement == null) {
            return new Action[0];
        }
        Database database = env.getTargetDatabase();
        if (!supports(statement, env)) {
            throw new UnsupportedException("Unsupported database for '"+statement.toString()+"': "+database.getShortName());
        }
        ValidationErrors validate = validate(statement, env);
        if (validate.hasErrors()) {
            throw new UnsupportedException("Validation failed: "+validate.toString());
        }
        StatementLogicChain statementLogicChain = createStatementLogicChain(statement, env);
        if (statementLogicChain == null) {
            throw new UnsupportedException("Cannot find "+statement.getClass().getName()+" StatementLogic implementation(s) for "+database.toString());
        }
        Action[] actions = statementLogicChain.generateActions(statement, env);
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
     * A null statement returns true.
     */
    public boolean supports(Statement statement, ExecutionEnvironment env) {
        if (statement == null) {
            return true;
        }
        return getStatementLogic(statement, env).size() > 0;
    }

    /**
     * Returns object containing validation errors for the given statement/execution environment combination.
     * Returns empty ValidationErrors if validation passes.
     * Relies on validation logic in the applicable {@link liquibase.statementlogic.StatementLogic} classes.
     */
    public ValidationErrors validate(Statement statement, ExecutionEnvironment env) {
        //noinspection unchecked
        StatementLogicChain statementLogicChain = createStatementLogicChain(statement, env);
        if (statementLogicChain == null) {
            throw new UnexpectedLiquibaseException("Unable to create statement logic chain for "+statement.getClass().getName()+" on "+env.getTargetDatabase().getShortName());
        }
        return statementLogicChain.validate(statement, env);
    }

    /**
     * Returns object containing non-fatal warnings for the given statement/execution environment combination.
     * Returns empty Warnings if validation passes.
     * Relies on warn in the applicable {@link liquibase.statementlogic.StatementLogic} classes.
     */
    public Warnings warn(Statement statement, ExecutionEnvironment env) {
        //noinspection unchecked
        return createStatementLogicChain(statement, env).warn(statement, env);
    }

    /**
     * Returns true if the StatementLogic implementations <b>may</b> require interacting with the outside environment in {@link #generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment)}.
     *
     * For example, a logic implementation needs to read all available tables, or query existing data to create the Action instances, this should return true.
     * If a logic implementation returns different Action objects depending on the time of day, this should return true.
     */
    public boolean generateActionsIsVolatile(Statement statement, ExecutionEnvironment env) {
        for (StatementLogic logic : getStatementLogic(statement, env)) {
            if (logic.generateActionsIsVolatile(env)) {
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
