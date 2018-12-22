package org.liquibase.maven.plugins;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
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
    public InputStreamList openStreams(String path) throws IOException {
        return super.openStreams(path.replaceFirst("^target/classes/", ""));
    }

    @Override
    public SortedSet<String> list(String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        SortedSet<String> contents = super.list(path, includeFiles, includeDirectories, recursive);
        if ((contents == null) || contents.isEmpty()) {
            contents = super.list(path.replaceFirst("^target/classes/", ""), includeFiles, includeDirectories, recursive);
        }
        return contents;

    }
}
