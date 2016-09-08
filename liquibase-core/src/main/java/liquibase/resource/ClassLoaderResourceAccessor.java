package liquibase.resource;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.list.ListHandlerBuilder;
import liquibase.resource.list.DefaultListHandlerBuilder;
import liquibase.resource.list.ListHandler;
import liquibase.util.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of {@link liquibase.resource.ResourceAccessor} that wraps a class loader.
 * The resource listing only works after an resources is accessed first, with on exception that if an URLClassLoader is used and url are listable.
 */
public class ClassLoaderResourceAccessor extends AbstractResourceAccessor {

    private final ListHandlerBuilder listHandlerBuilder;
    private Map<String,ListHandler> listHandlers = new HashMap<String,ListHandler>();
    private static final String JAR_SCAN_RESOURCE = "META-INF/MANIFEST.MF";

    public ClassLoaderResourceAccessor() {
        this(ClassLoaderResourceAccessor.class.getClassLoader());
    }

    public ClassLoaderResourceAccessor(ClassLoader classLoader) {
        this(classLoader,new DefaultListHandlerBuilder());
    }

    public ClassLoaderResourceAccessor(ClassLoader classLoader,ListHandlerBuilder listHandlerBuilder) {
        super(classLoader);
        this.listHandlerBuilder = Validate.notNullArgument(listHandlerBuilder,"Can list with null listHandlerBuilder");
        init();
    }

    protected void init() {
        if (classLoader instanceof URLClassLoader) {
            for (URL url:URLClassLoader.class.cast(classLoader).getURLs()) {
                String urlPath = url.toExternalForm();
                addListHandler(urlPath,listHandlerBuilder.buildListHandler(urlPath));
            }
        }

        try {
            Enumeration<URL> baseUrls = classLoader.getResources("");

            while (baseUrls.hasMoreElements()) {
                String urlPath = baseUrls.nextElement().toExternalForm();
                addListHandler(urlPath,listHandlerBuilder.buildListHandler(urlPath));
            }
        } catch (IOException ignore) {
            //cannot get default resource list
        }
        scanJars();
    }

    protected void scanJars() {
        try {
            Enumeration<URL> resources = classLoader.getResources(JAR_SCAN_RESOURCE);
            while (resources.hasMoreElements()) {
                addListHandlerFromResourceURL(resources.nextElement(), JAR_SCAN_RESOURCE);
            }
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected boolean addListHandlerFromResourceURL(URL url,String path) {
        String urlPath = listHandlerBuilder.buildURLPath(url,path);
        if (listHandlers.containsKey(urlPath)) {
            return false; // quick return
        }
        return addListHandler(urlPath,listHandlerBuilder.buildListHandler(urlPath));
    }

    protected boolean addListHandler(String urlPath,ListHandler listHandler) {
        if (listHandler == null) {
            return false;
        }
        if (listHandlers.containsKey(Validate.notNullArgument(urlPath, "Need none null urlPath for keying"))) {
            return false;
        }
        logger.debug("Adding list handler: "+listHandler);
        listHandlers.put(urlPath,listHandler);
        return true;
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        for (ListHandler handler:listHandlers.values()) {
            Set<String> result = handler.list(relativeTo, path, includeFiles, includeDirectories, recursive);
            if (result != null && !result.isEmpty()) {
                return result;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return getClass().getName()+"("+ classLoader.getClass().getName() +")";
    }

    public static ClassLoader createURLClassLoader(File...folders) {
        try {
            URL[] classLoaderSources = new URL[folders.length];
            for (int i=0;i<folders.length;i++) {
                classLoaderSources[i] = folders[i].toURI().toURL();
            }
            return new URLClassLoader(classLoaderSources);
        } catch (MalformedURLException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
