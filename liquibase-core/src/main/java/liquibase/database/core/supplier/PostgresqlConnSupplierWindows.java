package liquibase.database.core.supplier;

import liquibase.sdk.TemplateService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PostgresqlConnSupplierWindows extends PostgresqlConnSupplier {

    @Override
    public String getConfigurationName() {
        return "windows";
    }

    @Override
    public String getVersion() {
        return "9.3.2-3";
    }

    public String getInstallDir() {
        return "C:\\pgsql-"+getShortVersion();
    }

    @Override
    public String getVagrantBaseBoxName() {
        return VAGRANT_BOX_NAME_WINDOWS_STANDARD;
    }

    @Override
    public String getPuppetInit(String box) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("supplier", this);
        return TemplateService.getInstance().output("liquibase/sdk/vagrant/supplier/postgresql/postgresql-windows.puppet.vm", context);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "\n"+
                "Install Dir: "+getInstallDir()+"\n" +
                "REQUIRES: LIQUIBASE_HOME/sdk/vagrant/install-files/windows/vcredist_64.exe. Download Microsoft Visual C++ 2010 Redistributable Package (x64) from http://www.microsoft.com/en-us/download/confirmation.aspx?id=14632\n"+
                "REQUIRES: LIQUIBASE_HOME/sdk/vagrant/install-files/postgresql/postgresql-"+getVersion()+"-windows-x64-binaries.zip. Download Win x86-64 archive from http://www.enterprisedb.com/products-services-training/pgbindownload\n"+
                "Admin 'postgres' user password: "+getAdminPassword();
    }
}
