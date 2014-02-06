package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.util.Set;

public class MySQLSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "mysql";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:mysql://"+ getHostname() +"/liquibase";
    }

    @Override
    public Set<String> getPuppetModules() {
        Set<String> modules = super.getPuppetModules();
        addPuppetModules(modules);
        return modules;
    }

    protected void addPuppetModules(Set<String> modules) {
        modules.add("puppetlabs/mysql");
    }

    @Override
    public Set<String> getRequiredPackages(String vagrantBoxName) {
        return super.getRequiredPackages(vagrantBoxName);
    }

    @Override
    public String getPuppetInit(String box) {
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
