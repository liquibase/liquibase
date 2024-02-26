package liquibase.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UrlUtil {

    /**
     * Move the specified keys up in the connection url for better readability and remove additional parameters. As far as I can tell this is sql server specific right now.
     *
     * @param keys the important keys to keep at the start of the connection url
     * @param url  the url to modify
     * @return a new connection url with the connection parameters matching the keys provided moved forward, sorted alphabetically and with all other parameters REMOVED.
     */
    public static String handleSqlServerDbUrlParameters(List<String> keys, String url) {
        if (url != null && url.contains(";")) {
            List<String> moveForward = new ArrayList<>();
            String[] parts = url.split(";");
            String mainPart = parts[0];
            for (String part : parts) {
                if (keys.stream().anyMatch(key -> part.toLowerCase().startsWith(key.toLowerCase() + "="))) {
                    moveForward.add(part);
                }
            }
            Collections.sort(moveForward);
            String forward = String.join(";", moveForward);
            return String.format("%s;%s", mainPart, forward);
        }
        return url;
    }
}
