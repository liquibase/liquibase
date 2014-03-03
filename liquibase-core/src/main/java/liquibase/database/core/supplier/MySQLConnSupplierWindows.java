package liquibase.database.core.supplier;

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
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        return new ConfigTemplate("liquibase/sdk/vagrant/supplier/mysql/mysql-windows.puppet.vm", context);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "\n"+
                "Install Dir: "+getInstallDir()+"\n" +
                "Port: "+getPort()+"\n" +
                "Root Password: "+getAdminPassword();
    }

    @Override
    public Set<ConfigTemplate> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigTemplate> configTemplates = super.generateConfigFiles(context);

        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/mysql/mysql.ini.vm", context));

        return configTemplates;
    }
}
