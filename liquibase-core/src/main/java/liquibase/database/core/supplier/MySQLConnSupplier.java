package liquibase.database.core.supplier;

import liquibase.sdk.TemplateService;
import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    public Set<String> getRequiredPackages(String vagrantBoxName) {
        Set<String> packages = super.getRequiredPackages(vagrantBoxName);
        packages.add("mysql");
        return packages;
    }

    @Override
    public void writeConfigFiles(File configDir) throws IOException {
        super.writeConfigFiles(configDir);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("supplier", this);


        TemplateService.getInstance().write("liquibase/sdk/vagrant/supplier/mysql/mysql.init.sql.vm", new File(configDir, "mysql.init.sql"), context);
    }

    @Override
    public String getPuppetInit(String box) throws IOException {
        return "class { '::mysql::server':\n" +
                "    require => Package['mysql'],\n"+
                "    root_password => '"+getAdminPassword()+"',\n"+
                (getVersion() == null ? "" : "    package_ensure => '"+getVersion()+"',\n")+
                "    override_options => { 'mysqld' => { 'bind_address'  => '0.0.0.0' } }, \n" +
                "}\n" +
                "\n" +
                "exec { \"Create mysql users and databases\":\n" +
                "    require => Class['::mysql::server'],\n" +
                "    command => '/bin/sh -c \"mysql -u "+getAdminUsername()+" -p"+getAdminPassword()+" mysql < /vagrant/modules/conf/mysql.init.sql\"'\n" +
                "}\n";

    }
}
