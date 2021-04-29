package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

CommandTests.define {
    command = ["diffChangelog"]
    signature = """
Short Description: Compare two databases to produce changesets and write them to a changelog file
Long Description: NOT SET
Required Args:
  changelogFile (String) Changelog file to write results
  referenceUrl (String) The JDBC reference database connection URL
  url (String) The JDBC target database connection URL
Optional Args:
  password (String) The target database password
    Default: null
  referencePassword (String) The reference database password
    Default: null
  referenceUsername (String) The reference database username
    Default: null
  username (String) The target database username
    Default: null
"""


    run {
        arguments = [
            referenceUrl: "offline:postgresql?snapshot=snapshot1.json",
            url: "offline:postgresql?snapshot=snapshot2.json",
            changelogFile: "target/test-classes/diffChangelog-test.xml"
        ]
        setup {
            cleanTempResource("diffChangelog-test.xml")
            database = [
                    new CreateTableChange(
                            tableName: "FirstTable",
                            columns: [
                                    ColumnConfig.fromName("FirstColumn")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
                    new CreateTableChange(
                            tableName: "SecondTable",
                            columns: [
                                    ColumnConfig.fromName("SecondColumn")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
                    new TagDatabaseChange(
                            tag: "version_2.0"
                    ),
                    new CreateTableChange(
                            tableName: "liquibaseRunInfo",
                            columns: [
                                    ColumnConfig.fromName("timesRan")
                                            .setType("INT")
                            ]
                    ),
            ]
        }

        expectedResults = [
                statusMessage: "Successfully executed diffChangelog",
                statusCode   : 0
        ]
    }
}
