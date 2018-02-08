package liquibase.servicelocator;

import liquibase.exception.ServiceNotFoundException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.osgi.OSGiPackageScanClassResolver;
import liquibase.osgi.OSGiResourceAccessor;
import liquibase.osgi.OSGiUtil;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.util.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.Manifest;

/**
 * Entry point to the Liquibase specific ServiceLocator framework.
 * <p>
 * Services (concrete instances of interfaces) are located by scanning nominated
 * packages on the classpath for implementations of the interface.
 */
public interface ServiceLocator {

    Object newInstance(Class requiredInterface) throws ServiceNotFoundException;


    Class findClass(Class requiredInterface) throws ServiceNotFoundException;

    <T> Class<? extends T>[] findClasses(Class<T> requiredInterface) throws ServiceNotFoundException;
}
