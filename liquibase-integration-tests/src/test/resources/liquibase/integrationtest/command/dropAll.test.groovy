package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

CommandTest.define {
    command = ["dropAll"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  database (Database) MISSING DESCRIPTION
Optional Args:
  changelog (DatabaseChangeLog) MISSING DESCRIPTION
    Default: null
  changelogFile (String) MISSING DESCRIPTION
    Default: null
  hubConnectionId (UUID) MISSING DESCRIPTION
    Default: null
  schemas (CatalogAndSchema[]) MISSING DESCRIPTION
    Default: null
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
                statusCode   : 0,
                statusMessage: "Successfully executed dropAll"
        ]
    }
}
