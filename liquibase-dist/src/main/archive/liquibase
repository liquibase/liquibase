#!/usr/bin/env bash

if [ ! -n "${LIQUIBASE_HOME+x}" ]; then
  # echo "LIQUIBASE_HOME is not set."

  ## resolve links - $0 may be a symlink
  PRG="$0"
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
    else
    PRG=`dirname "$PRG"`"/$link"
    fi
  done


  LIQUIBASE_HOME=`dirname "$PRG"`

  # make it fully qualified
  LIQUIBASE_HOME=`cd "$LIQUIBASE_HOME" && pwd`
  # echo "Liquibase Home: $LIQUIBASE_HOME"
fi

if [ -z "${JAVA_HOME}" ]; then
  #JAVA_HOME not set, try to find a bundled version
  if [ -d "${LIQUIBASE_HOME}/jre" ]; then
    JAVA_HOME="$LIQUIBASE_HOME/jre"
  elif [ -d "${LIQUIBASE_HOME}/.install4j/jre.bundle/Contents/Home" ]; then
    JAVA_HOME="${LIQUIBASE_HOME}/.install4j/jre.bundle/Contents/Home"
  fi
else
  if [ ! -x "$JAVA_HOME" ] ; then
    echo "ERROR: The JAVA_HOME environment variable is not defined correctly, so Liquibase cannot be started. JAVA_HOME is set to \"$JAVA_HOME\" and it does not exist." >&2
    exit 1
  fi
fi

if [ -z "${JAVA_HOME}" ]; then
  JAVA_PATH="$(which java)"

  if [ -z "${JAVA_PATH}" ]; then
    echo "Cannot find java in your path. Install java or use the JAVA_HOME environment variable"
    exit 1
  fi
else
    #Use path in JAVA_HOME
    JAVA_PATH="${JAVA_HOME}/bin/java"
fi

# add any JVM options here
JAVA_OPTS="${JAVA_OPTS-}"

export LIQUIBASE_HOME
"${JAVA_PATH}" $JAVA_OPTS -jar "$LIQUIBASE_HOME/internal/lib/liquibase-core.jar" ${1+"$@"}
