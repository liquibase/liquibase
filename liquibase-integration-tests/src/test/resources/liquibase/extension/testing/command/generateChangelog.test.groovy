package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange
import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["generateChangelog"]
    signature = """
Short Description: Generate a changelog
Long Description: Writes Change Log XML to copy the current state of the database to standard out or a file
Required Args:
  changelogFile (String) File to write changelog to
  url (String) The JDBC database connection URL
Optional Args:
  dataOutputDirectory (String) Directory to write table data to
    Default: null
  diffTypes (String) Types of objects to compare
    Default: null
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  excludeObjects (String) Objects to exclude from diff
    Default: null
  includeObjects (String) Objects to include in diff
    Default: null
  includeTablespace (String) Include the tablespace attribute in the changelog
    Default: null
  overwriteOutputFile (String) Flag to allow overwriting of output changelog file
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  schemas (String) Schemas to include in diff
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path", {
        arguments = [
            url     : { it.url },
            username: { it.username },
            password: { it.password },
            changelogFile: "target/test-classes/changelog-test.xml"
        ]
        setup {
            cleanResources("changelog-test.xml")
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
        expectedFileContent = [
                "target/test-classes/changelog-test.xml" : [CommandTests.assertContains("<changeSet ", 3)]
        ]
        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Run without changelogFile throws exception", {
        arguments = [
                changelogFile: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without URL throws exception", {
        arguments = [
                url: "",
                changelogFile: "target/test-classes/changeLog-test.xml"
        ]
        expectedException = CommandValidationException.class
    }
}
