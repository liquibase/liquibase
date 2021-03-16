package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.changelog.DatabaseChangeLog
import liquibase.integrationtest.command.LiquibaseCommandTest
import liquibase.integrationtest.setup.SetupDatabaseChangeLog
import liquibase.integrationtest.setup.SetupDatabaseStructure

[
    new LiquibaseCommandTest.Spec(
        command: ["update"],

        setup: [
            new SetupDatabaseChangeLog("changelogs/common/common.tests.changelog.xml")
        ],
        expectedOutput: [
            "",
        ],

    )

] as LiquibaseCommandTest.Spec[]
