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