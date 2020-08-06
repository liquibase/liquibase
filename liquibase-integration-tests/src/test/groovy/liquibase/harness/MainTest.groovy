package liquibase.harness

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.util.DatabaseTestConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.harness.util.SnapshotHelpers
import liquibase.harness.util.TestUtils
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification


class MainTest extends Specification {
    def setupSpec() {
        Map<String, DatabaseUnderTest> map = FileUtils.readYamlConfig("testConfig.yml")
        println map
    }

    def "test apply changeset and verify SQL and snapshot"() {

        given:
        Database database = DatabaseTestConnectionUtil.initializeDatabase(dbName, dbVersion)
        Liquibase liquibase = TestUtils.createLiquibase(changeLogFile, database)
        String expectedSql = FileUtils.getFileContent("expectedSql", dbName, expectedSqlFile)
        String expectedSnapshot = FileUtils.getFileContent("expectedSnapshot", dbName, expectedSnapshotFile)
        List<CatalogAndSchema> catalogAndSchemaList = TestUtils.getCatalogAndSchema(database, dbSchema)
        ArrayList<String> expectedSqlList = TestUtils.parseValuesToList(expectedSql,"\n")

        when:
        List<String> generatedSql = TestUtils.toSqlFromLiquibaseChangeSets(liquibase);

        then:
        expectedSqlList == generatedSql;

        when:

        catalogAndSchemaList.each { database.dropDatabaseObjects(it) }
        liquibase.update(context);
        String jsonSnapshot = SnapshotHelpers.getJsonSnapshot(database, catalogAndSchemaList)
        catalogAndSchemaList.each { database.dropDatabaseObjects(it) }

        then:
        snapshotMatchesSpecifiedStructure(expectedSnapshot, jsonSnapshot)

        where:
        dbName << ["postgresql", "mysql"]
        dbVersion << ["12", "8"]
        dbSchema << ["public", ""]
        context << ["", ""]
        changeLogFile << ["harness/changelogs/createTable.xml", "harness/changelogs/createTable.xml"]
        expectedSqlFile << ["createTable.sql", "createTable.sql"]
        expectedSnapshotFile << ["createTable.json", "createTable.json"]

    }

    void snapshotMatchesSpecifiedStructure(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, new SnapshotHelpers.GeneralSnapshotComparator());
    }

}