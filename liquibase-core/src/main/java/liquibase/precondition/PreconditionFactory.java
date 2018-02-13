package liquibase.precondition;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

public class PreconditionFactory {
    @SuppressWarnings("unchecked")
    private final Map<String, Class<? extends Precondition>> preconditions;

    private static PreconditionFactory instance;

    @SuppressWarnings("unchecked")
    private PreconditionFactory() {
        preconditions = new HashMap<>();
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
        return preconditions;
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
            return (Precondition) aClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
