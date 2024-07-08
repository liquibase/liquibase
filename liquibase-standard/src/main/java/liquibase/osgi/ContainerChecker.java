package liquibase.osgi;

public class ContainerChecker {

    private static volatile boolean osgiPlatform = false;

    public static boolean isOsgiPlatform() {
        return osgiPlatform;
    }

    static void osgiPlatform() {
        osgiPlatform = true;
    }
}
