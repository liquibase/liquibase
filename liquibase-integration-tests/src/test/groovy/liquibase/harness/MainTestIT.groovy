package liquibase.harness

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.harness.util.SnapshotHelpers
import liquibase.harness.util.TestUtils
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll


class MainTestIT extends Specification {

    @Shared
    TestConfig config

    def setupSpec() {
        config = FileUtils.readYamlConfig("testConfig.yml")
    }

    @Unroll
    def "apply #testInput.changeObject for #testInput.databaseName #testInput.version; verify generated SQL and DB snapshot"() {

        given:
        Database database = DatabaseConnectionUtil.initializeDatabase(testInput)
        Liquibase liquibase = TestUtils.createLiquibase(testInput.changeObject, database)
        //TODO need to provide ability to override default expected file paths
        String expectedSql = FileUtils.getExpectedSqlFileContent(testInput)
        String expectedSnapshot = FileUtils.getExpectedSnapshotFileContent(testInput)
        List<CatalogAndSchema> catalogAndSchemaList = TestUtils.getCatalogAndSchema(database, testInput.dbSchema)
        ArrayList<String> expectedSqlList = TestUtils.parseValuesToList(expectedSql, "\n")

        when:
        List<String> generatedSql = TestUtils.toSqlFromLiquibaseChangeSets(liquibase);

        then:
        expectedSqlList == generatedSql;

        when:
        liquibase.update(testInput.context);

        String jsonSnapshot = SnapshotHelpers.getJsonSnapshot(database, catalogAndSchemaList)
        liquibase.rollback(liquibase.databaseChangeLog.changeSets.size(), testInput.context)

        then:
        snapshotMatchesSpecifiedStructure(expectedSnapshot, jsonSnapshot)

        where:
        testInput << TestUtils.buildTestInput(config)
    }

    void snapshotMatchesSpecifiedStructure(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, new SnapshotHelpers.GeneralSnapshotComparator());
    }

}