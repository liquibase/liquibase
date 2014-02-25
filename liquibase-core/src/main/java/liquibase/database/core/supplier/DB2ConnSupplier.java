package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class DB2ConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "db2";
    }

    @Override
    public String getAdminUsername() {
        return null;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:db2://"+ getIpAddress() +":50000/lqbase";
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

    //    @Override
//    public Set<String> getRequiredPackages() {
//        Set<String> requiredPackages = super.getRequiredPackages();
//        requiredPackages.addAll(Arrays.asList("lib32stdc++6"));
//
//        return requiredPackages;
//    }

}
