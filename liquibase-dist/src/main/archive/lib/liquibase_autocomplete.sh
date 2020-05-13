_liquibase()
{
    local cur prev opts
    COMPREPLY=()
    cur="${COMP_WORDS[COMP_CWORD]}"
    prev="${COMP_WORDS[COMP_CWORD-1]}"

    # Liquibase options, has to be improved to be more context aware
    opts="
update
updateSQL
updateCount 
updateCountSQL
rollback
rollbackSQL
rollbackToDate
rollbackToDateSQL
rollbackCount
rollbackCountSQL
futureRollbackSQL
futureRollbackSQL
updateTestingRollback
generateChangeLog
diff
diffChangeLog
dbDoc
tag
status
status --verbose
unexpectedChangeSets
unexpectedChangeSets --verbose
validate
calculateCheckSum
clearCheckSums
changelogSync
changelogSyncSQL
markNextChangeSetRan
markNextChangeSetRanSQL
listLocks
releaseLocks
dropAll
--changeLogFile=
--username=
--password
--url
--classpath=
--driver=
--databaseClass=
--defaultSchemaName=
--contexts=
--defaultsFile=
--driverPropertiesFile=
--includeSystemClasspath=
--promptForNonLocalDatabase=
--logLevel=
--logFile=
--currentDateTimeFunction=
--help
--version

"
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
