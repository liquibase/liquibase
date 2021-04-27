package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange

import java.util.regex.Pattern

CommandTests.define {
    command = ["diff"]
    signature = """
Short Description: Compare two databases
Long Description: Compare two databases
Required Args:
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

        expectedUI = "Liquibase command 'diff' was executed successfully"
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
                """
Missing Table(s): NONE
Unexpected Table(s): 
     FIRSTTABLE
     SECONDTABLE
Changed Table(s): NONE
""",
                """
Missing Column(s): NONE
Unexpected Column(s): 
     PUBLIC.FIRSTTABLE.FIRSTCOLUMN
     PUBLIC.SECONDTABLE.SECONDCOLUMN
Changed Column(s): NONE
""",
        ]

        expectedUI = "Liquibase command 'diff' was executed successfully"
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
                """
Missing Table(s): 
     FIRSTTABLE
     SECONDTABLE
Unexpected Table(s): NONE
Changed Table(s): NONE
""",
                """
Missing Column(s): 
     PUBLIC.FIRSTTABLE.FIRSTCOLUMN
     PUBLIC.SECONDTABLE.SECONDCOLUMN
Unexpected Column(s): NONE
Changed Column(s): NONE
""",
        ]

        expectedUI = "Liquibase command 'diff' was executed successfully"
    }

    run "Running diff against two empty databases finds no differences", {
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

        expectedUI = "Liquibase command 'diff' was executed successfully"
    }


    run "Running diff against differently structured databases finds changed objects", {
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
                """
Missing Table(s): 
     SECONDARYTABLE
Unexpected Table(s): 
     PRIMARYTABLE
Changed Table(s): NONE
""",
                Pattern.compile(/
Missing Column\(s\): 
     [\w.]*SECONDARYTABLE.ID
     [\w.]*SHAREDTABLE.NAME
Unexpected Column\(s\): 
     [\w.]*PRIMARYTABLE.ID
     [\w.]*SHAREDTABLE.ID
Changed Column\(s\): 
     PUBLIC.SHAREDTABLE.SHARED
          type changed from 'VARCHAR\(3.*?\)' to 'VARCHAR\(255.*?\)'/),
        ]

        expectedUI = "Liquibase command 'diff' was executed successfully"
    }

}
