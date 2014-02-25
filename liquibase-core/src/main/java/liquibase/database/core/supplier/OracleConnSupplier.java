package liquibase.database.core.supplier;

import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.IOException;
import java.util.Arrays;
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
    public String getJdbcUrl() {
        return "jdbc:oracle:thin:@" + getIpAddress() + ":1521:"+getSid();
    }

    @Override
    public String getPrimaryCatalog() {
        return getDatabaseUsername();
    }

    @Override
    public String getAlternateCatalog() {
        return getAlternateUsername();
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
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        if (isWindows()) {
            return new ConfigTemplate("liquibase/sdk/vagrant/supplier/oracle/oracle-windows.puppet.vm", context);
        } else {
            return new ConfigTemplate("liquibase/sdk/vagrant/supplier/oracle/oracle-linux.puppet.vm", context);
        }
    }

    @Override
    public Set<ConfigTemplate> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigTemplate> configTemplates = super.generateConfigFiles(context);
        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/oracle/oracle_install.rsp.vm", context));
        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/oracle/oracle_netca.rsp.vm", context));
        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/oracle/oracle.init.sql.vm", context));

        return configTemplates;
    }


    @Override
    public String getVersion() {
        return "12.1.0.1";
    }

    public String getZipFileBase() {
        if (getVersion().startsWith("12.")) {
            if (isWindows()) {
                return "winx64_12c_database";
            } else {
                return "linuxamd64_12c_database";
            }
        } else {
            throw new UnexpectedLiquibaseSdkException("Unsupported oracle version: "+getVersion());
        }
    }

    public String getInstallDir() {
        if (isWindows()) {
            return "C:\\oracle-"+getVersion();
        } else {
            return "/opt/oracle";
        }
    }

    public String getOracleHome() {
        if (getVersion().startsWith("12.")) {
            return getInstallDir()+getFileSeparator()+"12c";
        } else {
            throw new UnexpectedLiquibaseSdkException("Unsupported oracle version: "+getVersion());
        }
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
    
    @Override
    public String getDescription() {
        return super.getDescription() +
                "SID: "+ getSid()+"\n"+
                "Oracle Base: "+getInstallDir()+"\n" +
                "Oracle Home: "+getOracleHome()+"\n"+
                "SYS User Password: "+getSysPassword()+"\n"+
                "SYSTEM User Password: "+getSystemPassword()+"\n"+
                "\n"+
                "REQUIRED: You must manually download the oracle installation files into LIQUIBASE_HOME/sdk/vagrant/install-files/oracle/\n"+
                "      You can download the install files from http://www.oracle.com/technetwork/database/enterprise-edition/downloads/index.html with a free OTN account\n"+
                "      Expected files: "+getZipFileBase()+"_*.zip\n"+
                "\n"+
                "NOTE: For easier sqlplus usage, rlwrap is installed. See http://www.oraclealchemist.com/news/add-history-and-tab-completion-to-sqlplus/ for more information";
    }
}
