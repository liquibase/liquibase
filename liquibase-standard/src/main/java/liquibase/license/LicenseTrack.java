package liquibase.license;

import liquibase.Scope;
import liquibase.configuration.ConfiguredValue;
import liquibase.util.NetUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;

import java.util.Collections;
import java.util.List;

@Data
public class LicenseTrack {
    private String licenseKey = null;
    private String jdbcUrl;
    private String schema;
    private String catalog;
    private String databaseName;
    private List<User> users;

    public LicenseTrack(String jdbcUrl, String schema, String catalog, String databaseName) {
        LicenseServiceFactory licenseServiceFactory = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class);
        if (licenseServiceFactory != null) {
            LicenseService licenseService = licenseServiceFactory.getLicenseService();
            if (licenseService != null) {
                ConfiguredValue<String> configuredValue = licenseService.getLicenseKey();
                if (configuredValue != null && configuredValue.found()) {
                    this.licenseKey = configuredValue.getValue();
                }
            }
        }
        this.jdbcUrl = jdbcUrl;
        this.schema = schema;
        this.catalog = catalog;
        this.databaseName = databaseName;
        ConfiguredValue<String> userCurrentConfiguredValue = LicenseTrackingArgs.TRACKING_ID.getCurrentConfiguredValue();
        if (userCurrentConfiguredValue.found()) {
            this.users = Collections.singletonList(new User(userCurrentConfiguredValue.getValue()));
        } else {
            this.users = Collections.singletonList(new User(StringUtils.joinWith("@", SystemProperties.getUserName(), NetUtil.getLocalHostName())));
        }
    }
}
