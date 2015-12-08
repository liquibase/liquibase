package liquibase.servicelocator;

import liquibase.Scope;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public abstract class AbstractServiceFactory<T extends Service> {

    private final Scope rootScope;

    protected AbstractServiceFactory(Scope scope) {
        this.rootScope = scope;
    }

    protected abstract Class<T> getServiceClass();

    protected abstract int getPriority(T obj, Scope scope, Object... args);

    protected Scope getRootScope() {
        return rootScope;
    };

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

        for (T service : scope.getSingleton(ServiceLocator.class).findAllServices(getServiceClass())) {
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
