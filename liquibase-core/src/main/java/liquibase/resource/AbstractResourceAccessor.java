package liquibase.resource;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;
import liquibase.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.List;


public abstract class AbstractResourceAccessor implements ResourceAccessor {

    //We don't use an HashSet otherwise iteration order is not deterministic
	private List<String> rootStrings = new ArrayList<String>();

    protected AbstractResourceAccessor() {
        init();
    }

    protected void init() {
        try {
            Enumeration<URL> baseUrls;
            ClassLoader classLoader = toClassLoader();
            if (classLoader != null) {
                baseUrls = classLoader.getResources("");

                while (baseUrls.hasMoreElements()) {
                    addRootPath(baseUrls.nextElement());
                }
            }
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected boolean isCaseSensitive() {
        return !SystemUtils.isWindows();
    }

    protected void addRootPath(URL path) {
    	String externalForm = path.toExternalForm();
    	if (!externalForm.endsWith("/")) {
    		externalForm += "/";
    	}
    	if (!rootStrings.contains(externalForm)) {
    		rootStrings.add(externalForm);
    	}
    }

    protected List<String> getRootPaths() {
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
        String base;
        if (baseUrl.toExternalForm().startsWith("file:")) {
            File baseFile = new File(baseUrl.getPath());
            if (!baseFile.exists()) {
                throw new UnexpectedLiquibaseException("Base file '" + baseFile.getAbsolutePath() + "' does not exist");
            }
            if (baseFile.isFile()) {
                baseFile = baseFile.getParentFile();
            }
            base = baseFile.toURI().getPath();
        } else if (baseUrl.toExternalForm().startsWith("jar:file:")) {
                return convertToPath(new File(relativeTo).getParent() + '/' + path);
        } else {
            base = relativeTo;
        }
        String separator = "";
        if (!base.endsWith("/") && !path.startsWith("/")) {
        	separator = "/";
        }
        if (base.endsWith("/") && path.startsWith("/")) {
        	base = base.substring(0, base.length() - 1);
        }            
        return convertToPath(base + separator + path);
    }


}
