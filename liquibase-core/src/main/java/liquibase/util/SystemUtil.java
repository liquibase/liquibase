package liquibase.util;

public class SystemUtil {

    public static boolean isWindows() {
        return StringUtil.trimToEmpty(System.getProperties().getProperty("os.name")).toLowerCase().startsWith("windows");
    }

    /**
     * Returns java.version system property
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }


    /**
     * Returns the "major" version of java, including returning "8" for java "1.8"
     */
    public static int getJavaMajorVersion() {
        final String version = getJavaVersion();
        String[] splitVersion = version.split("\\.", 2);
        int majorVersion = Integer.parseInt(splitVersion[0]);
        if (majorVersion == 1) {
            splitVersion = splitVersion[1].split("\\.", 2);
            return Integer.parseInt(splitVersion[0]);
        }
        return majorVersion;
    }

    public static boolean isAtLeastJava11() {
        return getJavaMajorVersion() >= 11;
    }
}
