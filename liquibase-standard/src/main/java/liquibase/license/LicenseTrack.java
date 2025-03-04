package liquibase.license;

import liquibase.Scope;
import lombok.Data;

@Data
public class LicenseTrack {
    private String licenseKey;
    private String jdbcUrl;
    private String schema;
    private String catalog;

    public LicenseTrack(String jdbcUrl, String schema, String catalog) {
        LicenseServiceFactory license = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class);
        this.licenseKey = license.getLicenseService().getLicenseKey().getValue();
        this.jdbcUrl = jdbcUrl;
        this.schema = schema;
        this.catalog = catalog;
    }
}
