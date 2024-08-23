package liquibase.extension.testing.command

import static java.util.ResourceBundle.getBundle

import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import liquibase.change.core.AddForeignKeyConstraintChange
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange
import liquibase.exception.CommandValidationException
import liquibase.extension.testing.setup.SetupCleanResources

import java.util.regex.Pattern

CommandTests.define {
    command = ["generateChangelog"]
    signature = """
Short Description: Generate a changelog
Long Description: Writes Change Log XML to copy the current state of the database to standard out or a file
Required Args:
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  author (String) Specifies the author for changesets in the generated changelog
    Default: null
  changelogFile (String) Changelog file to write results
    Default: null
  contextFilter (String) Changeset contexts to generate
    Default: null
  dataOutputDirectory (String) Directory to write table data to
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
  overwriteOutputFile (Boolean) Flag to allow overwriting of output changelog file. Default: false
    Default: false
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
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

    run "Happy path", {
        arguments = [
            url     : { it.url },
            username: { it.username },
            password: { it.password },
            changelogFile: "target/test-classes/changelog-test.xml"
        ]
        setup {
            cleanResources(SetupCleanResources.CleanupMode.CLEAN_ON_SETUP, "changelog-test.xml")
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
    }

    run "File already exists and overwrite parameter is provided", {
        arguments = [
            url     : { it.url },
            username: { it.username },
            password: { it.password },
            changelogFile: "target/test-classes/changelog-test2.xml",
            overwriteOutputFile: true
        ]
        setup {
            copyResource("changelogs/diffChangeLog-test-21938109283.xml", "changelog-test2.xml")
            cleanResources("changelog-test2.xml")
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
                "target/test-classes/changelog-test2.xml" : [CommandTests.assertContains("<changeSet ", 3)]
        ]
    }

    run "File already exists and no overwrite parameter provided", {
        arguments = [
            url     : { it.url },
            username: { it.username },
            password: { it.password },
            changelogFile: "target/test-classes/changelog-test.xml"
        ]
        setup {
            copyResource("changelogs/diffChangeLog-test-21938109283.xml", "changelog-test.xml")
            cleanResources("changelog-test.xml")
        }
        expectedException = CommandValidationException.class
        expectedExceptionMessage = getBundle("liquibase/i18n/liquibase-core").getString("changelogfile.already.exists").replace("%s", "target/test-classes/changelog-test.xml")
    }

    run "Filtering with includeObjects", {
        arguments = [
            url     : { it.url },
            username: { it.username },
            password: { it.password },
            changelogFile: "target/test-classes/changelog-test.xml",
            includeObjects: "table:FIRSTTABLE"
        ]
        setup {
            cleanResources(SetupCleanResources.CleanupMode.CLEAN_ON_SETUP, "changelog-test.xml")
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
                "target/test-classes/changelog-test.xml" : [CommandTests.assertContains("<changeSet ", 1)]
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

    run "Running generateChangelog should add changesets in the correct order", {
        arguments = [
                url              : { it.url },
                username         : { it.username },
                password         : { it.password },
                changelogFile: "target/test-classes/generateChangelog-test.xml",
        ]

        setup {
            cleanResources(SetupCleanResources.CleanupMode.CLEAN_ON_SETUP, "generateChangelog-test.xml")
            database = [
                    new CreateTableChange(
                            tableName: "person",
                            columns: [
                                    ColumnConfig.fromName("address").setType("VARCHAR(255)"),
                                    ColumnConfig.fromName("id").setType("VARCHAR(255)")
                                            .setConstraints(new ConstraintsConfig().setPrimaryKey(true))
                            ]
                    ),
                    new CreateTableChange(
                            tableName: "child",
                            columns: [
                                    ColumnConfig.fromName("name").setType("VARCHAR(255)"),
                                    ColumnConfig.fromName("person_id").setType("VARCHAR(255)")
                            ]
                    ),
                    new AddForeignKeyConstraintChange(
                            referencedTableName: "person",
                            referencedColumnNames: "id",
                            baseTableName: "child",
                            baseColumnNames: "person_id",
                            constraintName: "fk_child_person_id"
                    )

            ]
        }
        expectedFileContent = [
                "target/test-classes/generateChangelog-test.xml" :
                        [
                                CommandTests.assertContains("<changeSet ", 4),
                                Pattern.compile(".*createTable.*createTable.*createIndex.*addForeignKeyConstraint.*", Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE)]
        ]
    }
}
