#!/usr/bin/env bash

###################################################################
## This script creates the installer files given an unziped directory
###################################################################

set -e
#set -x

if [ -z ${1+x} ]; then
  echo "This script requires the version to be passed to it. Example: package-install4j.sh 4.5.0";
  exit 1;
fi
version=$1

if [ -z ${INSTALL4J_LICENSE+x} ]; then
  echo "INSTALL4J_LICENSE must be set";
  exit 1
fi


mkdir -p ~/.install4j8
export INSTALL4J_CACHE=$HOME/.install4j8

install4jc="/usr/local/bin/install4jc"

if [ -f "$install4jc" ]; then
    echo "$install4jc already exists"
else
  echo "$install4jc does not exist. Installing..."
  wget -nv --directory-prefix=$INSTALL4J_CACHE -nc https://download-gcdn.ej-technologies.com/install4j/install4j_linux_8_0_11.deb
  sudo apt install -y $INSTALL4J_CACHE/install4j_linux_8_0_11.deb
fi

INSTALL4J_ARGS="$INSTALL4J_ARGS --release=$version -D liquibaseVersion=$version"

if [ ! -e target/keys ]; then
  echo "WARNING: not signing installer because target/keys directory does not exist."
  INSTALL4J_ARGS="$INSTALL4J_ARGS --disable-signing"
else
  INSTALL4J_ARGS="$INSTALL4J_ARGS --win-keystore-password=$INSTALL4J_WINDOWS_KEY_PASSWORD --mac-keystore-password=$INSTALL4J_APPLE_KEY_PASSWORD"
fi

"$install4jc" --license=$INSTALL4J_LICENSE
"$install4jc" $INSTALL4J_ARGS src/main/install4j/liquibase.install4j
