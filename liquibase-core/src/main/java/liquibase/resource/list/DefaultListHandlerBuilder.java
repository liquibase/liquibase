package liquibase.resource.list;

import java.net.URL;

import liquibase.util.Validate;

public class DefaultListHandlerBuilder implements ListHandlerBuilder {

    protected static final String PREFIX_FILE = "file:";
    protected static final String PREFIX_JAR_FILE = "jar:file:";
    protected static final String PREFIX_WS_JAR_FILE = "wsjar:file:";
    protected static final String PREFIX_ZIP_FILE = "zip:";
    //protected static final String PREFIX_VFS_FILE = "vfs:";
    protected static final String POSTFIX_JAR = ".jar";
    protected static final String POSTFIX_ZIP = ".zip";
    protected static final String JAR_URL_QUERY_PART = "!/";

    @Override
    public ListHandler buildListHandler(String urlPath) {
        Validate.notNullArgument(urlPath, "Can't select list handler on null urlPath");
        if (urlPath.startsWith(PREFIX_FILE)) {
            if (urlPath.endsWith(POSTFIX_JAR)) {
                return new JarFileListHandler(urlPath.substring(PREFIX_FILE.length()));
            } else if (urlPath.endsWith(POSTFIX_ZIP)) {
                return new JarFileListHandler(urlPath.substring(PREFIX_FILE.length()));
            } else {
                return new FolderListHandler(urlPath.substring(PREFIX_FILE.length()));
            }
        } else if (urlPath.startsWith(PREFIX_JAR_FILE)) {
            return new JarFileListHandler(urlPath.substring(PREFIX_JAR_FILE.length()));
        } else if (urlPath.startsWith(PREFIX_WS_JAR_FILE)) {
            return new JarFileListHandler(urlPath.substring(PREFIX_WS_JAR_FILE.length()));
        } else if (urlPath.startsWith(PREFIX_ZIP_FILE)) {
            return new JarFileListHandler(urlPath.substring(PREFIX_ZIP_FILE.length()));
        } else {
            return null;
        }
    }

    @Override
    public String buildURLPath(URL url,String path) {
        String rootPath = Validate.notNullArgument(url, "Can't add null rootPath").toExternalForm();
        Validate.isTrueArgument(!rootPath.isEmpty(), "Can't add empty rootPath");
        if (path != null) {
            if (path.startsWith(".")) {
                path = path.substring(1); // remove user provided ./
            }
            int contentIdx = rootPath.indexOf(path);
            if (contentIdx > 0) {
                rootPath = rootPath.substring(0, contentIdx); // remove content path
            } else {
                throw new IllegalArgumentException("path does not match rootPath='"+rootPath+"' path='"+path+"'");
            }
        }
        if (rootPath.endsWith(JAR_URL_QUERY_PART)) {
            rootPath = rootPath.substring(0,rootPath.length()-JAR_URL_QUERY_PART.length()); // remove jar url query string
        }
        if (rootPath.isEmpty()) {
            throw new IllegalArgumentException("Can't process empty url path for list scanning");
        }
        return rootPath;
    }
}
