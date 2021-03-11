_liquibase()
{
    local cur prev opts
    COMPREPLY=()
    cur="${COMP_WORDS[COMP_CWORD]}"
    prev="${COMP_WORDS[COMP_CWORD-1]}"

    # Liquibase options, has to be improved to be more context aware
    opts="
  help
  update
  updateSQL
  updateCount
  updateCountSQL
  updateToTag
  updateToTagSQL
  status
  registerChangeLog
  syncHub
  rollback
  rollbackSQL
  rollbackOneChangeSet
  rollbackOneChangeSetSQL
  rollbackOneUpdate
  rollbackOneUpdateSQL
  rollbackToDate
  rollbackToDateSQL
  rollbackCount
  rollbackCountSQL
  futureRollbackSQL
  futureRollbackFromTagSQL
  updateTestingRollback
  generateChangeLog
  snapshot
  snapshotReference
  diff
  diffChangeLog
  dbDoc
  history
  tag
  tagExists
  status
  unexpectedChangeSets
  validate
  calculateCheckSum
  clearCheckSums
  changelogSync
  changelogSyncSQL
changeLogSyncToTag
changeLogSyncToTagSQL
  markNextChangeSetRan
  markNextChangeSetRanSQL
  listLocks
  releaseLocks
  dropAll
  --changeLogFile
  --changeSetAuthor=<author>
  --changeSetId=<id>
  --changeSetPath=<changelogFile>
  --deploymentId=<deploymentId>
  --force
  --format
  --username
  --password
  --url
  --classpath
  --driver
  --databaseClass
  --propertyProviderClass
  --defaultSchemaName
  --contexts
  --labels
  --defaultsFile
  --delimiter
  --driverPropertiesFile
  --changeExecListenerClass
  --changeExecListenerPropertiesFile
  --liquibaseCatalogName
  --liquibaseSchemaName
  --databaseChangeLogTableName
  --databaseChangeLogLockTableName
  --databaseChangeLogTablespaceName
  --liquibaseSchemaName
  --includeSystemClasspath
  --overwriteOutputFile
  --promptForNonLocalDatabase
  --logLevel
  --logFile
  --currentDateTimeFunction
  --outputDefaultSchema
  --outputDefaultCatalog
  --outputFile
  --rollbackScript
  --excludeObjects
  --includeObjects
  --help
  --version
  --snapshotFormat
  --referenceUsername
  --referencePassword
  --referenceUrl
  --defaultCatalogName
  --defaultSchemaName
  --referenceDefaultCatalogName
  --referenceDefaultSchemaName
  --schemas
  --referenceSchemas
  --outputSchemaAs
  --includeCatalog
  --includeSchema
  --includeTablespace
  --referenceDriver
  --dataOutputDirectory
  --diffTypes
  --diffTypes=<catalog,tables,functions,views,columns,indexes,foreignkeys,primarykeys,uniqueconstraints,data,storedprocedure,triggers,sequences> -D<property.name>=<property.value>
  --verbose
  --liquibaseProLicenseKey"
    # Handle --xxxxxx=
    if [[ ${prev} == "--"* && ${cur} == "=" ]] ; then
        COMPREPLY=(*)
        return 0
    fi
    # Handle --xxxxx=path
    if [[ ${prev} == '=' ]] ; then
        # Unescape space
        cur=${cur//\\ / }
        # Expand tilder to $HOME
        [[ ${cur} == "~/"* ]] && cur=${cur/\~/$HOME}
        # Show completion if path exist (and escape spaces)
        local files=("${cur}"*)
        [[ -e ${files[0]} ]] && COMPREPLY=( "${files[@]// /\ }" )
        return 0
    fi

    # Handle other options
    COMPREPLY=( $(compgen -W "${opts}" -- "${cur}") )
    if [[ ${#COMPREPLY[@]} == 1 && ${COMPREPLY[0]} != "--"*"=" ]] ; then
        # If there's only one option, without =, then allow a space
        compopt +o nospace
    fi
    return 0
}
complete -o nospace -F _liquibase liquibase
