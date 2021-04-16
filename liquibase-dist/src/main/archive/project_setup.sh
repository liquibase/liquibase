#!/bin/bash

current_time="$(date "+%Y.%m.%d-%H.%M.%S")"
PROPERTIES_FILE=""
JDBC_DRIVER_WGET=""
CHANGELOG_BODY="--liquibase formatted sql\n"
CHANGELOG_NAME="changelog.sql"

echo "PLATFORM TYPE: $1"
echo "hostname: $2"
echo "portnumber: $3"
echo "service name: $4"
echo "username: $5"
echo "password: $6"
echo "database: $7"
echo "project: $8"
sleep 10   

case $1 in

  sqlite)

    echo "sqlite"
    ;;

  Oracle | oracle | 0 )
   JDBC_DRIVER_WGET="lib/ojdbc8.jar https://repo1.maven.org/maven2/com/oracle/ojdbc/ojdbc8/19.3.0.0/ojdbc8-19.3.0.0.jar"
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: jdbc:oracle:thin:@$2:$3/$4\nusername: $5\npassword: $6\n"

   ;;

  postgres | postgresql)
    echo -n "Postgresql"
    ;;

   mongodb | Mongodb | 13)
   JDBC_DRIVER_WGET="lib/mongodb.jar https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/3.12.8/mongo-java-driver-3.12.8.jar"
   CHANGELOG_NAME=changelog.xml
   CHANGELOG_BODY="<databaseChangeLog
        xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"
        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
        xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\"
        xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.0.xsd
        http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">



</databaseChangeLog>"
   
   if mkdir -p ~/LB_WORKSPACE; then
        echo " "
   else
        echo " "
   fi

   if mkdir -p ~/LB_WORKSPACE/EXTENSIONS; then
        echo " "
   else
        echo " "
   fi
   EXTESNSION_LATEST_VERSION=$(curl -s https://github.com/liquibase/liquibase-mongodb/releases/latest | grep -o "mongodb-.*" | sed s/'>.*'//g | sed 's/"//g'| sed s/'mongodb-'//g)
   
   wget -q --no-verbose -O ~/LB_WORKSPACE/EXTENSIONS/liquibase-mongodb-${EXTESNSION_LATEST_VERSION}.jar https://github.com/liquibase/liquibase-mongodb/releases/download/liquibase-mongodb-${EXTESNSION_LATEST_VERSION}/liquibase-mongodb-${EXTESNSION_LATEST_VERSION}.jar
   PROPERTIES_FILE="changeLogFile: ${CHANGELOG_NAME}\nurl: mongodb://${DB_HOST}:${DB_PORT}/${DB_NAME}?authSource=admin\nusername: ${DB_USERNAME}\npassword: ${DB_PASSWORD}\nclasspath: ../EXTENSIONS/liquibase-mongodb-${EXTESNSION_LATEST_VERSION}.jar"
   
    ;;

  *)
    echo -n "unknown"
    ;;
esac
   if mkdir -p ~/LB_WORKSPACE; then
   	#echo "creating a Liquibase workspace folder 'LB_WORKSPACE' in your user root directory"
         echo " "
   else
   	#echo "Folder 'LB_WORKSPACE' already exists.  Creating a project folder '$PROJ_NAME'"
        echo " "
   fi
   mkdir -p ~/LB_WORKSPACE/$8
   wget -q --no-verbose -O $JDBC_DRIVER_WGET
   echo -e "$CHANGELOG_BODY" > ~/LB_WORKSPACE/$8/${CHANGELOG_NAME}
   echo -e "$PROPERTIES_FILE" > ~/LB_WORKSPACE/$8/liquibase.properties   
   echo "Here is your liquibase.properties file location:"
   tput setaf 3;echo $(dirname ~/LB_WORKSPACE/$8/liquibase.properties);tput sgr0
   echo "Here is your liquibase.properties file content:"
   tput setaf 2;cat ~/LB_WORKSPACE/$8/liquibase.properties;tput sgr0
   while true; do
   echo " "
   read -p "Would you like to connect to the database $DB_NAME (Y/N)? " yn;tput sgr0
   case $yn in
     [Yy]* ) break;;
     [Nn]* ) exit;;
     * ) echo "Please answer Y or N.";;
    esac
   done
   cd ~/LB_WORKSPACE/$8
   if liquibase history > /dev/null 2>&1; then
      echo "Connection was successful!"
   else
      echo "Please check the following errors: "
      liquibase history
      
   fi
   echo Your project $8 location is here:
   echo $(pwd)
