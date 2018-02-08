package liquibase.servicelocator;

import liquibase.Scope;
import liquibase.exception.ServiceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class StandardServiceLocator implements ServiceLocator {

    @Override
    public Object newInstance(Class requiredInterface) throws ServiceNotFoundException {
        try {
            return findClass(requiredInterface).newInstance();
        } catch (Exception e) {
            throw new ServiceNotFoundException(e);
        }
    }

    @Override
    public Class findClass(Class requiredInterface) throws ServiceNotFoundException {
        Class[] classes = findClasses(requiredInterface);
        if (classes.length != 1) {
            throw new ServiceNotFoundException("Expected 1 " + requiredInterface + " implementation, found " + classes.length);
        }
        return classes[0];
    }

    @Override
    public <T> Class<? extends T>[] findClasses(Class<T> requiredInterface) throws ServiceNotFoundException {
        List<Class<T>> allInstances = new ArrayList<>();

        for (T t : ServiceLoader.load(requiredInterface, Scope.getCurrentScope().getClassLoader(true))) {
            allInstances.add((Class<T>) t.getClass());
        }

        return allInstances.toArray(new Class[allInstances.size()]);

    }
}
