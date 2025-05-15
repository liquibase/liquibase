package liquibase.license;

import liquibase.plugin.Plugin;

public interface LicenseTrackingListener extends Plugin {

    int getPriority();
    void handleEvent(LicenseTrackList event) throws Exception;
}
