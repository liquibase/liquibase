#!/usr/bin/env bash

###################################################################
## This script signs the release artifacts
###################################################################

set -e
set -x

if [ -z ${1+x} ]; then
  echo "This script requires the directory to be passed to it. Example: sign-artifacts.sh artifacts";
  exit 1;
fi

archiveDir=$1

if [ -z ${GPG_PASSWORD+x} ]; then
  echo "GPG_PASSWORD must be set";
  exit 1
fi

declare -a file_patterns=("*.jar" "*-installer-*" "*.zip" "*.tar.gz")

if [[ "$OSTYPE" != "linux-gnu"* ]]; then
  # install md5sum and sha1sum on macos-latest
  brew install md5sha1sum

  ##Fix files with Gnu-sed on macos-latest
  brew install gnu-sed

  # sha256sum is not included in md5sha1sum; use shasum -a 256 as fallback on macOS
  if ! command -v sha256sum &> /dev/null; then
    sha256sum() { shasum -a 256 "$@"; }
    export -f sha256sum
  fi
fi

for file_pattern in "${file_patterns[@]}"
do
  echo "Searching for $file_pattern files..."
  for i in `find $archiveDir -name $file_pattern -maxdepth 1`; do
    echo "Signing $i"
    rm -f $i.asc
    rm -f $i.md5
    rm -f $i.sha1
    rm -f $i.sha256

    gpg --batch --pinentry-mode=loopback --passphrase "$GPG_PASSWORD" -ab $i
    sleep 5
    md5sum < $i > $i.md5
    sha1sum < $i > $i.sha1
    sha256sum < $i > $i.sha256
  done
done

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  sed -i 's/ -//' $archiveDir/*.md5
  sed -i 's/ -//' $archiveDir/*.sha1
  sed -i 's/ -//' $archiveDir/*.sha256
else
  gsed -i 's/ -//' $archiveDir/*.md5
  gsed -i 's/ -//' $archiveDir/*.sha1
  gsed -i 's/ -//' $archiveDir/*.sha256
fi

# sed -i 's/ -//' $archiveDir/*.md5
# sed -i 's/ -//' $archiveDir/*.sha1

