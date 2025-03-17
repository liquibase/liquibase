package liquibase.license;

import liquibase.Scope;
import liquibase.configuration.ConfiguredValue;
import lombok.Data;

@Data
public class LicenseTrack {
    private String licenseKey = null;
    private String jdbcUrl;
    private String schema;
    private String catalog;

    public LicenseTrack(String jdbcUrl, String schema, String catalog) {
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
    }
}
