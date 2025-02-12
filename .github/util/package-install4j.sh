#!/usr/bin/env bash

###################################################################
## This script installs Install4j and configures it for creating 
## unsigned installers
###################################################################

set -e

# Check required arguments and environment variables
if [ -z "${1+x}" ]; then
    echo "Error: Version argument required. Usage: package-install4j.sh <version>"
    exit 1
fi
version=$1

if [ -z "${INSTALL4J_LICENSE_KEY+x}" ]; then
    echo "Error: INSTALL4J_LICENSE_KEY must be set"
    exit 1
fi

# Define constants
INSTALL4J_VERSION="10_0_9"
INSTALL4J_CACHE="$HOME/.install4j10"
install4jc="/usr/local/bin/install4jc"

# Create cache directory
mkdir -p "$INSTALL4J_CACHE"

# Install Install4j if not present
if [ -f "$install4jc" ]; then
    echo "Install4j is already installed at $install4jc"
else
    echo "Installing Install4j..."
    INSTALL4J_URL="https://download.ej-technologies.com/install4j/install4j_linux-x64_${INSTALL4J_VERSION}.deb"
    wget -nv --directory-prefix="$INSTALL4J_CACHE" -nc "$INSTALL4J_URL"
    sudo apt install -y "$INSTALL4J_CACHE/install4j_linux-x64_${INSTALL4J_VERSION}.deb"
    rm -f "$INSTALL4J_CACHE/install4j_linux-x64_${INSTALL4J_VERSION}.deb"
fi

# Configure and run Install4j
INSTALL4J_ARGS="-L $INSTALL4J_LICENSE_KEY --release=$version -D liquibaseVersion=$version -D install4j.logToStderr=true --disable-signing"
"$install4jc" $INSTALL4J_ARGS src/main/install4j/liquibase.install4j