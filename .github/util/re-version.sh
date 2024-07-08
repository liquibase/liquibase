#!/usr/bin/env bash

###################################################################
## This script updates all artifacts to have a given version
###################################################################

set -e
set -x

if [ -z ${1+x} ]; then
  echo "This script requires the path to unzipped liquibase-artifacts to be passed to it. Example: re-version.sh /path/to/liquibase-artifacts 4.24.0 release_4_24_0";
  exit 1;
fi

if [ -z ${2+x} ]; then
  echo "This script requires the version to be passed to it. Example: re-version.sh /path/to/liquibase-artifacts 4.24.0 release_4_24_0";
  exit 1;
fi

if [ -z ${3+x} ]; then
  echo "This script requires the name of the branch from which the version is released to be passed to it. Example: re-version.sh /path/to/liquibase-artifacts 4.24.0 release_4_24_0";
  exit 1;
fi

extension_name=${4:-}

if [ -n "$extension_name" ]; then
  echo "Extension name provided: $extension_name"
else
  echo "No extension name provided."
fi

## Check filesystem case sensitivity. Otherwise the unzip/zip of jars may overwrite files that only differ in case
touch case-test-abc
touch case-test-ABC
filesMade=$(ls case-test-* | wc -l)

if [ "$filesMade" == "2" ]; then
  echo "Case sensitive filesystem: OK"
else
  echo "re-version.sh requires a case sensitive filesystem"
  exit 1
fi

rm case-test-*

workdir=$(readlink -m $1)
version=$2
branch=$3
scriptDir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

outdir=$(pwd)/re-version/out

rm -rf outdir
mkdir -p $outdir

(cd $scriptDir && javac ManifestReversion.java)

#the branchName might have / in it. Hence replace it with _ as we do the same in build.yml file
MODIFIED_BRANCH_NAME=$(echo "$branch" | sed -e "s/[^a-zA-Z0-9-]/_/g")

#### Update  jars
declare -a jars=("liquibase-core-0-SNAPSHOT.jar" "liquibase-core-0-SNAPSHOT-sources.jar" "liquibase-commercial-$MODIFIED_BRANCH_NAME-SNAPSHOT.jar" "liquibase-commercial-$MODIFIED_BRANCH_NAME-SNAPSHOT-sources.jar" "liquibase-cdi-0-SNAPSHOT.jar" "liquibase-cdi-0-SNAPSHOT-sources.jar" "liquibase-cdi-jakarta-0-SNAPSHOT.jar" "liquibase-cdi-jakarta-0-SNAPSHOT-sources.jar" "liquibase-maven-plugin-0-SNAPSHOT.jar" "liquibase-maven-plugin-0-SNAPSHOT-sources.jar")

# If extension_name is specified, only reversion those jars
if [ -n "$extension_name" ]; then
  jars=("$extension_name-0-SNAPSHOT.jar" "$extension_name-0-SNAPSHOT-sources.jar")
fi

for jar in "${jars[@]}"
do
  ## MANIFEST.MF settings
  unzip -q $workdir/$jar META-INF/* -d $workdir

  java -cp $scriptDir ManifestReversion $workdir/META-INF/MANIFEST.MF $version
  find $workdir/META-INF -name pom.xml -exec sed -i -e "s/<version>0-SNAPSHOT<\/version>/<version>$version<\/version>/g" {} \;
  find $workdir/META-INF -name pom.xml -exec sed -i -e "s/<version>$MODIFIED_BRANCH_NAME-SNAPSHOT<\/version>/<version>$version<\/version>/g" {} \;
  find $workdir/META-INF -name pom.properties -exec sed -i -e "s/0-SNAPSHOT/$version/g" {} \;
  find $workdir/META-INF -name plugin*.xml -exec sed -i -e "s/<version>0-SNAPSHOT<\/version>/<version>$version<\/version>/g" {} \;
  (cd $workdir && jar -uMf $jar META-INF)
  rm -rf $workdir/META-INF

  ## Fix up liquibase.build.properties
  if [[ $jar == "liquibase-core-0-SNAPSHOT.jar" || $jar == "liquibase-commercial-$MODIFIED_BRANCH_NAME-SNAPSHOT.jar" ]]; then
    unzip -q $workdir/$jar liquibase.build.properties -d $workdir
    sed -i -e "s/build.version=.*/build.version=$version/" $workdir/liquibase.build.properties
    (cd $workdir && jar -uf $jar liquibase.build.properties)
    rm "$workdir/liquibase.build.properties"

    ##TODO: update XSD
  fi
  
  ##rebuild jar to ensure META-INF manifest is correct
  rm -rf $workdir/finalize-jar
  mkdir $workdir/finalize-jar
  (cd $workdir/finalize-jar && jar xf $workdir/$jar)
  mv $workdir/finalize-jar/META-INF/MANIFEST.MF $workdir/tmp-manifest.mf
  (cd $workdir/finalize-jar && jar cfm $workdir/$jar $workdir/tmp-manifest.mf .)

  cp $workdir/$jar $outdir
  RENAME_SNAPSHOTS=$(ls "$outdir/$jar" | sed -e "s|$MODIFIED_BRANCH_NAME-SNAPSHOT|$version|g" -e "s|0-SNAPSHOT|$version|g")
  if [[ "$RENAME_SNAPSHOTS" != "$outdir/$jar" ]]; then
      mv -v "$outdir/$jar" "$RENAME_SNAPSHOTS"
  fi

