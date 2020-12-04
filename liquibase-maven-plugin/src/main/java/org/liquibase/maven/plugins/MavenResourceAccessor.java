package org.liquibase.maven.plugins;

import liquibase.resource.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension of {@link liquibase.resource.ClassLoaderResourceAccessor} for Maven which will use a default or user specified {@link ClassLoader} to load files/resources.
 */
public class MavenResourceAccessor extends CompositeResourceAccessor {

    private final MavenProject project;

    public MavenResourceAccessor(ClassLoader mavenClassloader, MavenProject project, String changeLogDirectory) {
        this.project = project;
        if (changeLogDirectory != null) {
            this.addResourceAccessor(new FileSystemResourceAccessor(new File(calculateChangeLogDirectoryAbsolutePath(changeLogDirectory, project))));
        }
        this.addResourceAccessor(new FileSystemResourceAccessor(project.getBasedir()));
        this.addResourceAccessor(new ClassLoaderResourceAccessor(mavenClassloader));
        this.addResourceAccessor(new ClassLoaderResourceAccessor(getClass().getClassLoader()));
    }

    @Override
    public OutputStream openOutputStream(String relativeTo, String path, boolean append) throws IOException {
        return new FileOutputStream(new File("src/main/resources", path), append);
    }

    private String calculateChangeLogDirectoryAbsolutePath(String changeLogDirectory, MavenProject project) {
        if (changeLogDirectory != null) {
            // convert to standard / if using absolute path on windows
            changeLogDirectory = changeLogDirectory.trim().replace('\\', '/');
            // try to know if it's an absolute or relative path : the absolute path case is simpler and don't need more actions
            File changeLogDirectoryFile = new File(changeLogDirectory);
            if (!changeLogDirectoryFile.isAbsolute()) {
                // we are in the relative path case
                changeLogDirectory = project.getBasedir().getAbsolutePath().replace('\\', '/') + "/" + changeLogDirectory;
            }
        }

        return changeLogDirectory;
    }

}
