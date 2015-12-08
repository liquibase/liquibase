package liquibase.database.core.mysql;


import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.ConnectionSupplier;
import liquibase.database.Database;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class MySQLConnectionSupplier extends ConnectionSupplier {

    @Override
    public String getDatabaseShortName() {
        return "mysql";
    }

    public int getPort() {
        return 3306;
    }

    @Override
    public String getAdminUsername() {
        return "root";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:mysql://"+ getIpAddress() +"/"+getPrimaryCatalog();
    }

    @Override
    public Set<String> getPuppetModules() {
        Set<String> modules = super.getPuppetModules();
        modules.add("puppetlabs/mysql");
        return modules;
    }

    @Override
    public Database getDatabase(Scope scope) {
        Database database = super.getDatabase(scope);
        ((AbstractJdbcDatabase) database).setCaseSensitive(false);
        return database;
    }

    @Override
    public String getConfigurationName() {
        return "caseInsensitive";
    }

    @Override
    public Set<ConfigTemplate> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigTemplate> configTemplates = super.generateConfigFiles(context);
        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/mysql/mysql.init.sql.vm", context));

        if (isWindows()) {
            configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/mysql/mysql.ini.vm", context));
        }

        return configTemplates;
    }



//    @Override
//    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
//        if (isWindows()) {
//            return new ConfigTemplate("liquibase/sdk/vagrant/supplier/mysql/mysql-windows.puppet.vm", context);
//        } else {
//            return new ConfigTemplate("liquibase/sdk/vagrant/supplier/mysql/mysql-linux.puppet.vm", context);
//        }
//    }

    public String getSourceUrl() {
        return "http://dev.mysql.com/get/Downloads/MySQL-"+getShortVersion()+"/mysql-"+getVersion()+"-winx64.msi";
    }

    public String getInstallDir() {
        return "C:\\mysql-"+getShortVersion();
    }

    @Override
    public String getDescription() {
        if (getOs().equals("windows")) {
            return super.getDescription() + "\n"+
                    "Install Dir: "+getInstallDir()+"\n" +
                    "Port: "+getPort()+"\n" +
                    "Root Password: "+getAdminPassword();
        } else {
            return super.getDescription();
        }

    }

    @Override
    public String getPrimarySchema() {
        return "lbcat".toLowerCase();
    }

    @Override
    public String getAlternateSchema() {
        return "lbcat2".toLowerCase();
    }
}
