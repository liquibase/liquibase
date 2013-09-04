include apt

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


#file { "puppet:///modules/oradata":
#    mode => "0644",
#    source => '/vagrant-install-files/oracle',
#}

#file { "/etc/puppet/modules/oradb":
#    source => '/vagrant/modules/oradb',
#    recurse => true
#}

oradb::installdb{ '12.1.0.1_Linux-x86-64':
        version      => '12.1.0.1',
        file         => 'linuxamd64_12c_database',
        databaseType => 'SE',
        oracleBase   => '/oracle',
        oracleHome   => '/oracle/product/12.1/db',
        user         => 'oracle',
        group        => 'dba',
        downloadDir  => '/oracle/install',
        puppetDownloadMntPoint  => '/tmp/oracle/'
 }


oradb::database{ 'liquibase':
                  oracleBase              => '/oracle',
                  oracleHome              => '/oracle/product/12.1/db',
                  version                 => "12.1",
                  user                    => 'oracle',
                  group                   => 'dba',
                  downloadDir             => '/install',
                  action                  => 'create',
                  dbName                  => 'oracle',
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
                  require                 => Oradb::Listener['start listener'],
}

oradb::listener{'start listener':
        oracleBase   => '/oracle',
        oracleHome   => '/oracle/product/12.1/db',
        user         => 'oracle',
        group        => 'dba',
        action       => 'start',
     }
#