done

#### Update  javadoc jars
declare -a javadocJars=("liquibase-core-0-SNAPSHOT-javadoc.jar" "liquibase-cdi-0-SNAPSHOT-javadoc.jar" "liquibase-cdi-jakarta-0-SNAPSHOT-javadoc.jar" "liquibase-maven-plugin-0-SNAPSHOT-javadoc.jar" "liquibase-commercial-$MODIFIED_BRANCH_NAME-SNAPSHOT-javadoc.jar")

# If extension_name is specified, only reversion those jars
if [ -n "$extension_name" ]; then
  javadocJars=("$extension_name-0-SNAPSHOT-javadoc.jar")
fi

for jar in "${javadocJars[@]}"
do
  mkdir $workdir/rebuild
  ls $workdir
  echo "debug"
  unzip -q $workdir/$jar -d $workdir/rebuild
  find $workdir/rebuild -name "*.html" -exec sed -i -e "s/0-SNAPSHOT/$version/g" {} \;
  find $workdir/rebuild -name "*.xml" -exec sed -i -e "s/<version>0-SNAPSHOT<\/version>/<version>$version<\/version>/g" {} \;

  (cd $workdir/rebuild && jar -uf ../$jar *)
  rm -rf $workdir/rebuild

  cp $workdir/$jar $outdir
  RENAME_JAVADOC_SNAPSHOTS=$(ls "$outdir/$jar" | sed -e "s|$MODIFIED_BRANCH_NAME-SNAPSHOT|$version|g" -e "s|0-SNAPSHOT|$version|g")
  if [[ "$RENAME_JAVADOC_SNAPSHOTS" != "$outdir/$jar" ]]; then
    mv -v "$outdir/$jar" "$RENAME_JAVADOC_SNAPSHOTS"
  fi

done

## Test jar structure
for file in $outdir/*.jar
do

  ##Jars need MANIFEST.MF first in the file
  if jar -tf $file | grep "META-INF/MANIFEST.MF"; then
    ##only check if there is no MANIFEST.MF file
    secondLine=$(jar -tf $file | sed -n '2 p')

    if [ $secondLine == "META-INF/MANIFEST.MF" ]; then
      echo "$file has a correctly structured MANIFEST.MF entry"
    else
      echo "$file does not have MANIFEST.MF in the correct spot. Actual value: $secondLine"
      exit 1
    fi
  fi

  if [ -z "$extension_name" ]; then
    ##Make sure there are no left-over 0-SNAPSHOT versions in jar files
    mkdir -p $workdir/test
    unzip -q $file -d $workdir/test

    if grep -rl "0-SNAPSHOT" $workdir/test; then
      echo "Found '0-SNAPSHOT' in $file"
      exit 1
    fi

    if grep -rl "0.0.0.SNAPSHOT" $workdir/test; then
      echo "Found '0.0.0.SNAPSHOT' in $file"
      exit 1
    fi
  fi
  rm -rf $workdir/test
done

if [ -z "$extension_name" ]; then
  ##### update zip/tar files
  mkdir -p $workdir/internal/lib
  cp $outdir/liquibase-core-$version.jar $workdir/internal/lib/liquibase-core.jar ##save versioned jar as unversioned to include in zip/tar
  cp $outdir/liquibase-commercial-$version.jar $workdir/internal/lib/liquibase-commercial.jar ##save versioned jar as unversioned to include in zip/tar

  ## Extract tar.gz and rebuild it back into the tar.gz and zip
  mkdir $workdir/tgz-repackage
  tar -xzf $workdir/liquibase-$MODIFIED_BRANCH_NAME-SNAPSHOT.tar.gz -C $workdir/tgz-repackage --strip-components=1
  cp $workdir/internal/lib/liquibase-core.jar $workdir/tgz-repackage/internal/lib/liquibase-core.jar
  cp $workdir/internal/lib/liquibase-commercial.jar $workdir/tgz-repackage/internal/lib/liquibase-commercial.jar
  find $workdir/tgz-repackage -name "*.txt" -exec sed -i -e "s/0-SNAPSHOT/$version/" -e "s/release-SNAPSHOT/$version/" {} \;
  (cd $workdir/tgz-repackage && tar -czf $outdir/liquibase-$version.tar.gz *)
  (cd $workdir/tgz-repackage && zip -qr $outdir/liquibase-$version.zip *)
fi