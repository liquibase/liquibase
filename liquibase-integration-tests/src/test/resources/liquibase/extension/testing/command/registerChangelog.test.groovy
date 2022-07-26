package liquibase.extension.testing.command

import liquibase.exception.CommandExecutionException
import liquibase.exception.CommandValidationException
import liquibase.hub.core.MockHubService

import java.util.regex.Pattern

CommandTests.define {
    command = ["registerChangelog"]
    signature = """
Short Description: Register the changelog with a Liquibase Hub project
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
Optional Args:
  hubProjectId (UUID) Used to identify the specific Project in which to record or extract data at Liquibase Hub. Available in your Liquibase Hub account at https://hub.liquibase.com.
    Default: null
  hubProjectName (String) The Hub project name
    Default: null
"""
    run "Happy path", {
        arguments = [
                hubProjectName   : "Project 1",
                changelogFile: "changelogs/hsqldb/complete/registered-changelog-test.xml",
        ]
        setup {
            createTempResource "changelogs/hsqldb/complete/rollback.changelog.xml", "changelogs/hsqldb/complete/registered-changelog-test.xml"
        }
        expectedResults = [
                statusCode   : 0,
                registeredChangeLogId  : { MockHubService.randomUUID.toString() }
        ]
    }

    run "Happy path, supply hub project name when prompted interactively", {
        arguments = [
                changelogFile: "changelogs/hsqldb/complete/registered-changelog-test.xml",
        ]
        setup {
            createTempResource "changelogs/hsqldb/complete/rollback.changelog.xml", "changelogs/hsqldb/complete/registered-changelog-test.xml"
        }
        testUI = new CommandTests.TestUIWithAnswers(["c", "project name here"] as String[])
        expectedUI = "Please enter your Project name and press [enter]"
        expectedResults = [
                statusCode   : 0,
                registeredChangeLogId  : { MockHubService.randomUUID.toString() }
        ]
    }

    run "Name is too long", {
        arguments = [
                hubProjectName   : "String longer than 255 characters qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq",
                changelogFile: "changelogs/hsqldb/complete/registered-changelog-test.xml",
        ]
        setup {
            createTempResource "changelogs/hsqldb/complete/rollback.changelog.xml", "changelogs/hsqldb/complete/registered-changelog-test.xml"
        }
        expectedException = CommandExecutionException.class
        expectedExceptionMessage = "The project name you gave is longer than 255 characters"
    }

    run "Run with already-registered changelog throws an exception", {
        arguments = [
                changelogFile: "simple.changelog.with.id.xml"
        ]

        setup {
            copyResource "changelogs/hsqldb/complete/simple.changelog.xml", "simple.changelog.with.id.xml"
            modifyChangeLogId "simple.changelog.with.id.xml", MockHubService.alreadyRegisteredUUID.toString()
            runChangelog "changelogs/hsqldb/complete/simple.changelog.xml"
        }
        expectedException = CommandExecutionException.class
        expectedExceptionMessage = Pattern.compile(".*is already registered with changeLogId*", Pattern.MULTILINE | Pattern.DOTALL)
    }

    run "Run without a changeLogFile throws an exception", {
        arguments = [
                changelogFile: ""
        ]
        expectedException = CommandValidationException.class
    }
}
