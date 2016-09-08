package liquibase.resource.list;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import liquibase.util.Validate;

public class FolderListHandler implements ListHandler {

    private final File baseDirectory;

    public FolderListHandler(String folder) {
        this(new File(folder));
    }

    public FolderListHandler(File folder) {
        this.baseDirectory = Validate.notNullArgument(folder, "Can't list null folder.");
        Validate.isTrueArgument(folder.isDirectory(), folder + " must be a directory");
    }

    @Override
    public Set<String> list(String relativeToFile, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        File finalDir;
        String resourceUrlPrefix = path;
        if (relativeToFile == null) {
            finalDir = new File(baseDirectory, path);
        } else {
            File relativeFolder = new File(baseDirectory, relativeToFile).getParentFile();
            finalDir = new File(relativeFolder, path);
            resourceUrlPrefix = standardizeName(new File(relativeToFile).getParentFile() + ListHandler.RESOURCE_PATH_SEPERATOR + path);
        }
        if (!finalDir.exists() || !finalDir.isDirectory()) {
            return null;
        }
        return listFileTree(resourceUrlPrefix, finalDir, recursive, includeFiles, includeDirectories);
    }

    public Set<String> listFileTree(String resultPrefix, File rootFile, boolean recursive, boolean includeFiles, boolean includeDirectories) {
        return listFileTree(resultPrefix, rootFile, rootFile, recursive, includeFiles, includeDirectories, new LinkedHashSet<String>());
    }

    private Set<String> listFileTree(String resultPrefix, File rootFile, File currentFile, boolean recursive, boolean includeFiles, boolean includeDirectories, Set<String> returnSet) {
        File[] files = currentFile.listFiles();
        if (files == null) {
            return returnSet;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                if (includeDirectories) {
                    returnSet.add(standardizeName(buildResourcePath(file, rootFile, true, resultPrefix)));
                }
                if (recursive) {
                    listFileTree(resultPrefix, rootFile, file, recursive, includeFiles, includeDirectories, returnSet);
                }
            } else {
                if (includeFiles) {
                    returnSet.add(standardizeName(buildResourcePath(file, rootFile, false, resultPrefix)));
                }
            }
        }
        return returnSet;
    }

    private String buildResourcePath(File file, File relativeTo, boolean isFolder, String resultPrefix) {
        List<String> result = new ArrayList<String>();
        while (file != null) {
            if (file.getName().equals(relativeTo.getName())) {
                break; // FIXME: check more path somehow -> maybe bug in folders like; /foo/bar/bar/bar/inc.xml
            }
            result.add(standardizeName(file.getName()));
            file = file.getParentFile();
        }
        StringBuilder buf = new StringBuilder();
        buf.append(resultPrefix);
        if (!resultPrefix.endsWith(ListHandler.RESOURCE_PATH_SEPERATOR)) {
            buf.append(ListHandler.RESOURCE_PATH_SEPERATOR);
        }
        Collections.reverse(result);
        Iterator<String> i = result.iterator();
        while (i.hasNext()) {
            buf.append(i.next());
            if (i.hasNext() && !isFolder) {
                buf.append(ListHandler.RESOURCE_PATH_SEPERATOR);
            }
        }
        return buf.toString();
    }

    protected String standardizeName(String name) {
        return name.replace("\\", RESOURCE_PATH_SEPERATOR);
    }

    @Override
    public String toString() {
        return getClass().getName() + "(" + baseDirectory + ")";
    }
}
