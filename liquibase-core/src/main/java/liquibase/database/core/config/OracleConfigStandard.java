package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

public class OracleConfigStandard extends ConnectionConfiguration {
    @Override
    public String getDatabaseShortName() {
        return "oracle";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:oracle:thin:@" + getHostname() + ":1521:"+getDatabaseUsername();
    }

    @Override
    public Set<String> getPuppetModules() {
        Set<String> modules = super.getPuppetModules();
        modules.add("biemond/oradb");
        return modules;
    }

    @Override
    public String getVagrantBoxName() {
        return "linux.centos.6_4";
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
    public String getPuppetInit(String box) {
        return "Package <| |> -> Oradb::Installdb <| |>\n"+
                "\n"+
                "oradb::installdb{ '"+ getVersion() +"_Linux-x86-64':\n" +
                "        version      => '"+ getVersion() +"',\n" +
                "        file         => 'linuxamd64_12c_database',\n" +
                "        databaseType => 'SE',\n" +
                "        oracleBase   => '"+ getOracleBase() +"',\n" +
                "        oracleHome   => '"+ getOracleHome() +"',\n" +
                "        user         => '"+ getInstallUsername() +"',\n" +
                "        group        => 'dba',\n" +
                "        downloadDir  => '/install/oracle/',\n" +
                "        puppetDownloadMntPoint  => '/install/oracle/'\n" +
                "}\n" +
                "\n" +
                "oradb::database{ '"+ getSID()+"':\n" +
                "                  oracleBase              => '"+getOracleBase()+"',\n" +
                "                  oracleHome              => '"+getOracleHome()+"',\n" +
                "                  version                 => '"+ getShortVersion() +"',\n" +
                "                  user                    => '"+getInstallUsername()+"',\n" +
                "                  group                   => 'dba',\n" +
                "                  downloadDir             => '/install/oracle/',\n" +
                "                  action                  => 'create',\n" +
                "                  dbName                  => '"+ getSID()+"',\n" +
                "                  dbDomain                => '"+ getDatabaseDomain()+"',\n" +
                "                  sysPassword             => '"+getSysPassword()+"',\n" +
                "                  systemPassword          => '"+ getSystemPassword() +"',\n" +
                "                  dataFileDestination     => '"+getOracleBase()+"/oradata',\n" +
                "                  recoveryAreaDestination => '"+getOracleBase()+"/flash_recovery_area',\n" +
                "                  characterSet            => '"+ getCharacterSet() +"',\n" +
                "                  nationalCharacterSet    => '"+ getNationalCharacterSet() +"',\n" +
                "                  initParams              => '"+ getInitParams() +"',\n" +
                "                  sampleSchema            => 'FALSE',\n" +
                "                  memoryPercentage        => '"+ getMemoryPercentage() +"',\n" +
                "                  memoryTotal             => '"+ getMemoryTotal() +"',\n" +
                "                  databaseType            => \"MULTIPURPOSE\",\n" +
                "                  require                 => Oradb::InstallDb['"+ getVersion() +"_Linux-x86-64'],\n" +
                "}\n" +
                "\n" +
                "oradb::listener{'start listener':\n" +
                "        oracleBase   => '"+getOracleBase()+"',\n" +
                "        oracleHome   => '"+getOracleHome()+"',\n" +
                "        user         => '"+getInstallUsername()+"',\n" +
                "        group        => 'dba',\n" +
                "        action       => 'start',\n" +
                "        require      => Oradb::Database['"+ getSID()+"'],\n" +
                "   }\n" +
                "\n" +
                "oradb::autostartdatabase{ 'autostart oracle':\n" +
                "                   oracleHome              => '"+getOracleHome()+"',\n" +
                "                   user                    => '"+getInstallUsername()+"',\n" +
                "                   dbName                  => '"+ getSID()+"',\n" +
                "                   require                 => Oradb::Database['"+ getSID()+"'],\n" +
                "}\n"+
                "file { '~/oracle-init.sh':\n" +
                "    require      => Oradb::Autostartdatabase['autostart oracle'],\n"+
                "    mode => '755',\n" +
                "    content => \"#!/bin/sh\n" +
                "\n" +
                "export ORACLE_HOME="+getOracleHome()+";\n" +
                "export ORACLE_SID="+ getSID()+";\n" +
                "echo \\\"create user "+ getDatabaseUsername()+" identified by "+ getDatabasePassword()+";\n" +
                "grant all privileges to "+ getDatabaseUsername()+";\n" +
                "create user "+getAlternateUsername()+" identified by "+getAlternateUserPassword()+";\n" +
                "grant all privileges to "+getAlternateUsername()+";\n" +
                "create tablespace "+ getAlternateTablespace() +" datafile '"+getOracleBase()+"/oradata/"+ getSID()+"/"+ getAlternateTablespace()+".dbf' SIZE 5M autoextend on next 5M;    \\\" | "+getOracleHome()+"/bin/sqlplus / as sysdba; \n" +
                "touch ~/database-init.ran;\n" +
                "\",\n" +
                "}\n"+
                "\n"+
                "exec { 'execute oracle setup scripts':\n" +
                "    require      => [File['~/database-init.sh'], Oradb::Autostartdatabase['autostart oracle']],\n"+
                "    path => ['/bin','/usr/bin'],\n" +
                "    command => '~/database-init.sh',\n" +
                "    user => '"+getInstallUsername()+"',\n" +
                "    creates => '~/database-init.ran',\n" +
                "}\n";
    }

    @Override
    public String getVersion() {
        return "12.1.0.1";
    }

    protected String getShortVersion() {
        return "12.1";
    }

    public String getMemoryTotal() {
        return "800";
    }

    public String getMemoryPercentage() {
        return "40";
    }

    public String getInitParams() {
        return "open_cursors=1000,processes=600,job_queue_processes=4";
    }

    public String getNationalCharacterSet() {
        return "UTF8";
    }

    public String getCharacterSet() {
        return "AL32UTF8";
    }

    public String getSystemPassword() {
        return "oracle";
    }

    public String getSysPassword() {
        return "oracle";
    }

    public String getDatabaseDomain() {
        return "liquibase.org";
    }

    public String getSID() {
        return "liquibase";
    }

    public String getInstallUsername() {
        return "oracle";
    }

    public String getOracleHome() {
        return "/oracle/product/"+ getShortVersion() +"/db";
    }

    public String getOracleBase() {
        return "/oracle";
    }

    @Override
    public String getDescription() {
        return super.getDescription() +
                "SID: "+getSID()+"\n"+
                "Oracle Base: "+getOracleBase()+"\n" +
                "Oracle Home: "+getOracleHome()+"\n"+
                "Database Domain: "+getDatabaseDomain()+"\n"+
                "Installer OS Username: "+getInstallUsername()+"\n"+
                "             Password: "+getInstallUsername()+"\n"+
                "SYS User Password: "+getSysPassword()+"\n"+
                "SYSTEM User Password: "+getSystemPassword()+"\n"+
                "Init Params: "+getInitParams()+"\n"+
                "Character Set: "+getCharacterSet()+"\n"+
                "National Character Set: "+getNationalCharacterSet()+"\n"+
                "\n"+
                "NOTE: You must manually download the oracle installation files into "+new File("vagrant/install-files/oracle").getAbsolutePath()+"\n"+
                "      You can download the install files from http://www.oracle.com/technetwork/database/enterprise-edition/downloads/index.html with a free OTN account\n"+
                "      Expected files: linuxamd64_12c_database_1of2.zip and linuxamd64_12c_database_2of2.zip\n"+
                "\n"+
                "NOTE: For easier sqlplus usage, rlwrap is installed. See http://www.oraclealchemist.com/news/add-history-and-tab-completion-to-sqlplus/ for more information";
    }
}
