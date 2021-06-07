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
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  hubConnectionId (UUID) Used to identify the specific Connection in which to record or extract data at Liquibase Hub. Available in your Liquibase Hub Project at https://hub.liquibase.com.
    Default: null
  hubProjectId (UUID) Used to identify the specific Project in which to record or extract data at Liquibase Hub. Available in your Liquibase Hub account at https://hub.liquibase.com.
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  username (String) Username to use to connect to the database
    Default: null
"""
    run "Happy path", {
        arguments = [
            url:        { it.url },
            username:   { it.username },
            password:   { it.password },
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
            url:        { it.url },
            username:   { it.username },
            password:   { it.password },
            hubConnectionId: {UUID.randomUUID().toString()},
            hubProjectId: {UUID.randomUUID().toString()}
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }
        expectedException = CommandExecutionException.class
    }

    run "Run with unknown Hub connection ID throws an exception", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                hubConnectionId: {MockHubService.failUUID}
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }
        expectedException = CommandExecutionException.class
        expectedExceptionMessage = Pattern.compile(".*was either not found, or you do not have access.*", Pattern.MULTILINE | Pattern.DOTALL)
    }

    run "Run with deleted changelog throws an exception", {
        arguments = [
            url:        { it.url },
            username:   { it.username },
            password:   { it.password },
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
            url:        { it.url },
            username:   { it.username },
            password:   { it.password },
            hubProjectId: { MockHubService.failUUID.toString() }
        ]

        expectedException = CommandExecutionException.class
        expectedExceptionMessage = Pattern.compile(".*does not exist or you do not have access to it.*", Pattern.MULTILINE | Pattern.DOTALL)
    }

    run "Run with unrecognized changelog ID throws an exception", {
        arguments = [
            url:        { it.url },
            username:   { it.username },
            password:   { it.password },
            changelogFile: "changelogs/hsqldb/complete/simple.changelog.with.id.xml"
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }
        expectedException = CommandExecutionException.class
    }

    run "Run without any options should not throw an exception",  {
        arguments = [
            url:        { it.url },
            username:   { it.username },
            password:   { it.password },
        ]
    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
    }
}
