package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange
import liquibase.exception.CommandExecutionException
import liquibase.exception.CommandValidationException
import liquibase.extension.testing.setup.SetupCleanResources
import liquibase.extension.testing.setup.SetupEnvironmentVariableProvider

CommandTests.define {
    command = ["dropAll"]
    signature = """
Short Description: Drop all database objects owned by the user
Long Description: NOT SET
Required Args:
  force (Boolean) Argument to allow use of dropAll with values of 'true' or 'false'. The default is 'false'.
  requireForce (Boolean) Argument to require user of dropAll to supply a 'force' argument, with values of 'true' or 'false'. The default is 'false'.
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  schemas (String) Schemas to include in drop
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""
    run "Happy path", {
        arguments = [
                url       : { it.url },
                username  : { it.username },
                password  : { it.password }
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
        ]

        expectedUI = [
            "INFO: The drop-all command may result in unrecoverable destructive changes to objects at",
            "To protect against unwanted drops, set --requireForce=true, which will require a --force=true flag on the command.",
            "Learn more at https://docs.liquibase.com/dropall."
        ]
    }

    run "Happy path with explicit requireDropAllForce=false", {
        arguments = [
                url       : { it.url },
                username  : { it.username },
                password  : { it.password }
        ]
        setup {
            def add = [ LIQUIBASE_DROP_ALL_REQUIRE_FORCE:"false" ]
            String[] remove = [:]
            run(
                    new SetupEnvironmentVariableProvider(add, remove)
            )
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
        ]

        expectedUI = [
                "INFO: The drop-all command may result in unrecoverable destructive changes to objects at",
                "To protect against unwanted drops, set --requireForce=true, which will require a --force=true flag on the command.",
                "Learn more at https://docs.liquibase.com/dropall."
        ]
    }

    run "Happy path with requireDropAllForce=true and force=true", {
        arguments = [
                url       : { it.url },
                username  : { it.username },
                password  : { it.password },
                force: { true }
        ]
        setup {
            def add = [ LIQUIBASE_COMMAND_DROP_ALL_REQUIRE_FORCE:"true" ]
            String[] remove = [:]
            run(
                    new SetupEnvironmentVariableProvider(add, remove)
            )
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
        ]

        expectedUI = [
                "All objects dropped from"
        ]
    }

    run "Run with require dropAll flag set to true", {
        arguments = [
                url       : { it.url },
                username  : { it.username },
                password  : { it.password },
                requireForce: { true }
        ]
        setup {
            cleanResources(SetupCleanResources.CleanupMode.CLEAN_ON_BOTH, "liquibase.flowfile.yaml")
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

        expectedException = CommandExecutionException.class
        expectedExceptionMessage =
"""
The drop-all command may result in unrecoverable destructive changes by dropping all the objects at database
"""
    }

    run "Run without a URL should throw an exception",  {
        arguments = [
                url          : "",
        ]
        expectedException = CommandValidationException.class
    }
}
