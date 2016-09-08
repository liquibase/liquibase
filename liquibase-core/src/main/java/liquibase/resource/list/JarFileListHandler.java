package liquibase.resource.list;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import liquibase.util.Validate;

public class JarFileListHandler implements ListHandler {

    private final File jarFile;

    public JarFileListHandler(String jarFile) {
        this(new File(jarFile.replace("%20"," ")));
    }

    public JarFileListHandler(File jarFile) {
        this.jarFile = Validate.notNullArgument(jarFile, "Can't list null jar file.");
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        Set<String> result = new HashSet<String>();
        JarFile zipFile = new JarFile(jarFile, false);
        try {
            Enumeration<JarEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(path)) {
                    continue;
                }
                if (!recursive) {
                    String pathAsDir = path.endsWith(ListHandler.RESOURCE_PATH_SEPERATOR) ? path : path + ListHandler.RESOURCE_PATH_SEPERATOR;
                    if (!entry.getName().startsWith(pathAsDir) || entry.getName().substring(pathAsDir.length()).contains(ListHandler.RESOURCE_PATH_SEPERATOR)) {
                        continue;
                    }
                }
                if (entry.isDirectory() && includeDirectories) {
                    result.add(standardizeName(entry.getName()));
                } else if (includeFiles) {
                    result.add(standardizeName(entry.getName()));
                }
            }
            return result;
        } finally {
            zipFile.close();
        }
    }

    protected String standardizeName(String name) {
        return name.replace("\\", RESOURCE_PATH_SEPERATOR);
    }

    @Override
    public String toString() {
        return getClass().getName() + "(" + jarFile + ")";
    }
}
