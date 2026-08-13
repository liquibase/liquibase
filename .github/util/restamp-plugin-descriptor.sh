#!/usr/bin/env bash

# Rewrites the Maven descriptor embedded in a plugin jar so its version matches the coordinate
# the jar is published under. Maven refuses to run a plugin whose plugin.xml disagrees with the
# requested version ("Plugin's descriptor contains the wrong version"), which regular jars never
# hit because only plugins validate their descriptor.
#
# usage: restamp-plugin-descriptor.sh <jar> <from-version> <to-version>

set -euo pipefail

jar=$(cd "$(dirname "$1")" && pwd)/$(basename "$1")
from=$2
to=$3

workdir=$(mktemp -d)
trap 'rm -rf "$workdir"' EXIT

unzip -q "$jar" 'META-INF/*' -d "$workdir"

find "$workdir/META-INF" -name 'plugin*.xml' -exec sed -i.bak "s|<version>$from</version>|<version>$to</version>|g" {} \;
find "$workdir/META-INF" -name 'pom.xml' -exec sed -i.bak "s|<version>$from</version>|<version>$to</version>|g" {} \;
find "$workdir/META-INF" -name 'pom.properties' -exec sed -i.bak "s|$from|$to|g" {} \;
find "$workdir/META-INF" -name '*.bak' -delete

(cd "$workdir" && jar -uMf "$jar" META-INF)
