package liquibase.database.core.supplier;

import liquibase.sdk.TemplateService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    public String getPuppetInit(String box) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("supplier", this);
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
    public void writeConfigFiles(File configDir) throws IOException {
        super.writeConfigFiles(configDir);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("supplier", this);

        TemplateService.getInstance().write("liquibase/sdk/vagrant/supplier/mysql/mysql.ini.vm", new File(configDir, "mysql.ini"), context);
    }
}
