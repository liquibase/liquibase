package liquibase.resource;

import liquibase.integration.spring.SpringResourceAccessor;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor
public class ResourceAccessorUtils {

    /**
     * Ensure classpath prefix and duplicated /'s are removed from SearchPath.
     */
    public static String normalizeSearchPath(String searchPath, ResourceAccessor resourceAccessor) {
        if(resourceAccessor instanceof ClassLoaderResourceAccessor) {
            if (searchPath.matches("^classpath\\*?:.*")) {
                searchPath = searchPath.replaceFirst("^classpath\\*?:", "");
            }
            searchPath = searchPath.replaceAll("//+", "/");
        } else if(resourceAccessor instanceof SpringResourceAccessor) {
            if(searchPath.matches("^classpath\\*?:.*")) {
                searchPath = searchPath.replace("classpath:","").replace("classpath*:","");
                searchPath = "classpath*:/" +searchPath;
            } else if(!searchPath.matches("^\\w+:.*")) {
                searchPath = "classpath*:/" +searchPath;
            }
            searchPath = searchPath.replace("\\", "/");
            searchPath = searchPath.replaceAll("//+", "/");
        }
        searchPath = StringUtils.cleanPath(searchPath);
        return searchPath;
    }

}
