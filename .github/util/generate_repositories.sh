#!/usr/bin/env bash
# generate_repositories.sh generates the repositories.json file for the GitHub Maven Package Registry. It is usedby whelk-io/maven-settings-xml-action
IFS=',' read -ra EXT <<< "$EXTENSIONS"
echo "["
for i in "${EXT[@]}"; do
  echo "{
    \"id\": \"$i\",
    \"url\": \"https://maven.pkg.github.com/liquibase/$i\",
    \"releases\": {
      \"enabled\": \"true\"
    },
    \"snapshots\": {
      \"enabled\": \"true\",
      \"updatePolicy\": \"always\"
    }
  },"
done
echo "]"