package liquibase.integration.commandline;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;
import liquibase.resource.Resource;

import java.io.IOException;
import java.util.*;

/**
 * Extension of {@link liquibase.resource.ClassLoaderResourceAccessor} that adds extra fuzzy searching logic based on
 * what users may enter that is different than what is exactly correct.
 */
public class CommandLineResourceAccessor extends ClassLoaderResourceAccessor {

    public CommandLineResourceAccessor(ClassLoader loader) {
        super(loader);
    }

    @java.lang.SuppressWarnings("squid:S2095")
    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        InputStreamList resourcesAsStream = super.openStreams(relativeTo, streamPath);
        if (resourcesAsStream != null) {
            return resourcesAsStream;
        }
        for (String altPath : getAlternatePaths(streamPath)) {
            InputStreamList altResourcesAsStream = super.openStreams(relativeTo, altPath);
            if (altResourcesAsStream != null) {
                return altResourcesAsStream;
            }
        }
        return null;
    }


    @Override
    public List<Resource> find(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        List<Resource> contents = new ArrayList<>();
        List<Resource> superList = super.find(relativeTo, path, includeFiles, includeDirectories, recursive);
        if (superList != null) {
            contents.addAll(superList);
        }
        for (String altPath : getAlternatePaths(path)) {
            contents.addAll(super.find(relativeTo, altPath, includeFiles, includeDirectories, recursive));
        }
        if (contents.isEmpty()) {
            return new ArrayList<>();
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
