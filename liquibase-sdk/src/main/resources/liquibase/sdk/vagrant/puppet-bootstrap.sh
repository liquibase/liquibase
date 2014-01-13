#!/bin/sh

if [ ! -e /tmp/epel-release-6-8.noarch.rpm ]; then
    wget -P /tmp http://www.mirrorservice.org/sites/dl.fedoraproject.org/pub/epel/6/i386/epel-release-6-8.noarch.rpm;
    rpm -Uvh /tmp/epel-release-6-8.noarch.rpm;
fi

if ! gem list librarian-puppet -i > /dev/null 2>&1; then
    echo "Installing librarian-puppet"
    gem install librarian-puppet --no-rdoc --no-ri
fi

echo "Running librarian-puppet install"
mkdir -p /etc/puppet
cd /etc/puppet
cp /vagrant/Puppetfile .
librarian-puppet install

echo 'Copying vagrant-install-files...'
mkdir -p /install
cp -rn /vagrant-install-files/* /install

echo "puppet-boostrap.sh done"