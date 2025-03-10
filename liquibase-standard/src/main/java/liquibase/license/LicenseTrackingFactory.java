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
            Scope.getCurrentScope().getLog(getClass()).log(LicenseTrackingArgs.LOG_LEVEL.getCurrentValue(), "Failed to handle license tracking event", e);
        }
    }

}
