package liquibase.database.core.supplier;

import liquibase.sdk.TemplateService;
import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class MySQLConnSupplierWindows extends MySQLConnSupplier {

    @Override
    public String getConfigurationName() {
        return "windows";
    }

    @Override
    public String getVersion() {
        return "5.5.36";
    }

    public String getSourceUrl() {
        return "http://dev.mysql.com/get/Downloads/MySQL-"+getShortVersion()+"/mysql-"+getVersion()+"-winx64.msi";
    }

    public String getInstallDir() {
        return "C:\\mysql-"+getShortVersion();
    }

    @Override
    public String getPuppetInit(Map<String, Object> context) throws IOException {
        return TemplateService.getInstance().output("liquibase/sdk/vagrant/supplier/mysql/mysql-windows.puppet.vm", context);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "\n"+
                "Install Dir: "+getInstallDir()+"\n" +
                "Port: "+getPort()+"\n" +
                "Root Password: "+getAdminPassword();
    }

    @Override
    public Set<ConfigFile> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigFile> configFiles = super.generateConfigFiles(context);

        configFiles.add(new ConfigFile("liquibase/sdk/vagrant/supplier/mysql/mysql.ini.vm", context));

        return configFiles;
    }
}
