function _liquibase(){
   local state
   _arguments '1: :->commands' '2: :->attributes' '3: :->checks_attributes'
  
   case $state in
   commands)
      compadd -Q checks --help update updateSQL updateCount updateCountSQL updateToTag updateToTagSQL status rollback rollbackSQL 'rollbackOneChangeSet --changeSetAuthor --changeSetId --changeSetPath --force' 'rollbackOneChangeSetSQL  --changeSetAuthor --changeSetId --changeSetPath' 'rollbackOneUpdate --deploymentId --force' 'rollbackOneUpdateSQL --deploymentId' rollbackToDate rollbackToDateSQL rollbackCount rollbackCountSQL futureRollbackSQL futureRollbackFromTagSQL updateTestingRollback generateChangeLog snapshot snapshotReference diff diffChangeLog dbDoc history tag tagExists status unexpectedChangeSets validate calculateCheckSum clearCheckSums changelogSync changelogSyncSQL changeLogSyncToTag changeLogSyncToTagSQL markNextChangeSetRan markNextChangeSetRanSQL listLocks releaseLocks dropAll
      ;;
   attributes)
      compadd -Q show run customize enable disable delete reset bulk-set --changeLogFile --force --format --username --password --url --classpath --driver --databaseClass --propertyProviderClass --defaultSchemaName --contexts --labels --defaultsFile --delimiter --driverPropertiesFile --changeExecListenerClass --changeExecListenerPropertiesFile --liquibaseCatalogName --liquibaseSchemaName --databaseChangeLogTableName --databaseChangeLogLockTableName --databaseChangeLogTablespaceName --liquibaseSchemaName --includeSystemClasspath --overwriteOutputFile --promptForNonLocalDatabase --logLevel --logFile --currentDateTimeFunction --outputDefaultSchema --outputDefaultCatalog --outputFile --rollbackScript --excludeObjects --includeObjects --help --version --snapshotFormat --referenceUsername --referencePassword --referenceUrl --defaultCatalogName --defaultSchemaName --referenceDefaultCatalogName --referenceDefaultSchemaName --schemas --referenceSchemas --outputSchemaAs --includeCatalog --includeSchema --includeTablespace --referenceDriver --dataOutputDirectory --diffTypes --diffTypes=catalog,tables,functions,views,columns,indexes,foreignkeys,primarykeys,uniqueconstraints,data,storedprocedure,triggers,sequences --verbose --liquibaseProLicenseKey
         ;;
   checks_attributes)
      compadd -Q --dummy --check-name --checks-settings-file --format
         ;;
   esac
}
compdef _liquibase liquibase
