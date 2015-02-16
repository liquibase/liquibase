package liquibase.servicelocator;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.*;

public abstract class AbstractServiceFactory<T extends Service> {

    private List<T> registry = new ArrayList<>();

    protected AbstractServiceFactory(Scope scope) {
        try {
            for (Class clazz : findAllServiceClasses(scope)) {
                register((T) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected Class<? extends T>[] findAllServiceClasses(Scope scope) {
        return scope.getSingleton(ServiceLocator.class).findClasses(getServiceClass());
    }

    /**
     * Registers a new ActionLogic instance for future consideration.
     */
    public void register(T service) {
        this.registry.add(service);
    }

    public List<T> getRegistry() {
        return Collections.unmodifiableList(registry);
    }

    protected abstract Class<T> getServiceClass();

    protected abstract int getPriority(T obj, Scope scope, Object... args);

    protected T getService(final Scope scope, final Object... args) {
        TreeSet<T> applicable = new TreeSet<>(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                Integer o1Priority = getPriority(o1, scope, args);
                Integer o2Priority = getPriority(o2, scope, args);

                int i = o2Priority.compareTo(o1Priority);
                if (i == 0) {
                    return o1.getClass().getName().compareTo(o2.getClass().getName());
                }
                return i;
            }
        });

        for (T service : getRegistry()) {
            if (getPriority(service, scope, args) >= 0) {
                applicable.add(service);
            }
        }

        if (applicable.size() == 0) {
            return null;
        }
        return applicable.iterator().next();

    }

}
