package liquibase.license;

import liquibase.Scope;
import liquibase.plugin.AbstractPluginFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LicenseTrackingFactory extends AbstractPluginFactory<LicenseTrackingListener> {


    @Override
    protected Class<LicenseTrackingListener> getPluginClass() {
        return LicenseTrackingListener.class;
    }

    @Override
    protected int getPriority(LicenseTrackingListener obj, Object... args) {
        return obj.getPriority();
    }

    public void handleEvent(LicenseTrackList event) {
        try {
            if (LicenseTrackingArgs.ENABLED.getCurrentValue()) {
                LicenseTrackingListener plugin = getPlugin();
                if (plugin != null) {
                    plugin.handleEvent(event);
                }
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).severe("Liquibase was unable to transmit license tracking information to the Liquibase License Tracking (LLT) server. Please verify that LLT is running, Liquibase is configured with the correct LLT server address, and that the LLT server is accessible from the machine where Liquibase is executing.", e);
        }
    }
}
