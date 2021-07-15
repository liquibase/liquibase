package liquibase.util;

public class SystemUtil {

    public static boolean isWindows() {
        return StringUtil.trimToEmpty(System.getProperties().getProperty("os.name")).toLowerCase().startsWith("windows");
    }
}
