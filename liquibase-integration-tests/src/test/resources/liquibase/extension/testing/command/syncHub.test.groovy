package liquibase.extension.testing.command

import liquibase.exception.CommandExecutionException
import liquibase.exception.CommandValidationException
import liquibase.hub.core.MockHubService

import java.util.regex.Pattern

CommandTests.define {
    command = ["syncHub"]
    signature = """
Short Description: Synchronize the local DatabaseChangeLog table with Liquibase Hub
Long Description: NOT SET
Required Args:
  url (String) The JDBC database connection URL
Optional Args:
  changelogFile (String) The root changelog
    Default: null
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  hubConnectionId (String) Liquibase Hub Connection ID to sync
    Default: null
  hubProjectId (String) Liquibase Hub Project ID to sync
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""
    run "Happy path", {
        arguments = [
            changelogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }

        expectedResults = [
            statusCode   : 0
        ]
    }

    run "Run with both Hub connection ID and Hub Project ID throws an exception", {
        arguments = [
            hubConnectionId: {UUID.randomUUID().toString()},
            hubProjectId: {UUID.randomUUID().toString()}
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }
        expectedException = CommandExecutionException.class
    }

    run "Run with deleted changelog throws an exception", {
        arguments = [
            changelogFile: "simple.changelog.with.deleted-id.xml"
        ]

        setup {
            copyResource "changelogs/hsqldb/complete/simple.changelog.xml", "simple.changelog.with.deleted-id.xml"
            modifyChangeLogId "simple.changelog.with.deleted-id.xml", MockHubService.deletedUUID.toString()
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }
        expectedException = CommandExecutionException.class
        expectedExceptionMessage = Pattern.compile(".*the.*registered changelog has been deleted.*", Pattern.MULTILINE | Pattern.DOTALL)
    }

    run "Run with unrecognized project throws an exception", {
        arguments = [
                hubProjectId: { MockHubService.failUUID.toString() }
        ]

        expectedException = CommandExecutionException.class
        expectedExceptionMessage = Pattern.compile(".*does not exist or you do not have access to it.*", Pattern.MULTILINE | Pattern.DOTALL)
    }

    run "Run with unrecognized changelog ID throws an exception", {
        arguments = [
            changelogFile: "changelogs/hsqldb/complete/simple.changelog.with.id.xml"
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }
        expectedException = CommandExecutionException.class
    }

    run "Run without any options should not throw an exception",  {
    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
    }
}