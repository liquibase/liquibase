// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import liquibase.Scope;
import liquibase.exception.LiquibaseException;
import liquibase.license.*;
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
