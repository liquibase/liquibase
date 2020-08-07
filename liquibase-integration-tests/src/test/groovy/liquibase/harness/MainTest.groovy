package liquibase.harness

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.DatabaseVersion
import liquibase.harness.config.TestConfig
import liquibase.harness.config.TestInput
import liquibase.harness.util.DatabaseTestConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.harness.util.SnapshotHelpers
import liquibase.harness.util.TestUtils
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Shared
import spock.lang.Specification


class MainTest extends Specification {

    @Shared TestConfig config

    def setupSpec() {
        config = FileUtils.readYamlConfig("testConfig.yml")
    }

    def "test apply changeset and verify SQL and snapshot"() {

        given:
        Database database = DatabaseTestConnectionUtil.initializeDatabase(testInput)
        Liquibase liquibase = TestUtils.createLiquibase(testInput.getChangeObject(), database)
        //TODO need to provide ability to override default expected file paths
        String expectedSql = FileUtils.getExpectedSqlFileContent(testInput)
        String expectedSnapshot = FileUtils.getExpectedSnapshotFileContent(testInput)
        List<CatalogAndSchema> catalogAndSchemaList = TestUtils.getCatalogAndSchema(database, testInput.getDbSchema())
        ArrayList<String> expectedSqlList = TestUtils.parseValuesToList(expectedSql,"\n")

        when:
        List<String> generatedSql = TestUtils.toSqlFromLiquibaseChangeSets(liquibase);

        then:
        expectedSqlList == generatedSql;

        when:
        cleanDatabase(catalogAndSchemaList, database)
        //TODO make context configurable
        liquibase.update("testContext");
        String jsonSnapshot = SnapshotHelpers.getJsonSnapshot(database, catalogAndSchemaList)
        cleanDatabase(catalogAndSchemaList, database)
        then:
        snapshotMatchesSpecifiedStructure(expectedSnapshot, jsonSnapshot)

        where:
        testInput << buildTestInput(config)
    }

    List<TestInput> buildTestInput(TestConfig config){
        //TODO refactor this
        List<TestInput> inputList = new ArrayList<>();
        for(DatabaseUnderTest databaseUnderTest:config.getDatabasesUnderTest()){
            for(DatabaseVersion databaseVersion:databaseUnderTest.getVersions()){
                for(String changeObject: databaseUnderTest.getChangeObjects())
                    inputList.add(new TestInput(
                            databaseUnderTest.getName(),
                            databaseVersion.getUrl(),
                            databaseUnderTest.getDbSchema(),
                            databaseUnderTest.getUsername(),
                            databaseUnderTest.getPassword(),
                            databaseVersion.getVersion(),
                            changeObject)
                    )
            }
        }
        return inputList;
    }

    void snapshotMatchesSpecifiedStructure(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, new SnapshotHelpers.GeneralSnapshotComparator());
    }

    void cleanDatabase(List<CatalogAndSchema> catalogAndSchemaList, Database database){
        try{
            catalogAndSchemaList.each { database.dropDatabaseObjects(it) }
        }
        catch (Exception e){
            //TODO don't delete objects that i didn't create
        }
    }

}