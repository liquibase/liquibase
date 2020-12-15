package org.liquibase.maven.plugins;

import liquibase.resource.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

/**
 * Extension of {@link ResourceWriter} for Maven which will write to the src/main/resources directory.
 */
public class MavenResourceWriter extends FileSystemResouceWriter {

    public MavenResourceWriter(MavenProject project) throws IOException {
        this(project, "src/main/resources");
    }

    public MavenResourceWriter(MavenProject project, String rootPath) throws IOException {
        super(new File(project.getBasedir(), rootPath).toPath());
    }

}
