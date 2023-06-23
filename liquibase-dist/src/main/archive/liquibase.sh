#!/bin/sh

export LIQUIBASE_HOME=/opt/liquibase
export PATH=$PATH:$LIQUIBASE_HOME
source /etc/profile.d/
exec $SHELL