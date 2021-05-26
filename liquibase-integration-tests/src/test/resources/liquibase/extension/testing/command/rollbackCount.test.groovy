package liquibase.extension.testing.command

import java.util.regex.Pattern

CommandTests.define {
    command = ["rollbackCount"]
    signature = """
Short Description: Rollback the specified number of changes made to the database
Long Description: Rollback the specified number of changes made to the database
Required Args:
  count (Integer) The number of changes to rollback
  url (String) The JDBC database connection URL
Optional Args:
  changeLogFile (String) The root changelog
    Default: null
  contexts (String) Changeset contexts to match
    Default: null
  labels (String) Changeset labels to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  rollbackScript (String) Rollback script to execute
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                count        : 1,
                changeLogFile: "changelogs/hsqldb/complete/rollback.changelog.xml"
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.changelog.xml"
        }

        expectedDatabaseContent = [
                "txt": [Pattern.compile(".*liquibase.structure.core.Table.*FIRSTTABLE.*", Pattern.MULTILINE|Pattern.DOTALL),
                        CommandTests.assertNotContains(".*liquibase.structure.core.Table.*SECONDTABLE.*")]
        ]

        expectedResults = [
                statusMessage: "Successfully executed rollbackCount",
                statusCode   : 0
        ]
    }
}
