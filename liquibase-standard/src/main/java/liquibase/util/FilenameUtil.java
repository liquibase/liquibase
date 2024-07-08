/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package liquibase.util;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * General filename and filepath manipulation utilities.
 */
public class FilenameUtil {

    /**
     * Normalizes a path, removing double and single dot path steps as well as standardizing on "/" for a slash.
     * <p>
     */
    public static String normalize(String filename) {
        if (filename == null) {
            return null;
        }
        int size = filename.length();
        if (size == 0) {
            return filename;
        }
        filename = filename.replaceFirst("^//", "/");

        try {
            String returnPath = Paths.get(filename).normalize().toString();
            returnPath = returnPath.replace('\\', '/');
            returnPath = returnPath.replaceAll("/\\./", "/");
            returnPath = returnPath.replaceAll("//", "/");

            return returnPath;
        } catch (InvalidPathException e) {
            return null;
        }
    }

    /**
     * Concatenates a filename to a base path using normal command line style rules. This method uses the operating
     * system rules to determine the path separator.
     * <p>
     * The returned path will be {@link #normalize(String)}'ed
     */
    public static String concat(String basePath, String fullFilenameToAdd) {
        if (basePath == null) {
            return normalize(fullFilenameToAdd);
        }

        return normalize(Paths.get(basePath, fullFilenameToAdd).toString());
    }

    /**
     * If the path is a file, return everything up to the file. If the path is a directory, return the directory.
     * <p>
     * The returned path will be {@link #normalize(String)}'ed
     */
    public static String getDirectory(String filename) {
        if (filename == null) {
            return null;
        }

        if (filename.endsWith("/") || filename.endsWith("\\")) {
            return normalize(filename);
        }

        final Path path = Paths.get(filename);
        final Path fileName = path.getFileName();
        if (fileName.toString().contains(".")) {
            //probably a file
            final Path parent = path.getParent();
            if (parent == null) {
                return "";
            }

            return normalize(parent.toString());
        } else {
            //probably a directory
            return normalize(filename);
        }
    }


    /**
     * Remove problematic characters from filename and replace them with '_'
     * @see <a href="https://stackoverflow.com/questions/1976007/what-characters-are-forbidden-in-windows-and-linux-directory-names">
     *     What characters are forbidden in Windows and Linux directory names?</a>
     * <p>
     * ' ' '/' ':' '"' '\' '|' '*' '?' '<' '>'
     *
     * @param fileName Filename to remove characters from
     * @return String       Sanitized file name
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }

        fileName = fileName.replaceAll(" ", "_")
                .replaceAll("/", "_")
                .replaceAll(":", "_")
                .replaceAll("\"", "_")
                .replaceAll("\\|", "_")
                .replaceAll("\\*", "_")
                .replaceAll("\\?", "_")
                .replaceAll("<", "_")
                .replaceAll(">", "_")
                .replaceAll("@", "_");

        boolean done = false;
        while (!done) {
            String replacedString = fileName.replace('\\', '_');
            done = (fileName.equals(replacedString));
            if (!done) {
                fileName = replacedString;
                break;
            }
        }
        return fileName;
    }
}
