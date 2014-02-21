package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.io.IOException;
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
        return "jdbc:postgresql://"+ getIpAddress() +"/"+getPrimaryCatalog();
    }

    @Override
    public Set<String> getPuppetModules() {
        Set<String> modules = super.getPuppetModules();
        modules.add("puppetlabs/postgresql");
        return modules;
    }

    @Override
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        return new ConfigTemplate("liquibase/sdk/vagrant/supplier/postgresql/postgresql-linux.puppet.vm", context);
    }


    @Override
    public Set<ConfigTemplate> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigTemplate> configTemplates = super.generateConfigFiles(context);

        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/postgresql/postgresql.init.sql.vm", context));
        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/postgresql/postgresql.conf.vm", context));
        configTemplates.add(new ConfigTemplate("liquibase/sdk/vagrant/supplier/postgresql/pg_hba.conf.vm", context));

        return configTemplates;
    }
}
