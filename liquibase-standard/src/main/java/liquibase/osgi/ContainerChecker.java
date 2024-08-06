package liquibase.osgi;

import lombok.Getter;

public class ContainerChecker {

    @Getter
    private static volatile boolean osgiPlatform = false;

    static void osgiPlatform() {
        osgiPlatform = true;
    }
}
