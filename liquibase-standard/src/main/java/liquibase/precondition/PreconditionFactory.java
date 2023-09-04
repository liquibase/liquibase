package liquibase.precondition;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PreconditionFactory {
    private final Map<String, Class<? extends Precondition>> preconditions = new ConcurrentHashMap<>();

    private static PreconditionFactory instance;

    private PreconditionFactory() {
        try {
            for (Precondition precondition : Scope.getCurrentScope().getServiceLocator().findInstances(Precondition.class)) {
                register(precondition);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public static synchronized PreconditionFactory getInstance() {
        if (instance == null) {
            instance = new PreconditionFactory();
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = new PreconditionFactory();
    }

    public Map<String, Class<? extends Precondition>> getPreconditions() {
        return Collections.unmodifiableMap(preconditions);
    }

    public void register(Precondition precondition) {
        try {
            preconditions.put(precondition.getName(), precondition.getClass());
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void unregister(String name) {
        preconditions.remove(name);
    }

    /**
     * Create a new Precondition subclass based on the given tag name.
     */
    public Precondition create(String tagName) {
        Class<?> aClass = preconditions.get(tagName);
        if (aClass == null) {
            return null;
        }
        try {
            return (Precondition) aClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
