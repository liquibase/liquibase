package liquibase.dbdoc;

import java.net.URLEncoder;

public class DBDocUtil {


    public static String htmlEncode(String string) {
        return string.replace("\"","&quot;").replace( "'", "&#39;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String toFileName(String string) {
        return string.replaceAll("[^\\w\\.\\\\/-]", "_");
    }

}
