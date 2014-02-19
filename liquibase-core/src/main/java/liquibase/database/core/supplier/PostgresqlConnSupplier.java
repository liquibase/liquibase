package liquibase.database.core.supplier;

import liquibase.sdk.TemplateService;
import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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

    @Override
    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:postgresql://"+ getIpAddress() +"/liquibase";
    }

    @Override
    public Set<String> getPuppetModules() {
        Set<String> modules = super.getPuppetModules();
        modules.add("puppetlabs/postgresql");
        return modules;
    }

    @Override
    public String getPuppetInit(String box) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("supplier", this);
        return TemplateService.getInstance().output("liquibase/sdk/vagrant/supplier/postgresql/postgresql-linux.puppet.vm", context);
    }


    @Override
    public void writeConfigFiles(File configDir) throws IOException {
        super.writeConfigFiles(configDir);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("supplier", this);

        TemplateService.getInstance().write("liquibase/sdk/vagrant/supplier/postgresql/postgresql.init.sql.vm", new File(configDir, "postgresql.init.sql"), context);
        TemplateService.getInstance().write("liquibase/sdk/vagrant/supplier/postgresql/postgresql.conf.vm", new File(configDir, "postgresql.conf"), context);
        TemplateService.getInstance().write("liquibase/sdk/vagrant/supplier/postgresql/pg_hba.conf.vm", new File(configDir, "pg_hba.conf"), context);
    }

//    @Override
//    public String getPuppetInit(String box) throws IOException {
//        return "class { '::postgresql::server':\n" +
//                "    ip_mask_deny_postgres_user => '0.0.0.0/32',\n" +
//                "    ip_mask_allow_all_users    => '0.0.0.0/0',\n" +
//                "    listen_addresses           => '*',\n" +
//                "    ipv4acls                   => ['host all liquibase 0.0.0.0/0 password'],\n" +
//                "    postgres_password          => 'postgres',\n" +
//                "}\n" +
//                "\n" +
//                "postgresql::server::db { 'liquibase':\n" +
//                "  user     => '"+ getDatabaseUsername()+"',\n" +
//                "  password => '"+ getDatabasePassword()+"'\n" +
//                "}\n";
//    }
}
