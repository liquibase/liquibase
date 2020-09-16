#/usr/bin/env bash

complete -W \
  "help \
  update \
  updateSQL \
  updateCount \
  updateCountSQL \
  updateToTag \
  updateToTagSQL \
  'status --verbose' \
  'registerChangeLog --changeLogFile=<changelogFile>' \
  'syncHub --changeLogFile=<changelogFile>' \
  rollback \
  rollbackSQL \
  'rollbackOneChangeSet --changeSetAuthor=<author> --changeSetId=<id> --changeSetPath=<changelogFile> --force' \
  'rollbackOneChangeSetSQL --changeSetAuthor=<author> --changeSetId=<id> --changeSetPath=<changelogFile>' \
  'rollbackOneUpdate --deploymentId=<deploymentId> --force' \
  'rollbackOneUpdateSQL --deploymentId=<deploymentId>' \
  rollbackToDate \
  rollbackToDateSQL \
  rollbackCount \
  rollbackCountSQL \
  futureRollbackSQL \
  futureRollbackFromTagSQL \
  updateTestingRollback \
  generateChangeLog \
  snapshot \
  snapshotReference \
  diff \
  diffChangeLog \
  dbDoc \
  history \
  tag \
  tagExists \
  status \
  unexpectedChangeSets \
  validate \
  calculateCheckSum \
  clearCheckSums \
  changelogSync \
  changelogSyncSQL \
  markNextChangeSetRan \
  markNextChangeSetRanSQL \
  listLocks \
  releaseLocks \
  dropAll \
  --changeLogFile \
  --username \
  --password \
  --url \
  --classpath \
  --driver \
  --databaseClass \
  --propertyProviderClass \
  --defaultSchemaName \
  --contexts \
  --labels \
  --defaultsFile \
  --delimiter \
  --driverPropertiesFile \
  --changeExecListenerClass \
  --changeExecListenerPropertiesFile \
  --liquibaseCatalogName \
  --liquibaseSchemaName \
  --databaseChangeLogTableName \
  --databaseChangeLogLockTableName \
  --databaseChangeLogTablespaceName \
  --liquibaseSchemaName \
  --includeSystemClasspath \
  --overwriteOutputFile \
  --promptForNonLocalDatabase \
  --logLevel \
  --logFile \
  --currentDateTimeFunction \
  --outputDefaultSchema \
  --outputDefaultCatalog \
  --outputFile \
  --rollbackScript \
  --excludeObjects \
  --includeObjects \
  --help \
  --version \
  --snapshotFormat \
  --referenceUsername \
  --referencePassword \
  --referenceUrl \
  --defaultCatalogName \
  --defaultSchemaName \
  --referenceDefaultCatalogName \
  --referenceDefaultSchemaName \
  --schemas \
  --referenceSchemas \
  --outputSchemaAs \
  --includeCatalog \
  --includeSchema \
  --includeTablespace \
  --referenceDriver \
  --dataOutputDirectory \
  --diffTypes \
  --diffTypes=<catalog,tables,functions,views,columns,indexes,foreignkeys,primarykeys,uniqueconstraints,data,storedprocedure,triggers,sequences> -D<property.name>=<property.value> \
  --format \
  --liquibaseProLicenseKey" liquibase
