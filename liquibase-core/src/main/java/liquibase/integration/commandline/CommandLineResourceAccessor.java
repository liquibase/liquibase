package liquibase.integration.commandline;

import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extension of {@link liquibase.resource.ClassLoaderResourceAccessor} that adds extra fuzzy searching logic based on
 * what users may enter that is different than what is exactly correct.
 */
public class CommandLineResourceAccessor extends ClassLoaderResourceAccessor {

    public CommandLineResourceAccessor(ClassLoader loader) {
        super(loader);
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        Set<InputStream> resourcesAsStream = super.getResourcesAsStream(path);
        if (resourcesAsStream == null) {
            for (String altPath : getAlternatePaths(path)) {
                resourcesAsStream = super.getResourcesAsStream(altPath);
                if (resourcesAsStream != null) {
                    return resourcesAsStream;
                }
            }
        }
        return resourcesAsStream;
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        Set<String> contents = new HashSet<>();
        Set<String> superList = super.list(relativeTo, path, includeFiles, includeDirectories, recursive);
        if (superList != null) {
            contents.addAll(superList);
        }
        for (String altPath : getAlternatePaths(path)) {
            contents.addAll(super.list(relativeTo, altPath, includeFiles, includeDirectories, recursive));
        }
        if (contents.isEmpty()) {
            return null;
        }
        return contents;
    }

    /**
     * Return alternate options for the given path that the user maybe meant. Return in order of likelihood.
     */
    protected List<String> getAlternatePaths(String path) {
        List<String> alternatePaths = new ArrayList<>();

        if (path.startsWith("/")) { //People are often confused about leading slashes in resource paths...
            alternatePaths.add(path.substring(1));
        }

        return alternatePaths;

    }

}
