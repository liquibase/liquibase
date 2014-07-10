package liquibase.resource;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;
import liquibase.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractResourceAccessor implements ResourceAccessor {

    private Set<String> rootStrings = new HashSet<String>();

    protected void init() {
        Enumeration<URL> baseUrls;
        try {
            baseUrls = toClassLoader().getResources("");
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        while (baseUrls.hasMoreElements()) {
            this.rootStrings.add(baseUrls.nextElement().toExternalForm());
        }
    }

    protected boolean isCaseSensitive() {
        return !SystemUtils.isWindows();
    }

    protected Set<String> getRootPaths() {
        return rootStrings;
    }

    protected void getContents(File rootFile, boolean recursive, boolean includeFiles, boolean includeDirectories, String basePath, Set<String> returnSet) {
        File[] files = rootFile.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                if (includeDirectories) {
                    returnSet.add(convertToPath(file.getAbsolutePath()));
                }
                if (recursive) {
                    getContents(file, recursive, includeFiles, includeDirectories, basePath, returnSet);
                }
            } else {
                if (includeFiles) {
                    returnSet.add(convertToPath(file.getAbsolutePath()));
                }
            }
        }
    }

    protected String convertToPath(String string) {
        string = string.replace("\\", "/");

        String stringAsUrl = string;
        if (!stringAsUrl.matches("[a-zA-Z0-9]{2,}:.*")) {
            if (stringAsUrl.startsWith("/")) {
                stringAsUrl = "file:"+stringAsUrl;
            } else {
                stringAsUrl = "file:/" + stringAsUrl;
            }
        }
        for (String rootString : getRootPaths()) {
            boolean matches = false;
            if (isCaseSensitive()) {
                matches = stringAsUrl.startsWith(rootString);
            } else {
                matches = stringAsUrl.toLowerCase().startsWith(rootString.toLowerCase());
            }

            if (matches) {
                string = stringAsUrl.substring(rootString.length());
                break;
            }
        }

        string = string.replaceFirst("^//", "/");
        while (string.matches(".*[^:]//.*")) {
            string = string.replaceAll("([^:])//", "$1/");
        }
        while (string.contains("/./")) {
            string = string.replace("/./", "/");
        }
        while (string.matches(".*/.*?/\\.\\./.*")) {
            string = string.replaceAll("/[^/]+/\\.\\./", "/");
        }

        string = string.replaceFirst(".*liquibase-unzip\\d+\\.dir/", ""); //
        return string;
    }

    protected String convertToPath(String relativeTo, String path) {
        if (StringUtils.trimToNull(relativeTo) == null) {
            return path;
        }
        URL baseUrl = toClassLoader().getResource(relativeTo);
        if (baseUrl == null) {
            throw new UnexpectedLiquibaseException("Cannot find base path '"+relativeTo+"'");
        }
        if (baseUrl.toExternalForm().startsWith("file:")) {
            File baseFile = new File(baseUrl.getPath());
            if (!baseFile.exists()) {
                throw new UnexpectedLiquibaseException("Base file '"+baseFile.getAbsolutePath()+"' does not exist");
            }
            if (baseFile.isFile()) {
                baseFile = baseFile.getParentFile();
            }

            return convertToPath(baseFile.toURI().getPath()+"/"+path);
        } else {
            return convertToPath(relativeTo+"/"+path);
        }
    }


}
