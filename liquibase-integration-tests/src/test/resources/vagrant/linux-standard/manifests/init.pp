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
