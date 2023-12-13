#!/bin/sh

# Needed when restarting the terminal
# asd
export LIQUIBASE_HOME=/opt/liquibase
export PATH=$PATH:$LIQUIBASE_HOME
exec $SHELL
