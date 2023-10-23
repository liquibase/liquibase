#!/usr/bin/env bash

###################################################################
## This script creates the installer files given an unzipped directory
###################################################################

set -e
#set -x

if [ -z ${1+x} ]; then
  echo "This script requires the version to be passed to it. Example: package-install4j.sh 4.5.0";
  exit 1;
fi
version=$1

if [ -z ${INSTALL4J_10_LICENSE+x} ]; then
  echo "INSTALL4J_10_LICENSE must be set";
  exit 1
fi


mkdir -p ~/.install4j10
export INSTALL4J_CACHE=$HOME/.install4j10

# install4jc="/usr/local/bin/install4jc"
install4jc="/Applications/install4j.app/Contents/Resources/app/bin/install4jc"

if [ -f "$install4jc" ]; then
    echo "$install4jc already exists"
else
  echo "$install4jc does not exist. Installing..."

  # installer automation for ubuntu-latest; replaced
  # wget -nv --directory-prefix=$INSTALL4J_CACHE -nc https://download.ej-technologies.com/install4j/install4j_linux-x64_10_0_6.deb
  # sudo apt install -y $INSTALL4J_CACHE/install4j_linux-x64_10_0_6.deb

  # installer automation for macos-latest; macos needed for apple notarizing
  wget -nv --directory-prefix=$INSTALL4J_CACHE -nc https://download.ej-technologies.com/install4j/install4j_macos_10_0_6.dmg
  sleep 5
  hdiutil attach /Users/runner/.install4j10/install4j_macos_10_0_6.dmg
  sleep 5
  cp -rf /Volumes/install4j/install4j.app /Applications
  sleep 5
  hdiutil detach /Volumes/install4j

fi

INSTALL4J_ARGS="$INSTALL4J_ARGS --release=$version -D liquibaseVersion=$version -D install4j.logToStderr=true"

if [ ! -e target/keys ]; then
  echo "WARNING: not signing installer because target/keys directory does not exist."
  INSTALL4J_ARGS="$INSTALL4J_ARGS --disable-signing"
else
  INSTALL4J_ARGS="$INSTALL4J_ARGS --win-keystore-password=$INSTALL4J_WINDOWS_KEY_PASSWORD --mac-keystore-password=$INSTALL4J_APPLE_KEY_PASSWORD --apple-id=$INSTALL4J_APPLE_ID --apple-id-password=$INSTALL4J_APPLE_ID_PASSWORD"
fi

"$install4jc" --license=$INSTALL4J_10_LICENSE
"$install4jc" $INSTALL4J_ARGS src/main/install4j/liquibase.install4j

