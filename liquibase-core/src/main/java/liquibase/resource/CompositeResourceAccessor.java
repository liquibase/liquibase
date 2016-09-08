package liquibase.resource;

import liquibase.util.StringUtils;
import liquibase.util.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A {@link liquibase.resource.ResourceAccessor} that will search in a list of other ResourceAccessors until it finds
 * one that has a resource of the appropriate name and path.
 */
public class CompositeResourceAccessor implements ResourceAccessor {

    private final CompositeClassLoader classLoader;
    private final List<ResourceAccessor> resourceAccessors;

    public CompositeResourceAccessor(ResourceAccessor... resourceAccessors) {
        this(Arrays.asList(resourceAccessors));
    }

    public CompositeResourceAccessor(List<ResourceAccessor> resourceAccessors) {
        this.resourceAccessors = Validate.notNullArgument(resourceAccessors, "Can create composite with null value");
        this.classLoader = new CompositeClassLoader();
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        for (ResourceAccessor accessor : resourceAccessors) {
            Set<InputStream> returnSet = accessor.getResourcesAsStream(path);
            if (returnSet != null && !returnSet.isEmpty()) {
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
        if (!returnSet.isEmpty()) {
            return returnSet;
        }
        return null;
    }

    @Override
    public ClassLoader toClassLoader() {
        return classLoader;
    }

    //based on code from http://fisheye.codehaus.org/browse/xstream/trunk/xstream/src/java/com/thoughtworks/xstream/core/util/CompositeClassLoader.java?r=root
    private class CompositeClassLoader extends ClassLoader {

        @Override
        public Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
            for (ResourceAccessor resourceAccessor : resourceAccessors) {
                ClassLoader classLoader = resourceAccessor.toClassLoader();
                try {
                    Class<?> classe=classLoader.loadClass(name);
                    if(resolve) {
                        resolveClass(classe);
                    }
                    return classe;
                } catch (ClassNotFoundException notFound) {
                    // ok.. try another one
                }
            }
            throw new ClassNotFoundException(name);
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
