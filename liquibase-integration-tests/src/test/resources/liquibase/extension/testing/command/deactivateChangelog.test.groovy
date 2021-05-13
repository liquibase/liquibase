package liquibase.extension.testing.command

import liquibase.exception.CommandExecutionException
import liquibase.exception.CommandValidationException
import liquibase.hub.core.MockHubService

CommandTests.define {
    command = ["deactivateChangelog"]
    signature = """
Short Description: Removes the changelogID from your changelog so it stops sending reports to Liquibase Hub
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
Optional Args:
  NONE
"""

    run "Happy path", {

        arguments = [
                changelogFile: "simple.changelog.with.id-test.xml",
        ]
        setup {
            createTempResource "changelogs/hsqldb/complete/simple.changelog.xml", "simple.changelog.with.id-test.xml"
            modifyChangeLogId "simple.changelog.with.id-test.xml", MockHubService.alreadyRegisteredUUID.toString()
        }
        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Run without a changeLogFile should throw an exception",  {
        arguments = [
                changelogFile: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run against a changeLogFile with no changeLogId should throw an exception",  {
        arguments = [
                changelogFile: "changelogs/hsqldb/complete/simple.changelog.xml",
        ]
        expectedException = CommandExecutionException.class
    }

    run "Run against a changeLogFile with a changeLogId not in Hub should complain but not throw an exception",  {
        arguments = [
                changelogFile: "simple.changelog.xml"
        ]
        setup {
            createTempResource "changelogs/hsqldb/complete/simple.changelog.xml", "simple.changelog.xml"
            modifyChangeLogId "simple.changelog.xml", UUID.randomUUID().toString()
        }
        expectedUI = "has a changelog ID but was not found in Hub"
    }

    run "Run without any arguments should throw an exception",  {
        expectedException = CommandValidationException.class
    }
}
