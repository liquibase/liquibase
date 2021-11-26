#!/usr/bin/env bash

###################################################################
## This script takes updates the target/liquibase-0-SNAPSHOT.tar.gz to have the given version
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

workdir=$1
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
  find $workdir/META-INF -name plugin.xml -exec sed -i -e "s/<version>0-SNAPSHOT<\/version>/<version>$version<\/version>/" {} \;
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

  cp $workdir/$jar $outdir
  rename.ul 0-SNAPSHOT $version $outdir/$jar
done

#### Update  javadoc jars
declare -a javadocJars=("liquibase-0-SNAPSHOT-javadoc.jar" "liquibase-cdi-0-SNAPSHOT-javadoc.jar" "liquibase-maven-plugin-0-SNAPSHOT-javadoc.jar")

for jar in "${javadocJars[@]}"
do
  mkdir $workdir/rebuild
  unzip -q $workdir/$jar -d $workdir/rebuild

  find $workdir/rebuild -name *.html -exec sed -i -e "s/0-SNAPSHOT/$version/" {} \;
  (cd $workdir/rebuild && jar -uf ../$jar *)
  rm -rf $workdir/rebuild

  cp $workdir/$jar $outdir
  rename.ul 0-SNAPSHOT $version $outdir/$jar
done

##### update zip/tar files
cp $outdir/liquibase-$version.jar $workdir/liquibase.jar ##save versioned jar as unversioned to include in zip/tar

cp $workdir/liquibase-0-SNAPSHOT.zip $outdir/liquibase-$version.zip
cp $workdir/liquibase-0-SNAPSHOT.tar.gz $outdir/liquibase-$version.tar.gz

gzip -df $outdir/liquibase-$version.tar.gz
(cd $workdir && tar -uf $outdir/liquibase-$version.tar liquibase.jar)
(cd $workdir && jar -uf $outdir/liquibase-$version.zip liquibase.jar)

gzip $outdir/liquibase-$version.tar

##### Rebuild installers
mkdir -p liquibase-dist/target/liquibase-$version
(cd liquibase-dist/target/liquibase-$version && tar xfz $outdir/liquibase-$version.tar.gz)
(cd liquibase-dist && $scriptDir/package-install4j.sh $version)

##Sign Files
$scriptDir/sign-artifacts.sh $outdir


