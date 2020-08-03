package liquibase.harness

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.harness.util.DatabaseTestConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.harness.util.SnapshotHelpers
import liquibase.harness.util.TestUtils
import org.skyscreamer.jsonassert.JSONAssert

import spock.lang.Specification


class CreateTable extends Specification {

    def "test generate table and verify SQL and snapshot"() {
        given:

        Database database = DatabaseTestConnectionUtil.initializeDatabase(dbName)
        Liquibase liquibase = TestUtils.createLiquibase(changeLogFile, database)
        List<ChangeSet> changeSets = liquibase.getDatabaseChangeLog().getChangeSets()
        String expectedSql = FileUtils.getFileContent("expectedSql", dbName, expectedSqlFile)
        String expectedSnapshot =  FileUtils.getFileContent("expectedSnapshot", dbName, expectedSnapshotFile)
        List<CatalogAndSchema> catalogAndSchemaList = TestUtils.getCatalogAndSchema(database, dbSchema)
        ArrayList<String> expectedSqlList = TestUtils.parseValuesToList(expectedSql)

        when:
        List<String> generatedSql = TestUtils.toSqlFromChangeSets(changeSets, database);

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
        dbName  << ["postgresql","mysql"]
        dbSchema << ["public", ""]
        context << ["",""]
        changeLogFile << ["harness/changelogs/createTableChangeLog.xml","harness/changelogs/createTableChangeLog.xml"]
        expectedSqlFile << ["createTable.sql","createTable.sql"]
        expectedSnapshotFile << ["createTable.json","createTable.json"]

        }

    void snapshotMatchesSpecifiedStructure(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, new SnapshotHelpers.GeneralSnapshotComparator());
    }

}