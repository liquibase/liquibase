package org.liquibase.maven.plugins;

import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Extension of {@link liquibase.resource.ClassLoaderResourceAccessor} for Maven which will use a default or user specified {@link ClassLoader} to load files/resources.
 */
public class MavenResourceAccessor extends ClassLoaderResourceAccessor {

    public MavenResourceAccessor() {
        this(MavenResourceAccessor.class.getClassLoader());
    }

    public MavenResourceAccessor(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        return super.getResourcesAsStream(path.replaceFirst("^target/classes/", ""));
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        Set<String> contents = super.list(relativeTo, path, includeFiles, includeDirectories, recursive);
        if ((contents == null) || contents.isEmpty()) {
            contents = super.list(relativeTo, path.replaceFirst("^target/classes/", ""), includeFiles, includeDirectories, recursive);
        }
        return contents;

    }
}
