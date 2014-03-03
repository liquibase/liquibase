package liquibase.database.core.supplier;

import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;

import java.util.Map;

public class OracleConnSupplierWindows extends OracleConnSupplier {

    @Override
    public String getConfigurationName() {
        return "windows";
    }

    public String getInstallDir() {
        return "C:\\oracle-"+getVersion();
    }

    @Override
    public String getZipFileBase() {
        if (getVersion().startsWith("12.")) {
            return "winx64_12c_database";
        } else {
            throw new UnexpectedLiquibaseSdkException("Unsupported oracle version: "+getVersion());
        }
    }

    public String getFileSeparator() {
        return "\\";
    }

    @Override
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        return new ConfigTemplate("liquibase/sdk/vagrant/supplier/oracle/oracle-windows.puppet.vm", context);
    }
}
