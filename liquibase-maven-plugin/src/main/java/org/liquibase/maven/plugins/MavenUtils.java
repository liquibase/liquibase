// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * A Utilities class for Maven plugins.
 *
 * @author Peter Murray
 */
public class MavenUtils {

    public static final String LOG_SEPARATOR =
            "------------------------------------------------------------------------";

    /**
     * Obtains a {@link ClassLoader} that can load from the Maven project dependencies. If
     * the dependencies have not been resolved (or there are none) then this will just end up
     * delegating to the parent {@link ClassLoader} of this class.
     *
     * @return The ClassLoader that can load the resolved dependencies for the Maven
     * project.
     * @throws java.net.MalformedURLException If any of the dependencies cannot be resolved
     *                                        into a URL.
     */
    public static ClassLoader getArtifactClassloader(MavenProject project,
                                                     boolean includeArtifact,
                                                     boolean includeTestOutputDirectory,
                                                     Class clazz,
                                                     Log log,
                                                     boolean verbose)
            throws MalformedURLException {
        if (verbose) {
            log.info("Loading artifacts into URLClassLoader");
        }
        Set<URI> uris = new HashSet<>();
        // Find project dependencies, including the transitive ones.
        Set<Artifact> dependencies = project.getArtifacts();
        if ((dependencies != null) && !dependencies.isEmpty()) {
            for (Artifact dependency : dependencies) {
                addArtifact(uris, dependency, log, verbose);
            }
        } else {
            log.info("there are no resolved artifacts for the Maven project.");
        }

        // Include the artifact for the actual maven project if requested
        if (includeArtifact) {
            // If the actual artifact can be resolved, then use that, otherwise use the build
            // directory as that should contain the files for this project that we will need to
            // run against. It is possible that the build directly could be empty, but we cannot
            // directly include the source and resources as the resources may require filtering
            // to replace any placeholders in the resource files.
            Artifact a = project.getArtifact();
            if (a.getFile() != null) {
                addArtifact(uris, a, log, verbose);
            } else {
                addFile(uris, new File(project.getBuild().getOutputDirectory()), log, verbose);
            }
        }
        if (includeTestOutputDirectory) {
            addFile(uris, new File(project.getBuild().getTestOutputDirectory()), log, verbose);
        }
        if (verbose) {
            log.info(LOG_SEPARATOR);
        }

        List<URI> uriList = new ArrayList<>(uris);
        URL[] urlArray = new URL[uris.size()];
        for (int i = 0; i < uris.size(); i++) {
            urlArray[i] = uriList.get(i).toURL();
        }
        return new URLClassLoader(urlArray, clazz.getClassLoader());
    }

    /**
     * Adds the artifact file into the set of URLs so it can be used in a URLClassLoader.
     *
     * @param urls     The set to add the artifact file URL to.
     * @param artifact The Artifact to resolve the file for.
     * @throws MalformedURLException If there is a problem creating the URL for the file.
     */
    private static void addArtifact(Set<URI> urls,
                                    Artifact artifact,
                                    Log log,
                                    boolean verbose)
            throws MalformedURLException {
        File f = artifact.getFile();
        if (f == null) {
            log.warn("Artifact with no actual file, '" + artifact.getGroupId()
                    + ":" + artifact.getArtifactId() + "'");
        } else {
            addFile(urls, f, log, verbose);
        }
    }

    private static void addFile(Set<URI> urls, File f, Log log, boolean verbose)
            throws MalformedURLException {
        if (f != null) {
            URI fileUri = f.toURI();
            if (verbose) {
                log.info("  artifact: " + fileUri);
            }
            urls.add(fileUri);
        }
    }

    /**
     * Recursively searches for the field specified by the fieldName in the class and all
     * the super classes until it either finds it, or runs out of parents.
     *
     * @param clazz           The Class to start searching from.
     * @param keyPropertyName The name of the field to retrieve.
     * @return The {@link Field} identified by the field name.
     * @throws NoSuchFieldException If the field was not found in the class or any of its
     *                              super classes.
     */
    public static Field getDeclaredField(Class<?> clazz, String keyPropertyName)
            throws NoSuchFieldException {
        Field f = getField(clazz, keyPropertyName);
        if (f == null) {
            // Try the parent class
            Class<?> parent = clazz.getSuperclass();
            if (parent != null) {
                return getDeclaredField(parent, keyPropertyName);
            } else {
                throw new NoSuchFieldException("The field '" + keyPropertyName + "' could not be "
                        + "found in the class of any of its parent "
                        + "classes.");
            }
        } else {
            return f;
        }
    }

    private static Field getField(Class<?> clazz, String fieldName) {
        Field[] fields = clazz.getDeclaredFields();
        int i = 0;
        while (i < fields.length) {
            Field field = fields[i];
            PropertyElement annotation = field.getAnnotation(PropertyElement.class);
            if (annotation != null) {
                if (annotation.key().equalsIgnoreCase(fieldName)) {
                    return field;
                }
            }
            if (fields[i].getName().equalsIgnoreCase(fieldName)) {
                return fields[i];
            }
            i++;
        }
        return null;
    }
}
