# Liquibase Vagrant Database Servers

You can easily create databases for testing Liquibase using the vagrant configurations in this directory

## Conventions

### install-files

The install-files directory is where vagrant looks to find non-internet accessible install files.
For example, to install Oracle, download the oracle zip files and place them in install-files/oracle

### Network Setup

The vagrant boxes are configured to create host-only IPs in the 10.10.100.X range. Firewalls are disabled since they are host-only

- 10.10.100.100: linux-standard

### Standard Database Setup

- Most tests and scripts assume the database is accessible with a username of "liquibase" and a password of "liquibase".
If that is combination is not supported on a database, something as close as possible will be used

- Most tests and scripts assume a catalog/database called "liquibase" is created
- Most tests and scripts assume a schema called "liquibase" is created if the database supports it

### Provisioning

Provisioning in Vagrant is handed primarily by puppet, although there is a shell provisioning that is done first to bootstrap anything that cannot be done in puppet.

The starting puppet file will be manifests/init.pp within each vagrant config. For example, linux-standard/manifests/init.pp

## Vagrant Boxes

### linux-standard

Linux-standard is the only vagrant box currently configured. It is a 64 bit CentOS 6.4 server.
It will install:

- Mysql
- Postgres
- Oracle

## Database Configuration

### Oracle

To install Oracle, download the installation file(s) from http://www.oracle.com/technetwork/database/enterprise-edition/downloads/index.html and place them in install-files/oracle

Oracle Enterprise Manager can be downloaded from http://www.oracle.com/technetwork/oem/enterprise-manager/downloads/oem-windows64-download-1619155.html and installed on your workstation