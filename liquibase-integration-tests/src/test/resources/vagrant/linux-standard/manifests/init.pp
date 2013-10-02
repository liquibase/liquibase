package { "unzip":
    ensure => "installed"
}

resources { "firewall":
  purge => true
}

Firewall {
  before  => Class['my_firewall::post'],
  require => Class['my_firewall::pre'],
}

class { ['my_firewall::pre', 'my_firewall::post']: }

class { 'firewall': }

class { 'mysql::server':
  config_hash => {
	'root_password' => 'root',
	'bind_address'  => '0.0.0.0',	}
}

mysql::db { 'liquibase':
  user     => 'liquibase',
  password => 'liquibase',
  host     => '%',
  grant    => ['all'],
}

mysql::db { 'liquibaseb':
  user     => 'liquibase',
  password => 'liquibase',
  host     => '%',
  grant    => ['all'],
}


class { 'postgresql::server':
  config_hash => {
    'ip_mask_deny_postgres_user' => '0.0.0.0/32',
    'ip_mask_allow_all_users'    => '0.0.0.0/0',
    'listen_addresses'           => '*',
    'ipv4acls'                   => ['host all liquibase 0.0.0.0/0 password'],
    'postgres_password'          => 'postgres'
  },
}

postgresql::db { 'liquibase':
  user     => 'liquibase',
  password => 'liquibase'
}


$oracle_packages = [
"binutils",
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
]

package { $oracle_packages: ensure => "installed" }

oradb::installdb{ '12.1.0.1_Linux-x86-64':
        version      => '12.1.0.1',
        file         => 'linuxamd64_12c_database',
        databaseType => 'SE',
        oracleBase   => '/oracle',
        oracleHome   => '/oracle/product/12.1/db',
        user         => 'oracle',
        group        => 'dba',
        downloadDir  => '/install/oracle/',
        puppetDownloadMntPoint  => '/install/oracle/'
}

oradb::database{ 'liquibase':
                  oracleBase              => '/oracle',
                  oracleHome              => '/oracle/product/12.1/db',
                  version                 => "12.1",
                  user                    => 'oracle',
                  group                   => 'dba',
                  downloadDir             => '/install/oracle/',
                  action                  => 'create',
                  dbName                  => 'liquibase',
                  dbDomain                => 'liquibase.org',
                  sysPassword             => 'liquibase',
                  systemPassword          => 'liquibase',
                  dataFileDestination     => "/oracle/oradata",
                  recoveryAreaDestination => "/oracle/flash_recovery_area",
                  characterSet            => "AL32UTF8",
                  nationalCharacterSet    => "UTF8",
                  initParams              => "open_cursors=1000,processes=600,job_queue_processes=4",
                  sampleSchema            => 'FALSE',
                  memoryPercentage        => "40",
                  memoryTotal             => "800",
                  databaseType            => "MULTIPURPOSE",
                  require                 => Oradb::InstallDb['12.1.0.1_Linux-x86-64'],
}

 oradb::net{ 'config net8':
        oracleHome   => '/oracle/product/12.1/db',
        version      => "12.1",
        user         => 'oracle',
        group        => 'dba',
        downloadDir  => '/install/oracle/',
        require      => Oradb::Database['liquibase'],
   }

oradb::listener{'start listener':
        oracleBase   => '/oracle',
        oracleHome   => '/oracle/product/12.1/db',
        user         => 'oracle',
        group        => 'dba',
        action       => 'start',
        require      => Oradb::Net['config net8'],
   }

oradb::autostartdatabase{ 'autostart oracle':
                   oracleHome              => '/oracle/product/12.1/db',
                   user                    => 'oracle',
                   dbName                  => 'liquibase',
                   require                 => Oradb::Database['liquibase'],
}