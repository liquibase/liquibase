package liquibase.extension.testing.command


import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import liquibase.change.core.AddPrimaryKeyChange
import liquibase.change.core.CreateTableChange
import liquibase.exception.CommandExecutionException
import liquibase.exception.CommandValidationException
import liquibase.extension.testing.setup.SetupCleanResources

import java.util.regex.Pattern

CommandTests.define {
    command = ["diffChangelog"]
    signature = """
Short Description: Compare two databases to produce changesets and write them to a changelog file
Long Description: NOT SET
Required Args:
  changelogFile (String) Changelog file to write results
  referenceUrl (String) The JDBC reference database connection URL
    OBFUSCATED
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  author (String) Specifies the author for changesets in the generated changelog
    Default: null
  contextFilter (String) Changeset contexts to generate
    Default: null
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
  includeCatalog (Boolean) If true, the catalog will be included in generated changeSets. Defaults to false.
    Default: false
  includeObjects (String) Objects to include in diff
    Default: null
  includeSchema (Boolean) If true, the schema will be included in generated changeSets. Defaults to false.
    Default: false
  includeTablespace (Boolean) Include the tablespace attribute in the changelog. Defaults to false.
    Default: false
  labelFilter (String) Changeset labels to generate
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
  replaceIfExistsTypes (String) Sets replaceIfExists="true" for changes of these types (supported types: createProcedure, createView)
    Default: none
  runOnChangeTypes (String) Sets runOnChange="true" for changesets containing solely changes of these types (e. g. createView, createProcedure, ...).
    Default: none
  schemas (String) Schemas to include in diff
    Default: null
  skipObjectSorting (Boolean) When true will skip object sorting. This can be useful on databases that have a lot of packages/procedures that are linked to each other
    Default: false
  useOrReplaceOption (Boolean) If true, will add 'OR REPLACE' option to the create view change object
    Default: false
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Running diffChangelog against itself finds no differences and don't generate an output file", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.url },
                referenceUsername: { it.username },
                referencePassword: { it.password },
                changelogFile: "target/test-classes/diffChangelog-test.xml",
        ]

        setup {
            cleanResources(SetupCleanResources.CleanupMode.CLEAN_ON_SETUP, "diffChangelog-test.xml")
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

        expectFileToNotExist = new File("target/test-classes/diffChangelog-test.xml")
    }

    run "illegal file path should throw exception", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.url },
                referenceUsername: { it.username },
                referencePassword: { it.password },
                changelogFile: "nonexistant://thisfileshouldnotbecreated.xml",
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

        expectedException = CommandExecutionException.class
        expectedExceptionMessage = "java.io.IOException: Cannot parse resource location: 'nonexistant://thisfileshouldnotbecreated.xml'"
    }

    run "Running diffChangelog should add changesets with specified author", {
        arguments = [
                author           : "Test Author",
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword },
                changelogFile: "target/test-classes/diffChangeLog-test.xml",
        ]

        setup {
            cleanResources(SetupCleanResources.CleanupMode.CLEAN_ON_SETUP, "diffChangeLog-test.xml")
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
        expectedFileContent = [
                "target/test-classes/diffChangeLog-test.xml" : [CommandTests.assertContains("<changeSet ", 5),
                                                                CommandTests.assertContains("<dropTable ", 1),
                                                                CommandTests.assertContains("author=\"Test Author\"", 5)]
        ]
    }

    run "Running diffChangelog should add changesets in the correct order", {
        arguments = [
                url              : { it.altUrl },
                username         : { it.altUsername },
                password         : { it.altPassword },
                referenceUrl     : { it.url},
                referenceUsername: { it.username},
                referencePassword: { it.password},
                changelogFile: "target/test-classes/diffChangelogOrder-test.xml",
        ]

        setup {
            cleanResources(SetupCleanResources.CleanupMode.CLEAN_ON_SETUP, "diffChangelogOrder-test.xml")
            database = [
                    new CreateTableChange(
                        tableName: "person",
                        columns: [
                            ColumnConfig.fromName("address").setType("VARCHAR(255)"),
                            ColumnConfig.fromName("id").setType("VARCHAR(255)")
                                    .setConstraints(new ConstraintsConfig().setNullable(false))
                        ]
                    ),
                    new AddPrimaryKeyChange(
                        tableName:       "person",
                        columnNames:     "id",
                        constraintName:  "pk_person",
                    )
            ]

            altDatabase = [
                    new CreateTableChange(
                        tableName: "person",
                        columns: [
                            ColumnConfig.fromName("address").setType("VARCHAR(255)"),
                        ]
                    )
            ]

        }
        expectedFileContent = [
                "target/test-classes/diffChangelogOrder-test.xml" :
                [
                        CommandTests.assertContains("<changeSet ", 2),
                        CommandTests.assertNotContains("<dropColumn "),
                        Pattern.compile(".*addColumn.*addPrimaryKey.*", Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE)]
                ]
    }

    run "Running diff against differently structured databases finds changed objects", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword },
                changelogFile: "target/test-classes/diffChangeLog-test-1212093821.xml",
        ]

        setup {
            cleanResources("diffChangeLog-test-1212093821.xml")
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
        expectedFileContent = [
                "target/test-classes/diffChangeLog-test-1212093821.xml" :
                        [
                                CommandTests.assertContains("<changeSet ", 5),
                                CommandTests.assertContains("<createTable ", 1),
                                CommandTests.assertContains("<addColumn ", 1),
                                CommandTests.assertContains("<dropTable ", 1),
                                CommandTests.assertContains("<dropColumn ", 1),
                                CommandTests.assertContains("<modifyDataType ", 1),
                        ]
        ]
    }

    run "Running diff against differently structured databases finds changed objects, with existing changelog file", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword },
                changelogFile: "target/test-classes/diffChangeLog-test-21938109283.xml",
        ]

        setup {
            cleanResources("diffChangeLog-test-21938109283.xml")
            copyResource("changelogs/diffChangeLog-test-21938109283.xml", "diffChangeLog-test-21938109283.xml")
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
        expectedFileContent = [
                "target/test-classes/diffChangeLog-test-21938109283.xml" :
                        [
                                CommandTests.assertContains("<changeSet ", 10),
                                CommandTests.assertContains("<createTable ", 2),
                                CommandTests.assertContains("<addColumn ", 2),
                                CommandTests.assertContains("<dropTable ", 2),
                                CommandTests.assertContains("<dropColumn ", 2),
                                CommandTests.assertContains("<modifyDataType ", 2),
                                CommandTests.assertContains("</databaseChangeLog>", 1),
                        ]
        ]
    }

    run "Running without changelogFile gives an error", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                referenceUrl     : { it.altUrl },
                referenceUsername: { it.altUsername },
                referencePassword: { it.altPassword },
        ]

        setup {
            cleanResources("diffChangeLog-test.xml")
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
        expectedException = CommandValidationException.class
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
                url              : "",
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
        expectedException = CommandValidationException.class
    }

    run "Run without any arguments throws an exception", {
        setup {
            database = []
            altDatabase = []

        }
        expectedException = CommandValidationException.class
    }
}
