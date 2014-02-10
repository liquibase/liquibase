package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.util.Set;

public class PostgresConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "postgresql";
    }

    @Override
    public String getAdminUsername() {
        return "root";
    }

    @Override
    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:postgresql://"+ getIpAddress() +"/liquibase";
    }

    @Override
    public Set<String> getPuppetModules() {
        Set<String> modules = super.getPuppetModules();
        modules.add("puppetlabs/postgresql");
        return modules;
    }

    @Override
    public String getPuppetInit(String box) {
        return "class { '::postgresql::server':\n" +
                "    ip_mask_deny_postgres_user => '0.0.0.0/32',\n" +
                "    ip_mask_allow_all_users    => '0.0.0.0/0',\n" +
                "    listen_addresses           => '*',\n" +
                "    ipv4acls                   => ['host all liquibase 0.0.0.0/0 password'],\n" +
                "    postgres_password          => 'postgres',\n" +
                "}\n" +
                "\n" +
                "postgresql::server::db { 'liquibase':\n" +
                "  user     => '"+ getDatabaseUsername()+"',\n" +
                "  password => '"+ getDatabasePassword()+"'\n" +
                "}\n";
    }
}
