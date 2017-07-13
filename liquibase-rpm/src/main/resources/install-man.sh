#!/bin/sh
echo "Installing man page (and run makewhatis, may take a few seconds) ..."
gzip -f /opt/liquibase/man/liquibase.1
mkdir -p /usr/man/man1
rm -f /usr/man/man1/liquibase.1.gz
cp /opt/liquibase/man/liquibase.1.gz /usr/man/man1/
echo "Running makewhatis ..."
makewhatis
echo "man page installed."
