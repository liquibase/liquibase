package org.liquibase.maven.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import liquibase.resource.FileOpener;

/**
 * Implementation of liquibase.FileOpener for Maven which will use a default or user
 * specified {@link ClassLoader} to load files/resources.
 *
 * @see liquibase.resource.FileOpener
 */
public class MavenFileOpener implements FileOpener {

    /**
     * The class loader to use to load files/resources from.
     */
    private ClassLoader _loader;

    /**
     * Creates a {@link liquibase.resource.FileOpener} that uses the classloader for the class.
     */
    public MavenFileOpener() {
        this(MavenFileOpener.class.getClassLoader());
    }

    /**
     * Creates a {@link liquibase.resource.FileOpener} that will use the specified
     * {@link ClassLoader} to load files.
     *
     * @param cl The {@link ClassLoader} to use to load files/resources.
     */
    public MavenFileOpener(ClassLoader cl) {
        _loader = cl;
    }

    public InputStream getResourceAsStream(String file) throws IOException {
        return _loader.getResourceAsStream(file);
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return _loader.getResources(packageName);
    }

    public ClassLoader toClassLoader() {
        return _loader;
    }
}
