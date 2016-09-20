package liquibase.resource;

import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A {@link liquibase.resource.ResourceAccessor} that will search in a list of other ResourceAccessors until it finds
 * one that has a resource of the appropriate name and path.
 */
public class CompositeResourceAccessor implements ResourceAccessor {

    private List<ResourceAccessor> resourceAccessors;

    public CompositeResourceAccessor(List<ResourceAccessor> resourceAccessors) {
        this.resourceAccessors = resourceAccessors;
    }

    public CompositeResourceAccessor(ResourceAccessor... resourceAccessors) {
        this.resourceAccessors = Arrays.asList(resourceAccessors);
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        for (ResourceAccessor accessor : resourceAccessors) {
            Set<InputStream> returnSet = accessor.getResourcesAsStream(path);
            if (returnSet != null && returnSet.size() > 0) {
                return returnSet;
            }
        }
        return null;
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        Set<String> returnSet = new HashSet<String>();
        for (ResourceAccessor accessor : resourceAccessors) {
            Set<String> thisSet = accessor.list(relativeTo, path, includeFiles, includeDirectories, recursive);
            if (thisSet != null) {
                returnSet.addAll(thisSet);
            }
        }

        if (returnSet.size() > 0) {
            return returnSet;
        }
        return null;
    }

    @Override
    public ClassLoader toClassLoader() {
        ClassLoader[] loaders=new ClassLoader[resourceAccessors.size()];
        int i=0;
        for (ResourceAccessor fo: resourceAccessors) {
           loaders[i++]=fo.toClassLoader();
        }

        return new CompositeClassLoader(loaders);
    }

    //based on code from http://fisheye.codehaus.org/browse/xstream/trunk/xstream/src/java/com/thoughtworks/xstream/core/util/CompositeClassLoader.java?r=root
    private static class CompositeClassLoader extends ClassLoader {

        private final List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();

        public CompositeClassLoader(ClassLoader... classLoaders) {
            this.classLoaders.addAll(Arrays.asList(classLoaders));
        }

        @Override
        public Class loadClass(String name,boolean resolve) throws ClassNotFoundException {
            for (Object classLoader1 : classLoaders) {
                ClassLoader classLoader = (ClassLoader) classLoader1;
                try {
                    Class classe=classLoader.loadClass(name);
                    if(resolve)
                        resolveClass(classe);
                    return classe;
                } catch (ClassNotFoundException notFound) {
                    // ok.. try another one
                }
            }

            // One last try - the context class loader associated with the current thread. Often used in j2ee servers.
            // Note: The contextClassLoader cannot be added to the classLoaders list up front as the thread that constructs
            // liquibase is potentially different to thread that uses it.
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                Class classe=contextClassLoader.loadClass(name);
                if(resolve)
                    resolveClass(classe);
                return classe;
            } else {
                throw new ClassNotFoundException(name);
            }


        }

        

 	}

    @Override
    public String toString() {
        List<String> openerStrings = new ArrayList<String>();
        for (ResourceAccessor opener : resourceAccessors) {
            openerStrings.add(opener.toString());
        }
        return getClass().getName()+"("+StringUtils.join(openerStrings,",")+")";
    }
}
