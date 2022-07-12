#!/usr/bin/env bash

###################################################################
## This script updates all artifacts to have a given version
###################################################################

set -e
set -x

if [ -z ${1+x} ]; then
  echo "This script requires the path to unzipped liquibase-artifacts to be passed to it. Example: re-version.sh /path/to/liquibase-artifacts 4.5.0";
  exit 1;
fi

if [ -z ${2+x} ]; then
  echo "This script requires the version to be passed to it. Example: re-version.sh /path/to/liquibase-artifacts 4.5.0";
  exit 1;
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
scriptDir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

outdir=$(pwd)/re-version/out

rm -rf outdir
mkdir -p $outdir

(cd $scriptDir && javac ManifestReversion.java)

#### Update  jars
declare -a jars=("liquibase-0-SNAPSHOT.jar" "liquibase-0-SNAPSHOT-sources.jar" "liquibase-cdi-0-SNAPSHOT.jar" "liquibase-cdi-0-SNAPSHOT-sources.jar" "liquibase-maven-plugin-0-SNAPSHOT.jar" "liquibase-maven-plugin-0-SNAPSHOT-sources.jar")

for jar in "${jars[@]}"
do
  ## MANIFEST.MF settings
  unzip -q $workdir/$jar META-INF/* -d $workdir

  java -cp $scriptDir ManifestReversion $workdir/META-INF/MANIFEST.MF $version
  find $workdir/META-INF -name pom.xml -exec sed -i -e "s/<version>0-SNAPSHOT<\/version>/<version>$version<\/version>/" {} \;
  find $workdir/META-INF -name pom.properties -exec sed -i -e "s/0-SNAPSHOT/$version/" {} \;
  find $workdir/META-INF -name plugin*.xml -exec sed -i -e "s/<version>0-SNAPSHOT<\/version>/<version>$version<\/version>/" {} \;
  (cd $workdir && jar -uMf $jar META-INF)
  rm -rf $workdir/META-INF

  ## Fix up liquibase.build.properties
  if [ $jar == "liquibase-0-SNAPSHOT.jar" ]; then
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
  rename.ul 0-SNAPSHOT $version $outdir/$jar
done

#### Update  javadoc jars
declare -a javadocJars=("liquibase-0-SNAPSHOT-javadoc.jar" "liquibase-cdi-0-SNAPSHOT-javadoc.jar" "liquibase-maven-plugin-0-SNAPSHOT-javadoc.jar")

for jar in "${javadocJars[@]}"
do
  mkdir $workdir/rebuild
  unzip -q $workdir/$jar -d $workdir/rebuild

  find $workdir/rebuild -name "*.html" -exec sed -i -e "s/0-SNAPSHOT/$version/" {} \;
  find $workdir/rebuild -name "*.xml" -exec sed -i -e "s/<version>0-SNAPSHOT<\/version>/<version>$version<\/version>/" {} \;

  (cd $workdir/rebuild && jar -uf ../$jar *)
  rm -rf $workdir/rebuild

  cp $workdir/$jar $outdir
  rename.ul 0-SNAPSHOT $version $outdir/$jar
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

  rm -rf $workdir/test
done


##### update zip/tar files
cp $outdir/liquibase-$version.jar $workdir/liquibase.jar ##save versioned jar as unversioned to include in zip/tar

## Extract tar.gz and rebuild it back into the tar.gz and zip
mkdir $workdir/tgz-repackage
(cd $workdir/tgz-repackage && tar -xzf $workdir/liquibase-0-SNAPSHOT.tar.gz)
cp $workdir/liquibase.jar $workdir/tgz-repackage/liquibase.jar
find $workdir/tgz-repackage -name "*.txt" -exec sed -i -e "s/0-SNAPSHOT/$version/" {} \;
(cd $workdir/tgz-repackage && tar -czf $outdir/liquibase-$version.tar.gz *)
(cd $workdir/tgz-repackage && zip -qr $outdir/liquibase-$version.zip *)
