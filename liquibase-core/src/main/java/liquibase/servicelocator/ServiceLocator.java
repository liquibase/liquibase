package liquibase.servicelocator;

import liquibase.Scope;
import liquibase.exception.ServiceNotFoundException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.Manifest;

public class ServiceLocator {


    protected ServiceLocator(Scope scope) {
    }

    public <T> Collection<T> findAllServices(Class<T> requiredInterface) throws ServiceNotFoundException {
        Iterator<T> iterator = ServiceLoader.load(requiredInterface).iterator();
        List<T> returnList = new ArrayList<>();
        while (iterator.hasNext()) {
            returnList.add(iterator.next());
        }
        return Collections.unmodifiableCollection(returnList);
    }

}
