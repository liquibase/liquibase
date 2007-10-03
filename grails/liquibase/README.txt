The LiquiBase Grails plugin is designed to allow ActiveRecord::Migration-like functionality via the LiquiBase project.

The following commands are currently available:
liquibase-migrate
liquibase-migrateSQL
liquibase-tag <tag>
liquibase-rollback <tag>
liquibase-rollbackCount <num>
liquibase-rollbackSQL <tag>
liquibase-rollbackCountSQL <num>

Set up your database using the standard grails-app/conf/DataSource.groovy file

Add your database changes to the grails-app/migrations/changelog.xml file

A starting changelog.xml is:

<?xml version="1.0" encoding="UTF-8"?>

<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog/1.3"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog/1.3 http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-1.3.xsd">

</databaseChangeLog>

For more information on the LiquiBase project, see http://www.liquibase.org