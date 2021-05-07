package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

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
                changelogFile: "changelogs/hsqldb/complete/simple.changelog.with.id-test.xml",
        ]
        setup {
            createTempResource "changelogs/hsqldb/complete/simple.changelog.with.id.xml", "changelogs/hsqldb/complete/simple.changelog.with.id-test.xml"
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

    run "Run without any arguments should throw an exception",  {
        expectedException = CommandValidationException.class
    }
}
