package org.liquibase.maven.plugins;

import liquibase.Scope;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.InputStreamList;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import liquibase.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.List;
import java.util.SortedSet;

import static sun.awt.FontConfiguration.verbose;

/**
 * Extension of {@link liquibase.resource.ClassLoaderResourceAccessor} for Maven which will use a default or user specified {@link ClassLoader} to load files/resources.
 */
public class MavenResourceAccessor extends CompositeResourceAccessor {

    public MavenResourceAccessor(MavenProject project) throws DependencyResolutionRequiredException {
        for (String element : project.getCompileClasspathElements()) {
            this.addResourceAccessor(new FileSystemResourceAccessor(new File(element)));
        }

        Set<Artifact> dependencies = project.getArtifacts();
        if (dependencies != null) {
            for (Artifact artifact : dependencies) {
                this.addResourceAccessor(new FileSystemResourceAccessor(artifact.getFile()));
            }
        } else {
            Scope.getCurrentScope().getLog(getClass()).fine("No artifacts for the Maven project to add to the searchPath");
        }

        // If the actual artifact can be resolved, then use that, otherwise use the build
        // directory as that should contain the files for this project that we will need to
        // run against. It is possible that the build directly could be empty, but we cannot
        // directly include the source and resources as the resources may require filtering
        // to replace any placeholders in the resource files.
        File projectArtifactFile = project.getArtifact().getFile();
        if (projectArtifactFile == null) {
            this.addResourceAccessor(new FileSystemResourceAccessor(new File(project.getBuild().getOutputDirectory())));
        } else {
            this.addResourceAccessor(new FileSystemResourceAccessor(projectArtifactFile));
        }

//TODO        if (includeTestOutputDirectory) {
            this.addResourceAccessor(new FileSystemResourceAccessor(new File(project.getBuild().getTestOutputDirectory())));
//        }
    }

}
