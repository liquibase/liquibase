package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

CommandTest.define {
    command = ["diff"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  compareControl (CompareControl) MISSING DESCRIPTION
  objectChangeFilter (ObjectChangeFilter) MISSING DESCRIPTION
  outputStream (PrintStream) MISSING DESCRIPTION
  referenceDatabase (Database) MISSING DESCRIPTION
  referenceSnapshotControl (SnapshotControl) MISSING DESCRIPTION
  snapshotListener (SnapshotListener) MISSING DESCRIPTION
  snapshotTypes (Class[]) MISSING DESCRIPTION
  targetDatabase (Database) MISSING DESCRIPTION
  targetSnapshotControl (SnapshotControl) MISSING DESCRIPTION
Optional Args:
  NONE
"""
    run {
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
                statusMessage: "Successfully executed diff",
                statusCode   : 0
        ]
    }
}
