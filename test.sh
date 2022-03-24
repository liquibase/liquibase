#!/usr/bin/env bash

touch case-test-abc
touch case-test-ABC
filesMade=$(ls case-test-* | wc -l)

if [ "$filesMade" == "2" ]; then
  echo "Case sensitive filesystem: OK"
else
  echo "re-version.sh requires a case sensitive filesystem due to proguard"
  exit 1
fi

rm case-test-*
