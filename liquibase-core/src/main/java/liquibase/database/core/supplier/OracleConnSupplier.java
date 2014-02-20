package liquibase.database.core.supplier;

import liquibase.sdk.TemplateService;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OracleConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "oracle";
    }

    @Override
    public String getAdminUsername() {
        return "system";
    }

    @Override
    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:oracle:thin:@" + getIpAddress() + ":1521:"+getSid();
    }

    @Override
    public Set<String> getRequiredPackages(String vagrantBoxName) {
        Set<String> requiredPackages = super.getRequiredPackages(vagrantBoxName);
        requiredPackages.addAll(Arrays.asList("binutils",
                "compat-libcap1",
                "gcc",
                "gcc-c++",
                "glibc",
                "glibc-devel",
                "ksh",
                "libgcc",
                "libstdc++",
                "libstdc++-devel",
                "libaio",
                "libaio-devel",
                "libXext",
                "libX11",
                "libXau",
                "libxcb",
                "libXi",
                "make",
                "sysstat",
                "rlwrap"
        ));

        return requiredPackages;
    }

    @Override
    public String getPuppetInit(String box) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("supplier", this);
        return TemplateService.getInstance().output("liquibase/sdk/vagrant/supplier/oracle/oracle-linux.puppet.vm", context);
    }

    @Override
    public void writeConfigFiles(File configDir) throws IOException {
        super.writeConfigFiles(configDir);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("supplier", this);

        TemplateService.getInstance().write("liquibase/sdk/vagrant/supplier/oracle/oracle_install.rsp.vm", new File(configDir, "oracle/oracle_install.rsp"), context);
        TemplateService.getInstance().write("liquibase/sdk/vagrant/supplier/oracle/oracle_netca.rsp.vm", new File(configDir, "oracle/oracle_netca.rsp"), context);
        TemplateService.getInstance().write("liquibase/sdk/vagrant/supplier/oracle/oracle.init.sql.vm", new File(configDir, "oracle/oracle_init.sql"), context);
    }


    @Override
    public String getVersion() {
        return "12.1.0.1";
    }

    public String getZipFileBase() {
        if (getVersion().startsWith("12.")) {
            return "linuxamd64_12c_database";
        } else {
            throw new UnexpectedLiquibaseSdkException("Unsupported oracle version: "+getVersion());
        }
    }

    public String getInstallDir() {
        return "/opt/oracle";
    }

    public String getOracleHome() {
        if (getVersion().startsWith("12.")) {
            return getInstallDir()+getFileSeparator()+"12c";
        } else {
            throw new UnexpectedLiquibaseSdkException("Unsupported oracle version: "+getVersion());
        }
    }

    public String getGlobalName() {
        return "lqbase";
    }

    public String getSysPassword() {
        return getAdminPassword();
    }

    public String getSystemPassword() {
        return getAdminPassword();
    }

    public String getSid() {
        return "lqbase";
    }
    
   public String getFileSeparator() {
        return "/";
    }

    @Override
    public String getDescription() {
        return super.getDescription() +
                "SID: "+ getSid()+"\n"+
                "Oracle Base: "+getInstallDir()+"\n" +
                "Oracle Home: "+getOracleHome()+"\n"+
                "SYS User Password: "+getSysPassword()+"\n"+
                "SYSTEM User Password: "+getSystemPassword()+"\n"+
                "\n"+
                "REQUIRES: postgresql-"+getVersion()+"-windows-x64-binaries.zip. Download Win x86-64 archive from http://www.enterprisedb.com/products-services-training/pgbindownload\n"+

                "NOTE: You must manually download the oracle installation files into LIQUIBASE_HOME/sdk/vagrant/install-files/oracle/\n"+
                "      You can download the install files from http://www.oracle.com/technetwork/database/enterprise-edition/downloads/index.html with a free OTN account\n"+
                "      Expected files: "+getZipFileBase()+"_*.zip\n"+
                "\n"+
                "NOTE: For easier sqlplus usage, rlwrap is installed. See http://www.oraclealchemist.com/news/add-history-and-tab-completion-to-sqlplus/ for more information";
    }
}
