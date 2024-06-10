#!/usr/bin/env bash
# generate_servers.sh generates the servers.json file for the GitHub Maven Package Registry. It is used by whelk-io/maven-settings-xml-action
IFS=',' read -ra EXT <<< "$EXTENSIONS"
echo "["
for i in "${EXT[@]}"; do
  echo "{
    \"id\": \"$i\",
    \"username\": \"liquibot\",
    \"password\": \"$1\"
  },"
done
echo "]"