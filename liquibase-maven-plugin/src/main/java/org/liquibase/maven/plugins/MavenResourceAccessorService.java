package org.liquibase.maven.plugins;

import org.apache.maven.project.MavenProject;
import liquibase.resource.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MavenResourceAccessorService implements ResourceAccessorService {
    private final MavenProject project;
    private String changeLogDirectory;

    public MavenResourceAccessorService(MavenProject project, String changeLogDirectory) {
        this.project = project;
        this.changeLogDirectory = changeLogDirectory;
    }

    @Override
    public ResourceAccessor getResourceAccessor(ClassLoader classLoader) {
        List<ResourceAccessor> resourceAccessors = new ArrayList<ResourceAccessor>();
        resourceAccessors.add(new MavenResourceAccessor(classLoader));
        resourceAccessors.add(new FileSystemResourceAccessor(project.getBasedir()));
        resourceAccessors.add(new ClassLoaderResourceAccessor(getClass().getClassLoader()));

        if (changeLogDirectory != null) {
            calculateChangeLogDirectoryAbsolutePath();
            resourceAccessors.add(new FileSystemResourceAccessor(new File(changeLogDirectory)));
        }

        return new CompositeResourceAccessor(resourceAccessors);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    private void calculateChangeLogDirectoryAbsolutePath() {
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
    }
}
