package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class PostgresqlConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "postgresql";
    }

    @Override
    public String getAdminUsername() {
        return "postgres";
    }

    public String getInstallDir() {
        return "C:\\pgsql-"+getShortVersion();
    }

    @Override
    public String getVersion() {
        String version = super.getVersion();
        if (version == null) {
            return "9.3.2-3";
        }
        return version;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:postgresql://"+ getIpAddress() +"/"+getPrimaryCatalog();
    }

    @Override
    public Set<String> getPuppetModules() {
        Set<String> modules = super.getPuppetModules();
        modules.add("puppetlabs/postgresql");
        return modules;
    }

    @Override
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        if (isWindows()) {
            return new ConfigTemplate("liquibase/sdk/vagrant/supplier/postgresql/postgresql-windows.puppet.vm", context);
        } else {
            return new ConfigTemplate("liquibase/sdk/vagrant/supplier/postgresql/postgresql-linux.puppet.vm", context);
        }
    }


    @Override
    public Set<ConfigTemplate> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigTemplate> configTemplates = super.generateConfigFiles(context);

        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/postgresql/postgresql.init.sql.vm", context));
        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/postgresql/postgresql.conf.vm", context));
        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/postgresql/pg_hba.conf.vm", context));

        return configTemplates;
    }

    @Override
    public String getDescription() {
        if (isWindows()) {
            return super.getDescription() + "\n"+
                    "Install Dir: "+getInstallDir()+"\n" +
                    "REQUIRES: LIQUIBASE_HOME/sdk/vagrant/install-files/windows/vcredist_64.exe. Download Microsoft Visual C++ 2010 Redistributable Package (x64) from http://www.microsoft.com/en-us/download/confirmation.aspx?id=14632\n"+
                    "REQUIRES: LIQUIBASE_HOME/sdk/vagrant/install-files/postgresql/postgresql-"+getVersion()+"-windows-x64-binaries.zip. Download Win x86-64 archive from http://www.enterprisedb.com/products-services-training/pgbindownload\n"+
                    "Admin 'postgres' user password: "+getAdminPassword();
        } else {
            return super.getDescription();
        }
    }


}
