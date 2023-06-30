#!/bin/sh

# Needed when restarting the terminal
export LIQUIBASE_HOME=/opt/liquibase
export PATH=$PATH:$LIQUIBASE_HOME
exec $SHELL