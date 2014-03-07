package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class DB2ConnSupplier extends ConnectionSupplier {
    private String installDir = "C:\\Program Files\\IBM\\SQLLIB";
    private String sshInstallDir = "C:\\Program Files\\IBM\\IBM SSH Server";
    private String instanceName = "DB2";

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
        if (isWindows()) {
            return new ConfigTemplate("liquibase/sdk/vagrant/supplier/db2/db2-windows.puppet.vm", context);
        } else {
            return new ConfigTemplate("liquibase/sdk/vagrant/supplier/db2/db2-linux.puppet.vm", context);
        }
    }

    @Override
    public Set<ConfigTemplate> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigTemplate> configTemplates = super.generateConfigFiles(context);
        if (isWindows()) {
            configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/db2/db2expc_install.windows.rsp.vm", context));
        } else {
            configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/db2/db2expc_install.linux.rsp.vm", context));
        }
        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/db2/db2.init.sql.vm", context));

        return configTemplates;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstallDir() {
        return installDir;
    }

    public void setInstallDir(String installDir) {
        this.installDir = installDir;
    }

    public String getSshInstallDir() {
        return sshInstallDir;
    }

    public void setSshInstallDir(String sshInstallDir) {
        this.sshInstallDir = sshInstallDir;
    }

    @Override
    public String getDescription() {
        return super.getDescription() +
                "Instance Name: "+ getInstanceName()+"\n"+
                "Install Directory: "+getInstallDir()+"\n" +
                "\n"+
                "REQUIRED: You must manually download the db2 express installation files into LIQUIBASE_HOME/sdk/vagrant/install-files/db2/\n"+
                "      You can download the DB2 Express-C install files from http://www-01.ibm.com/software/data/db2/linux-unix-windows/downloads.html\n"+
                "      Expected file(s): v10.5fp1_winx64_expc.exe";
    }

    //    @Override
//    public Set<String> getRequiredPackages() {
//        Set<String> requiredPackages = super.getRequiredPackages();
//        requiredPackages.addAll(Arrays.asList("lib32stdc++6"));
//
//        return requiredPackages;
//    }

}
