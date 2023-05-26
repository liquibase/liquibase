package liquibase.command.diff

import liquibase.Scope
import liquibase.change.core.AddForeignKeyConstraintChange
import liquibase.change.core.DropForeignKeyConstraintChange
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.CommandScope
import liquibase.command.core.DiffChangelogCommandStep
import liquibase.command.core.DiffCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.command.core.helpers.DiffOutputControlCommandStep
import liquibase.command.core.helpers.PreCompareCommandStep
import liquibase.command.core.helpers.ReferenceDbUrlConnectionCommandStep
import liquibase.command.util.CommandUtil
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.ObjectChangeFilter
import liquibase.diff.output.StandardObjectChangeFilter
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.parser.core.json.JsonChangeLogParser
import liquibase.resource.SearchPathResourceAccessor
import liquibase.snapshot.SnapshotControl
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.DatabaseObjectFactory
import liquibase.structure.core.Sequence
import liquibase.util.FileUtil
import liquibase.util.StringUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class DiffIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem postgres =
            (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Diff with excludes that reference objects on target should work" () {
        when:
        CommandUtil.runDropAll(postgres)
        CommandUtil.runSnapshot(postgres, "target/test-classes/snapshot.json")
        CommandUtil.runTag(postgres, "1.0.0")
        def diffFile = "target/test-classes/diff.json"
        def url = "offline:postgresql?snapshot=target/test-classes/snapshot.json"
        Database targetDatabase =
                DatabaseFactory.getInstance().openDatabase(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword(), null, new SearchPathResourceAccessor("."))

        def refUrl = "offline:postgresql?snapshot=target/test-classes/snapshot.json"
        Database refDatabase =
                DatabaseFactory.getInstance().openDatabase(refUrl, null, null, null, new SearchPathResourceAccessor("."))
        String excludeObjects = "(?i)DATABASECHANGELOG, (?i)DATABASECHANGELOGLOCK"
        ObjectChangeFilter objectChangeFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, excludeObjects);

        CommandScope commandScope = new CommandScope(DiffCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, refDatabase)
        commandScope.addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, CompareControl.STANDARD)
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, targetDatabase)
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)
        commandScope.addArgumentValue(PreCompareCommandStep.OBJECT_CHANGE_FILTER_ARG, objectChangeFilter)
        def diffOutput = new File(diffFile)
        OutputStream outputStream = new FileOutputStream(diffOutput)
        commandScope.setOutput(outputStream)
        commandScope.execute()
        String outputText = diffOutput.getText().toLowerCase()

        then:
        assert ! outputText.contains("databasechangelog")
        assert ! outputText.contains("databasechangeloglock")

        cleanup:
        try {
            diffOutput.delete()
        } catch (Exception ignored) {

        }
        CommandUtil.runDropAll(postgres)
        postgres.getConnection().close()
        refDatabase.close()
        targetDatabase.close()
    }
}
