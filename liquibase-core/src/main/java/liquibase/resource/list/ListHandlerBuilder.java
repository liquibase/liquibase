package liquibase.resource.list;

import java.net.URL;

public interface ListHandlerBuilder {

    /**
     * Creates an ListHander based on the urlPath.
     */
    ListHandler buildListHandler(String urlPath);

    /**
     * Builds an urlPath from a classloader resources url.
     * when the path parameter is given then it is matched an removed from the url.
     */
    String buildURLPath(URL url,String path);
}
