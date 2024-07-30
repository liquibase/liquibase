package liquibase.integration.spring;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.AbstractResourceAccessor;
import org.springframework.core.io.*;

import static java.net.URLDecoder.*;

import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.UnsupportedOperationException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public void close() throws Exception {

    }

    @Override
    public List<liquibase.resource.Resource> getAll(String path) throws IOException {
        path = finalizeSearchPath(path);
        final Resource[] springResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(path);
        List<liquibase.resource.Resource> returnList = new ArrayList<>();
        for (Resource resource : springResources) {
            if (resource.exists()) {
                returnList.add(new SpringResource(path, resource.getURI(), resource));
            }
        }

        if (returnList.isEmpty()) {
            return null;
        }
        return returnList;
    }

    @Override
    public List<liquibase.resource.Resource> search(String searchPath, boolean recursive) throws IOException {
        if (recursive) {
            searchPath += "/**";
        } else {
            searchPath += "/*";
        }
        searchPath = finalizeSearchPath(searchPath);

        final Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(searchPath);
        List<liquibase.resource.Resource> returnList = new ArrayList<>();
        for (Resource resource : resources) {
            final boolean isFile = resourceIsFile(resource);
            if (isFile) {
                returnList.add(new SpringResource(getResourcePath(resource), resource.getURI(), resource));
            }
        }

        return returnList;
    }

    @Override
    public List<String> describeLocations() {
        return Collections.singletonList("Spring classpath");
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

            url = decodeUrl(resource, url);

            if (url.contains("!")) {
                return url.replaceFirst(".*!", "");
            } else {
                while (!getResource("classpath:" + url).exists()) {
                    String newUrl = url.replaceFirst("^/?.*?/", "");
                    if (newUrl.equals(url)) {
                        throw new UnexpectedLiquibaseException("Could not determine path for " + resource.getURL().toExternalForm());
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
                String relativePath = relativeTo.replaceFirst("[^/]+$", "");
                searchPath = relativePath + path;
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
            try {
                //we can know for sure
                return resource.getFile().isFile();
            } catch (UnsupportedOperationException e) {
                //native image throws on getFile
                return true;
            }
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
        if (searchPath.matches("^classpath\\*?:.*")) {
            searchPath = searchPath.replace("classpath:", "").replace("classpath*:", "");
            searchPath = "classpath*:/" + searchPath;
        } else if (!searchPath.matches("^\\w+:.*")) {
            searchPath = "classpath*:/" + searchPath;
        }
        searchPath = searchPath.replace("\\", "/");
        searchPath = searchPath.replaceAll("//+", "/");

        searchPath = StringUtils.cleanPath(searchPath);

        return searchPath;
    }

    private String decodeUrl(Resource resource, String url) throws IOException {
        try {
            url = decode(resource.getURL().toExternalForm(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Failed to decode path " + url + "; continuing without decoding.", e);
        }
        return url;
    }

}
