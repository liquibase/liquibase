package liquibase.database.core.supplier;

import liquibase.sdk.TemplateService;

import java.io.IOException;
import java.util.HashMap;
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
        return "winx64_12c_database";
    }

    @Override
    public String getPuppetInit(String box) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("supplier", this);
        return TemplateService.getInstance().output("liquibase/sdk/vagrant/supplier/oracle/oracle-windows.puppet.vm", context);
    }
}
