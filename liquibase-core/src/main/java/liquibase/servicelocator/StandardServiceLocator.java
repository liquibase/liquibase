package liquibase.servicelocator;

import com.sun.org.apache.xpath.internal.operations.Mod;
import liquibase.Scope;
import liquibase.exception.ServiceNotFoundException;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

public class StandardServiceLocator implements ServiceLocator {

    @Override
    public <T> List<T> findInstances(Class<T> interfaceType) throws ServiceNotFoundException {
        List<T> allInstances = new ArrayList<>();

        for (T t : ServiceLoader.load(interfaceType, Scope.getCurrentScope().getClassLoader(true))) {
            allInstances.add(t);
        }

        return Collections.unmodifiableList(allInstances);

    }

    @Override
    public <T> List<Class<? extends T>> findClasses(Class<T> interfaceType) throws ServiceNotFoundException {
        List<Class<T>> allInstances = new ArrayList<>();

        for (T t : findInstances(interfaceType)) {
            allInstances.add((Class<T>) t.getClass());
        }

        return Collections.unmodifiableList(allInstances);

    }
}
