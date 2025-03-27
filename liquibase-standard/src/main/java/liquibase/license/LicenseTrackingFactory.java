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
            Scope.getCurrentScope().getLog(getClass()).severe("Liquibase was unable to transmit license tracking information to the Liquibase License Utility (LLU) server. Please verify that LLU is running, Liquibase is configured with the correct LLU server address, and that the LLU server is accessible from the machine where Liquibase is executing.", e);
        }
    }
}
