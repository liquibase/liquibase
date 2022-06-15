package liquibase.integration.spring;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.InputStreamList;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.SortedSet;
import java.util.TreeSet;

public class SpringResourceAccessor extends AbstractResourceAccessor {

    private final ResourceLoader resourceLoader;
    private final DefaultResourceLoader fallbackResourceLoader;

    public SpringResourceAccessor(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        if (resourceLoader == null) {
            this.fallbackResourceLoader = new DefaultResourceLoader(Thread.currentThread().getContextClassLoader());
        } else {
            this.fallbackResourceLoader = new DefaultResourceLoader(resourceLoader.getClassLoader());
        }
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        String searchPath = getCompletePath(relativeTo, path);

        if (recursive) {
            searchPath += "/**";
        } else {
            searchPath += "/*";
        }
        searchPath = finalizeSearchPath(searchPath);

        final Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(searchPath);
        SortedSet<String> returnSet = new TreeSet<>();
        for (Resource resource : resources) {
            final boolean isFile = resourceIsFile(resource);
            if (isFile && includeFiles) {
                returnSet.add(getResourcePath(resource));
            }
            if (!isFile && includeDirectories) {
                returnSet.add(getResourcePath(resource));
            }
        }

        return returnSet;
    }

    @Override
    public SortedSet<String> describeLocations() {
        final TreeSet<String> returnSet = new TreeSet<>();

        final ClassLoader classLoader = resourceLoader.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                returnSet.add(url.toExternalForm());
            }
        } else {
            returnSet.add("Spring resources");
        }

        return returnSet;
    }

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        String searchPath = getCompletePath(relativeTo, streamPath);
        searchPath = finalizeSearchPath(searchPath);

        final Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(searchPath);

        InputStreamList returnList = new InputStreamList();
        for (Resource foundResource : resources) {
            try {
                returnList.add(foundResource.getURI(), foundResource.getInputStream());
            } catch (FileNotFoundException ignored) {
                //don't add it to the return list
            }
        }

        return returnList;
    }

    /**
     * Returns the lookup path to the given resource.
     */
    protected String getResourcePath(Resource resource) {
        if (resource instanceof ContextResource) {
            return ((ContextResource) resource).getPathWithinContext();
        }
        if (resource instanceof ClassPathResource) {
            return ((ClassPathResource) resource).getPath();
        }

        //have to fall back to figuring out the path as best we can
        try {
            String url = resource.getURL().toExternalForm();
            if (url.contains("!")) {
                return url.replaceFirst(".*!", "");
            } else {
                while (!getResource("classpath:" + url).exists()) {
                    String newUrl = url.replaceFirst("^/?.*?/", "");
                    if (newUrl.equals(url)) {
                        throw new UnexpectedLiquibaseException("Could determine path for " + resource.getURL().toExternalForm());
                    }
                    url = newUrl;
                }

                return url;
            }
        } catch (IOException e) {
            //the path gets stored in the databasechangelog table, so if it gets returned incorrectly it will cause future problems.
            //so throw a breaking error now rather than wait for bigger problems down the line
            throw new UnexpectedLiquibaseException("Cannot determine resource path for " + resource.getDescription());
        }
    }

    /**
     * Returns the complete path to the resource, taking the relative path into account
     */
    protected String getCompletePath(String relativeTo, String path) throws IOException {
        path = path.replace("\\", "/");

        String searchPath;
        if (relativeTo == null) {
            searchPath = path;
        } else {
            relativeTo = relativeTo.replace("\\", "/");

            boolean relativeIsFile;
            Resource rootResource = getResource(relativeTo);
            relativeIsFile = resourceIsFile(rootResource);

            if (relativeIsFile) {
                searchPath = relativeTo.replaceFirst("/[^/]+$", "") + "/" + path;
            } else {
                searchPath = relativeTo + "/" + path;
            }
        }
        return searchPath;
    }

    /**
     * Looks up the given resource.
     */
    protected Resource getResource(String resourcePath) {
        //some ResourceLoaders (FilteredReactiveWebContextResource) lie about whether they exist or not which can confuse the rest of the code.
        //check the "fallback" loader first, and if that can't find it use the "real" one.
        // The fallback one should be more reasonable in it's `exists()` function
        Resource defaultLoaderResource = fallbackResourceLoader.getResource(resourcePath);
        if (defaultLoaderResource.exists()) {
            return defaultLoaderResource;
        }

        return resourceLoader.getResource(resourcePath);
    }

    /**
     * Return true if the given resource is a standard file. Return false if it is a directory.
     */
    protected boolean resourceIsFile(Resource resource) throws IOException {
        if (resource.exists() && resource.isFile()) {
            //we can know for sure
            return resource.getFile().isFile();
        } else {
            //we have to guess
            final String filename = resource.getFilename();
            return filename != null && filename.contains(".");
        }
    }

    /**
     * Ensure the given searchPath is a valid searchPath.
     * Default implementation adds "classpath:" and removes duplicated /'s and classpath:'s
     */
    protected String finalizeSearchPath(String searchPath) {
        if(searchPath.matches("^classpath\\*?:.*")) {
            searchPath = searchPath.replace("classpath:","").replace("classpath*:","");
            searchPath = "classpath*:/" +searchPath;
        } else if(!searchPath.matches("^\\w+:.*")) {
            searchPath = "classpath*:/" +searchPath;
        }
        searchPath = searchPath.replace("\\", "/");
        searchPath = searchPath.replaceAll("//+", "/");

        return searchPath;
    }

}
