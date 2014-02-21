package liquibase.database.core.supplier;

import liquibase.sdk.TemplateService;
import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class MSSQLConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "mssql";
    }

    @Override
    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    @Override
    public String getVagrantBaseBoxName() {
        return VAGRANT_BOX_NAME_WINDOWS_STANDARD;
    }

    @Override
    public String getAdminUsername() {
        return "sa";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:sqlserver://"+ getIpAddress() +":1433;databaseName="+getPrimaryCatalog();
    }

    @Override
    public String getPuppetInit(Map<String, Object> context) throws IOException {
        return TemplateService.getInstance().output("liquibase/sdk/vagrant/supplier/mssql/mssql.puppet.vm", context);
    }

    @Override
    public String getDescription() {
        return super.getDescription() +
                "REQUIRES: You must manually download the sql server express installation files into LIQUIBASE_HOME/sdk/vagrant/install-files/mssql/SQLEXPR_x64_ENU.exe\n"+
                "      You can download the install files from http://www.microsoft.com/en-us/sqlserver/get-sql-server/try-it.aspx#tab2\n"+
                "\n"+
                "NOTE: If Exec[mssql install] fails, you may need to remote desktop to the vagrant box and run the failed command locally. After running, re-run liquibase-sdk vagrant [BOX_NAME] provision. Watch the process manager for SQLEXPR_x64_ENU.exe to exit\n"+
                "\n";
    }

    @Override
    public Set<ConfigFile> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigFile> configFiles = super.generateConfigFiles(context);
        configFiles.add(new ConfigFile("liquibase/sdk/vagrant/supplier/mssql/mssql.init.sql.vm", context));

        return configFiles;
    }


}
