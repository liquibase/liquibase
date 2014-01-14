package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

import java.util.Arrays;
import java.util.Set;

public class DB2ConfigStandard extends ConnectionConfiguration {
    @Override
    public String getDatabaseShortName() {
        return "db2";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:db2://"+ getHostname() +":50000/lqbase";
    }

    @Override
    public String getVagrantBoxName() {
        return "linux.centos.6_4";
    }

    @Override
    public Set<String> getRequiredPackages(String vagrantBoxName) {
        Set<String> requiredPackages = super.getRequiredPackages(vagrantBoxName);
        requiredPackages.addAll(Arrays.asList("compat-libstdc++-33", "pam.i686", "numactl"));

        return requiredPackages;
    }

    @Override
    public String getPuppetInit(String box) {
        return "Package <| |> -> Exec['unzip db2']\n" +
                "\n" +
                "exec {'unzip db2':\n" +
                "    command     => '/bin/tar xfzv /install/db2/*expc.tar.gz',\n"+
                "    cwd     => '/install/db2/',\n" +
                "    creates  => '/install/db2/expc/',\n" +
                "    path    => ['/usr/bin', '/usr/sbin', '/bin'],\n" +
                "}\n" +
                "\n" +
                "exec {'/install/db2/expc/db2setup -r /install/db2/db2expc.rsp':\n"+
                "     require     => [Exec['unzip db2'], User['liquibase'],\n"+
                "     cwd     => '/install/db2/expc',\n"+
                "     creates  => '/opt/ibm/db2/',\n" +
                "     path    => ['/usr/bin', '/usr/sbin', '/bin'],\n" +
                "}\n";
    }

    //    @Override
//    public Set<String> getRequiredPackages() {
//        Set<String> requiredPackages = super.getRequiredPackages();
//        requiredPackages.addAll(Arrays.asList("lib32stdc++6"));
//
//        return requiredPackages;
//    }

}
