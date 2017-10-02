package liquibase.precondition;

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
        Class[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(Precondition.class);

            for (Class<? extends Precondition> clazz : classes) {
                    register(clazz);
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

    public void register(Class<? extends Precondition> clazz) {
        try {
            preconditions.put(clazz.newInstance().getName(), clazz);
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
