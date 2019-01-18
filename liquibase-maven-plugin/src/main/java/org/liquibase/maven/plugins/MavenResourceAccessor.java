package org.liquibase.maven.plugins;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;

import java.io.IOException;
import java.util.SortedSet;

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
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        return super.openStreams(relativeTo, streamPath.replaceFirst("^target/classes/", ""));
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        SortedSet<String> contents = super.list(relativeTo, path, includeFiles, includeDirectories, recursive);
        if ((contents == null) || contents.isEmpty()) {
            contents = super.list(relativeTo, path.replaceFirst("^target/classes/", ""), includeFiles, includeDirectories, recursive);
        }
        return contents;

    }
}
