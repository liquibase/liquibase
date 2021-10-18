#!/usr/bin/env bash

###################################################################
## This script takes updates the target/liquibase-0-SNAPSHOT.tar.gz to have the given version
###################################################################

set -e
set -x

if [ -z ${1+x} ]; then
  echo "This script requires the version to be passed to it. Example: reversion.sh 4.5.0";
  exit 1;
fi

version=$1
workdir=$(pwd)/target/reversion
original_tgz=$(pwd)/target/liquibase-0-SNAPSHOT.tar.gz
original_zip=$(pwd)/target/liquibase-0-SNAPSHOT.zip

final_tar=$(pwd)/target/liquibase-$version.tar
final_zip=$(pwd)/target/liquibase-$version.zip
echo "Re-Versioning $original_tgz & $original_zip to $version...";

rm -rf $workdir
mkdir -p $workdir
cp $original_tgz $final_tar.gz
gzip -df $final_tar.gz

cp $original_zip $final_zip

#### Update liquibase.jar
tar -xf $final_tar -C $workdir liquibase.jar

## Fix up MANIFEST.MF
unzip -q $workdir/liquibase.jar META-INF/MANIFEST.MF -d $workdir
(cd src/main/package && javac ManifestReversion.java)
java -cp src/main/package ManifestReversion $workdir/META-INF/MANIFEST.MF $version
(cd $workdir && jar -uf liquibase.jar META-INF/MANIFEST.MF)

## Fix up pom.xml
unzip -q $workdir/liquibase.jar META-INF/maven/org.liquibase/liquibase-core/pom.xml -d $workdir
sed -i -e "s/<version>0-SNAPSHOT<\/version>/<version>$version<\/version>/" $workdir/META-INF/maven/org.liquibase/liquibase-core/pom.xml
(cd $workdir && jar -uf liquibase.jar META-INF/maven/org.liquibase/liquibase-core/pom.xml)

## Fix up liquibase.build.properties
unzip -q $workdir/liquibase.jar liquibase.build.properties -d $workdir
sed -i -e "s/build.version=.*/build.version=$version/" $workdir/liquibase.build.properties
(cd $workdir && jar -uf liquibase.jar liquibase.build.properties)

##TODO: SIGN JAR

##Save versioned liquibase.jar
cp $workdir/liquibase.jar target/liquibase-$version.jar

## Update final tar/zip with liquibase.jar
(cd $workdir && tar -uf $final_tar liquibase.jar)
(cd $workdir && jar -uf $final_zip liquibase.jar)

rm -rf target/liquibase-$version
mkdir target/liquibase-$version
tar xf $final_tar -C target/liquibase-$version

#### Re-gzip final tar
gzip $final_tar

