package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateSequenceChange
import liquibase.change.core.CreateTableChange
import liquibase.exception.CommandValidationException

import java.util.regex.Pattern

final int PATTERN_FLAGS = Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE

CommandTests.define {
    command = ["diff"]
    signature = """
Short Description: Outputs a description of differences.  If you have a Liquibase Pro key, you can output the differences as JSON using the --format=JSON option
Long Description: NOT SET
Required Args:
  referenceUrl (String) The JDBC reference database connection URL
    OBFUSCATED
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
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
  outputSchemas (String) Output schemas names. This is a CSV list.
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  referenceDefaultCatalogName (String) The default catalog name to use for the reference database connection
    Default: null
  referenceDefaultSchemaName (String) The default schema name to use for the reference database connection
    Default: null
  referenceDriver (String) The JDBC driver class for the reference database
    Default: null
  referenceDriverPropertiesFile (String) The JDBC driver properties file for the reference database
    Default: null
  referenceLiquibaseCatalogName (String) Reference catalog to use for Liquibase objects
    Default: null
  referenceLiquibaseSchemaName (String) Reference schema to use for Liquibase objects
    Default: null
  referencePassword (String) The reference database password
    Default: null
    OBFUSCATED
  referenceSchemas (String) Schemas names on reference database to use in diff. This is a CSV list.
    Default: null
  referenceUsername (String) The reference database username
    Default: null
  schemas (String) Schemas to include in diff
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Running diff against itself finds no differences", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.url },
                referenceUsername: { it.username },
                referencePassword: { it.password },
        ]

        setup {
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
            ]

        }
        expectedOutput = [
                """
Missing Table(s): NONE
Unexpected Table(s): NONE
Changed Table(s): NONE
""",
                """
Missing Column(s): NONE
Unexpected Column(s): NONE
Changed Column(s): NONE
""",
        ]
    }

    run "Running diff against an empty database finds things unexpected", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword },
        ]

        setup {
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
            ]

            altDatabase = []

        }
        expectedOutput = [
                Pattern.compile(".*Missing Table.s.*NONE.*Unexpected Table.s.*FIRSTTABLE.*SECONDTABLE.*", PATTERN_FLAGS),
                Pattern.compile(".*Missing Column.s.*NONE.*Unexpected Column.s.*FIRSTTABLE.*FIRSTCOLUMN.*SECONDTABLE.*SECONDCOLUMN.*", PATTERN_FLAGS)
        ]
    }

    run "Running diff against a full database finds things missing", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword },
        ]

        setup {
            database = []

            altDatabase = [
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
            ]

        }
        expectedOutput = [
                Pattern.compile(".*Missing Table.s.*FIRSTTABLE.*SECONDTABLE.*", PATTERN_FLAGS),
                Pattern.compile(".*Missing Column.s.*FIRSTTABLE.*FIRSTCOLUMN.*SECONDTABLE.*SECONDCOLUMN.*", PATTERN_FLAGS)
        ]
    }
    run "Running diff against a full database finds things missing and writes to an output file", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword },
        ]

        setup {
            cleanResources("target/test-classes/diff.txt")
            database = []

            altDatabase = [
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
            ]

        }

        outputFile = new File("target/test-classes/diff.txt")

        expectedFileContent = [
                //
                // Find the " -- Release Database Lock" line
                //
                "target/test-classes/diff.txt" : [CommandTests.assertContains("Changed Column(s): NONE")]
        ]
    }

    run "Running diff against two empty databases finds no differences", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword },
                schemas          : { it.database.getDefaultSchemaName() }
        ]

        setup {
            database = []
            altDatabase = []

        }
        expectedOutput = [
                """
Missing Table(s): NONE
Unexpected Table(s): NONE
Changed Table(s): NONE
""",
                """
Missing Column(s): NONE
Unexpected Column(s): NONE
Changed Column(s): NONE
""",
        ]
    }

    run "Running diff against differently structured databases finds changed objects", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword }
        ]

        setup {
            database = [
                    new CreateTableChange(
                            tableName: "SharedTable",
                            columns: [
                                    ColumnConfig.fromName("Id")
                                            .setType("VARCHAR(255)"),
                                    ColumnConfig.fromName("Shared")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
                    new CreateTableChange(
                            tableName: "PrimaryTable",
                            columns: [
                                    ColumnConfig.fromName("Id")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
                    new CreateSequenceChange(
                            sequenceName: "seq1",
                            dataType: "bigint",
                            incrementBy: 1,
                            startValue: 1
                    )
            ]

            altDatabase = [
                    new CreateTableChange(
                            tableName: "SharedTable",
                            columns: [
                                    ColumnConfig.fromName("Name")
                                            .setType("VARCHAR(255)"),
                                    ColumnConfig.fromName("Shared")
                                            .setType("VARCHAR(3)")
                            ]
                    ),
                    new CreateTableChange(
                            tableName: "SecondaryTable",
                            columns: [
                                    ColumnConfig.fromName("Id")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
            ]

        }
        expectedOutput = [
                Pattern.compile(".*Missing Table.s.*SECONDARYTABLE.*Unexpected Table.s.*PRIMARYTABLE.*Changed Table.s.*NONE", PATTERN_FLAGS),
                Pattern.compile(".*Missing Column.s.*SECONDARYTABLE.ID.*SHAREDTABLE.NAME.*", PATTERN_FLAGS),
                Pattern.compile(".*Unexpected Column.s.*PRIMARYTABLE.ID.*SHAREDTABLE.ID.*Changed Column.s.*", PATTERN_FLAGS),
                Pattern.compile(".*SHAREDTABLE.SHARED.*type changed from .CHARACTER VARYING.3.*to .CHARACTER VARYING.255.*", PATTERN_FLAGS)
        ]
    }

    run "Running diff against differently structured databases should not find non-included types", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword },
                diffTypes        : "tables"
        ]

        setup {
            database = [
                    new CreateTableChange(
                            tableName: "SharedTable",
                            columns: [
                                    ColumnConfig.fromName("Id")
                                            .setType("VARCHAR(255)"),
                                    ColumnConfig.fromName("Shared")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
                    new CreateTableChange(
                            tableName: "PrimaryTable",
                            columns: [
                                    ColumnConfig.fromName("Id")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
                    new CreateSequenceChange(
                            sequenceName: "seq1",
                            dataType: "bigint",
                            incrementBy: 1,
                            startValue: 1
                    )
            ]

            altDatabase = [
                    new CreateTableChange(
                            tableName: "SharedTable",
                            columns: [
                                    ColumnConfig.fromName("Name")
                                            .setType("VARCHAR(255)"),
                                    ColumnConfig.fromName("Shared")
                                            .setType("VARCHAR(3)")
                            ]
                    ),
                    new CreateTableChange(
                            tableName: "SecondaryTable",
                            columns: [
                                    ColumnConfig.fromName("Id")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
            ]

        }
        expectedOutput = [
                CommandTests.assertNotContains("Sequence")
        ]
    }

    run "Run without a URL throws an exception", {
        arguments = [
                url              : "",
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword },
        ]

        setup {
            database = []
            altDatabase = []

        }
        expectedException = CommandValidationException.class
    }

    run "Run without a referenceURL throws an exception", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password }
        ]

        setup {
            database = []
            altDatabase = []

        }
        expectedException = CommandValidationException.class
        expectedExceptionMessage = "Invalid argument 'referenceUrl': missing required argument"
    }

    run "Run without any arguments throws an exception", {
        setup {
            database = []
            altDatabase = []

        }
        expectedException = CommandValidationException.class
    }

}
