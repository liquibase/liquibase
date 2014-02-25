package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class DB2ConnSupplier extends ConnectionSupplier {
    private String installDir = "C:\\Program Files\\IBM\\SQLLIB\\";
    private String sshInstalDir = "C:\\Program Files\\IBM\\IBM SSH Server";

    @Override
    public String getDatabaseShortName() {
        return "db2";
    }

    @Override
    public String getAdminUsername() {
        return "db2admin";
    }

    @Override
    public String getPrimaryCatalog() {
        return super.getPrimaryCatalog().toUpperCase();
    }

    @Override
    public String getAlternateCatalog() {
        return super.getAlternateCatalog().toUpperCase();
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:db2://"+ getIpAddress() +":50000/"+getPrimaryCatalog().toLowerCase();
    }

    @Override
    public Set<String> getRequiredPackages(String vagrantBoxName) {
        Set<String> requiredPackages = super.getRequiredPackages(vagrantBoxName);
        requiredPackages.addAll(Arrays.asList("compat-libstdc++-33", "pam.i686", "numactl"));

        return requiredPackages;
    }

    @Override
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        return new ConfigTemplate("liquibase/sdk/vagrant/supplier/db2/db2.puppet.vm", context);
    }

    @Override
    public Set<ConfigTemplate> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigTemplate> configTemplates = super.generateConfigFiles(context);
        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/db2/db2exprc_install.windows.rsp.vm", context));

        return configTemplates;
    }


    public String getInstallDir() {
        return installDir;
    }

    public void setInstallDir(String installDir) {
        this.installDir = installDir;
    }

    public String getSshInstalDir() {
        return sshInstalDir;
    }

    public void setSshInstalDir(String sshInstalDir) {
        this.sshInstalDir = sshInstalDir;
    }

    //    @Override
//    public Set<String> getRequiredPackages() {
//        Set<String> requiredPackages = super.getRequiredPackages();
//        requiredPackages.addAll(Arrays.asList("lib32stdc++6"));
//
//        return requiredPackages;
//    }

}
