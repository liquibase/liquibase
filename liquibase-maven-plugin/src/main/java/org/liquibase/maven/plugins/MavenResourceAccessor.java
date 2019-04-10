package org.liquibase.maven.plugins;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.SortedSet;

/**
 * Extension of {@link liquibase.resource.ClassLoaderResourceAccessor} for Maven which will use a default or user specified {@link ClassLoader} to load files/resources.
 */
public class MavenResourceAccessor extends ClassLoaderResourceAccessor {

    public MavenResourceAccessor(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        return super.openStreams(relativeTo, streamPath);
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        return super.list(relativeTo, path, recursive, includeFiles, includeDirectories);
    }
}
