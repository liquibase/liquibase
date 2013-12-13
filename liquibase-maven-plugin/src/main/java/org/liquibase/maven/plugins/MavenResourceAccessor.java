package org.liquibase.maven.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

/**
 * Implementation of liquibase.FileOpener for Maven which will use a default or user
 * specified {@link ClassLoader} to load files/resources.
 *
 * @see liquibase.resource.ResourceAccessor
 */
public class MavenResourceAccessor implements ResourceAccessor {

    /**
     * The class loader to use to load files/resources from.
     */
    private ClassLoader _loader;

    /**
     * Creates a {@link liquibase.resource.ResourceAccessor} that uses the classloader for the class.
     */
    public MavenResourceAccessor() {
        this(MavenResourceAccessor.class.getClassLoader());
    }

    /**
     * Creates a {@link liquibase.resource.ResourceAccessor} that will use the specified
     * {@link ClassLoader} to load files.
     *
     * @param cl The {@link ClassLoader} to use to load files/resources.
     */
    public MavenResourceAccessor(ClassLoader cl) {
        _loader = cl;
    }

    @Override
    public InputStream getResourceAsStream(String file) throws IOException {
        file = file.replaceFirst("^target/classes/","");

        return _loader.getResourceAsStream(file);
    }

    @Override
    public Enumeration<URL> getResources(String packageName) throws IOException {
        try {
            URL fileUrl = _loader.getResource(packageName);
            if (fileUrl != null) {
                File file = new File(fileUrl.toURI());
                if (file.exists() && ! file.isDirectory()) {
                    return new Vector<URL>(Arrays.asList(fileUrl)).elements();
                }
            }
        } catch (Throwable e) {
            //not local file, continue on
        }

        packageName = packageName.replaceFirst("^target/classes/","");

        return _loader.getResources(packageName);
    }

    @Override
    public ClassLoader toClassLoader() {
        return _loader;
    }

    @Override
    public String toString() {
        String description;
        if (_loader instanceof URLClassLoader) {
            List<String> urls = new ArrayList<String>();
            for (URL url : ((URLClassLoader) _loader ).getURLs()) {
                urls.add(url.toExternalForm());
            }
            description = StringUtils.join(urls, ",");
        } else {
            description = _loader .getClass().getName();
        }
        return getClass().getName()+"("+ description +")";
    }
}
