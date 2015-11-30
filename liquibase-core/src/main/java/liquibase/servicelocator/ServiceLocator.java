package liquibase.servicelocator;

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

    private static ServiceLocator instance;

    private ResourceAccessor resourceAccessor;

    protected ServiceLocator() {
        setResourceAccessor(new ClassLoaderResourceAccessor());
    }

    protected ServiceLocator(ResourceAccessor accessor) {
        setResourceAccessor(accessor);
    }

    public static ServiceLocator getInstance() {
        return instance;
    }

    public static void setInstance(ServiceLocator newInstance) {
        instance = newInstance;
    }

    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    public <T> Iterator<T> findAllServices(Class<T> requiredInterface) throws ServiceNotFoundException {
        return ServiceLoader.load(requiredInterface).iterator();
    }

    public static void reset() {
        instance = new ServiceLocator();
    }
}
