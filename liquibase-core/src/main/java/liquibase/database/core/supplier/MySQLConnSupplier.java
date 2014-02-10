package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.IOException;
import java.util.Set;

public class MySQLConnSupplier extends ConnectionSupplier {
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
    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:mysql://"+ getIpAddress() +"/"+getPrimaryCatalog();
    }

    @Override
    public Set<String> getPuppetModules() {
        Set<String> modules = super.getPuppetModules();
        modules.add("puppetlabs/mysql");
        return modules;
    }

    @Override
    public Set<String> getRequiredPackages(String vagrantBoxName) {
        return super.getRequiredPackages(vagrantBoxName);
    }

    @Override
    public String getPuppetInit(String box) throws IOException {
        return "class { '::mysql::server':\n" +
                "    root_password => 'root',\n"+
                (getVersion() == null ? "" : "    package_ensure => '"+getVersion()+"',\n")+
                "    override_options => { 'mysqld' => { 'bind_address'  => '0.0.0.0' } }, \n" +
                "}\n" +
                "\n" +
                "mysql::db { '"+getPrimaryCatalog()+"':\n" +
                "  user     => '"+ getDatabaseUsername()+"',\n" +
                "  password => '"+ getDatabasePassword()+"',\n" +
                "  host     => '%',\n" +
                "  grant    => ['all'],\n" +
                "}\n" +
                "\n" +
                "mysql::db { '"+getAlternateCatalog()+"':\n" +
                "  user     => '"+ getAlternateUsername()+"',\n" +
                "  password => '"+ getAlternateUserPassword()+"',\n" +
                "  host     => '%',\n" +
                "  grant    => ['all'],\n" +
                "}\n";

    }
}
