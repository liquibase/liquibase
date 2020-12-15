package org.liquibase.maven.plugins;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Extension of {@link liquibase.resource.ClassLoaderResourceAccessor} for Maven which will use a default or user specified {@link ClassLoader} to load files/resources.
 */
public class MavenResourceAccessor extends CompositeResourceAccessor {

    public MavenResourceAccessor(ClassLoader mavenClassloader, MavenProject project, String changeLogDirectory) {
        if (changeLogDirectory != null) {
            this.addResourceAccessor(new FileSystemResourceAccessor(new File(calculateChangeLogDirectoryAbsolutePath(changeLogDirectory, project))));
        }
        this.addResourceAccessor(new FileSystemResourceAccessor(project.getBasedir()));
        this.addResourceAccessor(new ClassLoaderResourceAccessor(mavenClassloader));
        this.addResourceAccessor(new ClassLoaderResourceAccessor(getClass().getClassLoader()));
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
