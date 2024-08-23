package liquibase.changelog

import liquibase.ContextExpression
import liquibase.LabelExpression
import liquibase.Labels
import liquibase.Scope
import liquibase.change.core.CreateTableChange
import liquibase.change.core.RawSQLChange
import liquibase.change.visitor.ChangeVisitor
import liquibase.database.Database
import liquibase.database.core.MockDatabase
import liquibase.exception.SetupException
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.logging.core.BufferedLogService
import liquibase.parser.ChangeLogParserConfiguration
import liquibase.parser.core.ParsedNode
import liquibase.precondition.core.OrPrecondition
import liquibase.precondition.core.PreconditionContainer
import liquibase.precondition.core.RunningAsPrecondition
import liquibase.resource.Resource
import liquibase.resource.ResourceAccessor
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.util.FileUtil
import org.mockito.Mock
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths
import java.util.logging.Level
import java.util.stream.Stream

class DatabaseChangeLogTest extends Specification {

    @Shared
            resourceSupplier = new ResourceSupplier()
    def test1Xml = '''<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.4.xsd">

    <preConditions>
        <runningAs username="${loginUser}"/>
        <or>
            <dbms type="mssql"/>
            <dbms type="mysql"/>
        </or>
    </preConditions>

    <changeSet id="1" author="nvoxland">
        <createTable tableName="person">
            <column name="id" type="int">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="firstname" type="varchar(50)"/>
            <column name="lastname" type="varchar(50)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
</databaseChangeLog>'''

    def test2Xml = '''<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:pro="http://www.liquibase.org/xml/ns/pro" xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
    http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-latest.xsd ">       

    <changeSet id="1" author="mallod">
        <createTable tableName="city">
            <column name="id" type="int">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="name" type="varchar(50)"/>
            <column name="zipcode" type="varchar(50)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
</databaseChangeLog>'''

    def test3Xml = '''<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:pro="http://www.liquibase.org/xml/ns/pro" xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
    http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-latest.xsd ">       

    <changeSet id="1" author="jlyle" runAlways="true">
        <empty/>
    </changeSet>
</databaseChangeLog>'''

    def testSql = '''-- an included raw sql file
create table sql_table (id int);
create view sql_view as select * from sql_table;'''

    def testProperties = '''context: test'''

    def "getChangeSet passing id, author and file"() {
        def path = "com/example/path.xml"
        def path2 = "com/example/path2.xml"
        when:
        def changeLog = new DatabaseChangeLog(path)
        changeLog.addChangeSet(new ChangeSet("1", "auth", false, false, path, null, null, changeLog))
        changeLog.addChangeSet(new ChangeSet("2", "auth", false, false, path, null, null, changeLog))
        changeLog.addChangeSet(new ChangeSet("1", "other-auth", false, false, path, null, null, changeLog))
        changeLog.addChangeSet(new ChangeSet("1", "auth", false, false, path2, null, null, changeLog)) //path 2
        changeLog.addChangeSet(new ChangeSet("with-dbms", "auth", false, false, path, null, "mock, oracle", changeLog))
        changeLog.addChangeSet(new ChangeSet("with-context", "auth", false, false, path, "test, live", null, changeLog))
        changeLog.addChangeSet(new ChangeSet("with-dbms-and-context", "auth", false, false, path, "test, live", "mock, oracle", changeLog))

        then:
        changeLog.getChangeSet(path, "auth", "1").id == "1"
        changeLog.getChangeSet(path, "other-auth", "1").id == "1"
        changeLog.getChangeSet(path2, "auth", "1").id == "1"
        changeLog.getChangeSet(path, "auth", "2").id == "2"
        changeLog.getChangeSet(path, "auth", "with-dbms").id == "with-dbms"
        changeLog.getChangeSet(path, "auth", "with-context").id == "with-context"
        changeLog.getChangeSet(path, "auth", "with-dbms-and-context").id == "with-dbms-and-context"

        when: "changeLog has properties but no database set"
        changeLog.setChangeLogParameters(new ChangeLogParameters())
        then:
        changeLog.getChangeSet(path, "auth", "with-dbms-and-context").id == "with-dbms-and-context"

        when: "dbms attribute matches database"
        changeLog.getChangeLogParameters().set("database.typeName", "mock")
        then:
        changeLog.getChangeSet(path, "auth", "with-dbms-and-context").id == "with-dbms-and-context"

        when: "dbms attribute does not match database"
        changeLog.setChangeLogParameters(new ChangeLogParameters())
        changeLog.getChangeLogParameters().set("database.typeName", "mysql")
        then:
        changeLog.getChangeSet(path, "auth", "with-dbms-and-context") == null
    }

    def "load handles both changes and preconditions"() {
        when:
        def children = [
                new ParsedNode(null, "preConditions").setValue([[runningAs: [username: "user1"]], [runningAs: [username: "user2"]]]),
                new ParsedNode(null, "changeSet").addChildren([id: "1", author: "nvoxland", createTable: [tableName: "my_table"]]),
                new ParsedNode(null, "changeSet").addChildren([id: "2", author: "nvoxland", createTable: [tableName: "my_other_table"]]),
        ]
        def nodeWithChildren = new ParsedNode(null, "databaseChangeLog").addChildren([logicalFilePath: "com/example/logical.xml"])
        for (child in children) {
            nodeWithChildren.addChild(child)
        }
        def nodeWithValue = new ParsedNode(null, "databaseChangeLog").addChildren([logicalFilePath: "com/example/logical.xml"]).setValue(children)

        def changeLogFromChildren = new DatabaseChangeLog()
        def changeLogFromValue = new DatabaseChangeLog()

        changeLogFromValue.load(nodeWithChildren, resourceSupplier.simpleResourceAccessor)
        changeLogFromChildren.load(nodeWithValue, resourceSupplier.simpleResourceAccessor)

        then:
        changeLogFromChildren.preconditions.nestedPreconditions[0].nestedPreconditions.size() == 2
        changeLogFromValue.preconditions.nestedPreconditions[0].nestedPreconditions.size() == 2

        ((RunningAsPrecondition) changeLogFromChildren.preconditions.nestedPreconditions[0].nestedPreconditions[0]).username == "user1"
        ((RunningAsPrecondition) changeLogFromValue.preconditions.nestedPreconditions[0].nestedPreconditions[0]).username == "user1"

        ((RunningAsPrecondition) changeLogFromChildren.preconditions.nestedPreconditions[0].nestedPreconditions[1]).username == "user2"
        ((RunningAsPrecondition) changeLogFromValue.preconditions.nestedPreconditions[0].nestedPreconditions[1]).username == "user2"

        changeLogFromChildren.changeSets.size() == 2
        changeLogFromValue.changeSets.size() == 2

        ((CreateTableChange) changeLogFromChildren.changeSets[0].changes[0]).tableName == "my_table"
        ((CreateTableChange) changeLogFromValue.changeSets[0].changes[0]).tableName == "my_table"

        ((CreateTableChange) changeLogFromChildren.changeSets[1].changes[0]).tableName == "my_other_table"
        ((CreateTableChange) changeLogFromValue.changeSets[1].changes[0]).tableName == "my_other_table"
    }
    def "load handles removeChangeSetProperty"() {
        when:
        def children = [
                new ParsedNode(null, "removeChangeSetProperty").setValue([change: "addColumn", dbms: "mock", "remove": "afterColumn"]),
                new ParsedNode(null, "changeSet").addChildren([id: "1", author: "kirangodishala", createTable: [tableName: "my_table"]]),
        ]
        def nodeWithChildren = new ParsedNode(null, "databaseChangeLog").addChildren([logicalFilePath: "com/example/logical.xml"])
        for (child in children) {
            nodeWithChildren.addChild(child)
        }

        def changeLogFromChildren = new DatabaseChangeLog()
        Database database = new MockDatabase();
        changeLogFromChildren.setChangeLogParameters(new ChangeLogParameters(database))

        changeLogFromChildren.load(nodeWithChildren, resourceSupplier.simpleResourceAccessor)

        then:

        changeLogFromChildren.changeVisitors.size() == 1
        changeLogFromChildren.changeSets.size() == 1


        ((ChangeVisitor) changeLogFromChildren.changeVisitors[0]).change == "addColumn"
        ((ChangeVisitor) changeLogFromChildren.changeVisitors[0]).dbms == ["mock"] as HashSet
        ((ChangeVisitor) changeLogFromChildren.changeVisitors[0]).remove == "afterColumn"

    }

    def "included changelog files have their preconditions and changes included in root changelog"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/test1.xml": test1Xml, "com/example/test2.xml": test1Xml.replace("\${loginUser}", "otherUser").replace("person", "person2")])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.setChangeLogParameters(new ChangeLogParameters())
        rootChangeLog.getChangeLogParameters().set("loginUser", "testUser")

        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChild(new ParsedNode(null, "preConditions").addChildren([runningAs: [username: "user1"]]))
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([include: [file: "com/example/test1.xml"]])
                .addChildren([include: [file: "com/example/test2.xml"]])
                , resourceAccessor)


        then:
        rootChangeLog.preconditions.nestedPreconditions.size() == 3
        ((RunningAsPrecondition) rootChangeLog.preconditions.nestedPreconditions[0].nestedPreconditions[0]).username == "user1"

        ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[1]).nestedPreconditions.nestedPreconditions[0].size() == 2
        ((RunningAsPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[1]).nestedPreconditions[0].nestedPreconditions[0]).username == "testUser"
        ((OrPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[1]).nestedPreconditions[0].nestedPreconditions[1]).nestedPreconditions.size() == 2

        ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions.nestedPreconditions[0].size() == 2
        ((RunningAsPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions[0].nestedPreconditions[0]).username == "otherUser"
        ((OrPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions[0].nestedPreconditions[1]).nestedPreconditions.size() == 2

        rootChangeLog.changeSets.size() == 3
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/root.xml", "nvoxland", "1").changes[0]).tableName == "test_table"
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/test1.xml", "nvoxland", "1").changes[0]).tableName == "person"
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/test2.xml", "nvoxland", "1").changes[0]).tableName == "person2"
    }

    def "included changelog files inside modifyChangeSets set runWith"() {
        when:
        def resourceAccessor =
                new MockResourceAccessor(["com/example/test1.xml": test1Xml, "com/example/test2.xml": test1Xml
                        .replace("\${loginUser}", "otherUser")
                        .replace("person", "person2")])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.setChangeLogParameters(new ChangeLogParameters())
        rootChangeLog.getChangeLogParameters().set("loginUser", "testUser")


        def topLevel =
                new ParsedNode(null, "databaseChangeLog")
                        .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
        def modifyNode =
                new ParsedNode(null, "modifyChangeSets").addChildren([runWith: "psql"])
        modifyNode
                .addChildren([include: [file: "com/example/test1.xml"]])
                .addChildren([include: [file: "com/example/test2.xml"]])
        topLevel.addChild(modifyNode)
        rootChangeLog.load(topLevel, resourceAccessor)


        then:

        rootChangeLog.changeSets.size() == 3
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/root.xml", "nvoxland", "1").changes[0]).tableName == "test_table"
        rootChangeLog.getChangeSet("com/example/test1.xml", "nvoxland", "1").getRunWith() == "psql"
        rootChangeLog.getChangeSet("com/example/test2.xml", "nvoxland", "1").getRunWith() == "psql"
    }

    def "includeAll files have preconditions and changeSets loaded"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/test1.xml": test1Xml,
                "com/example/test2.xml": test1Xml.replace("\${loginUser}", "otherUser").replace("person", "person2"),
                "com/example/test.sql" : testSql
        ])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.setChangeLogParameters(new ChangeLogParameters())
        rootChangeLog.getChangeLogParameters().set("loginUser", "testUser")
        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChild(new ParsedNode(null, "preConditions").addChildren([runningAs: [username: "user1"]]))
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([includeAll: [path: "com/example", resourceComparator: "liquibase.changelog.ReversedChangeLogNamesComparator"]])
                , resourceAccessor)

        then:
        rootChangeLog.preconditions.nestedPreconditions.size() == 4
        ((RunningAsPrecondition) rootChangeLog.preconditions.nestedPreconditions[0].nestedPreconditions[0]).username == "user1"

        ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[1]).nestedPreconditions[0].nestedPreconditions.size() == 2

        ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions[0].nestedPreconditions.size() == 2
        ((RunningAsPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions[0].nestedPreconditions[0]).username == "testUser"
        ((OrPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions[0].nestedPreconditions[1]).nestedPreconditions.size() == 2

        ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[3]).nestedPreconditions.size() == 0

        rootChangeLog.changeSets.size() == 4
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/root.xml", "nvoxland", "1").changes[0]).tableName == "test_table"
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/test1.xml", "nvoxland", "1").changes[0]).tableName == "person"
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/test2.xml", "nvoxland", "1").changes[0]).tableName == "person2"
        ((RawSQLChange) rootChangeLog.getChangeSet("com/example/test.sql", "includeAll", "raw").changes[0]).sql == testSql

        // assert reversed order
        ((CreateTableChange) rootChangeLog.getChangeSets().get(0).changes[0]).tableName == "test_table"
        ((CreateTableChange) rootChangeLog.getChangeSets().get(2).changes[0]).tableName == "person"
        ((CreateTableChange) rootChangeLog.getChangeSets().get(1).changes[0]).tableName == "person2"
        ((RawSQLChange) rootChangeLog.getChangeSets().get(3).changes[0]).sql == testSql
    }

    def "includeAll files inside modifyChangeSets set runWith"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/test1.xml": test1Xml,
                "com/example/test2.xml": test1Xml.replace("\${loginUser}", "otherUser").replace("person", "person2"),
                "com/example/test.sql" : testSql
        ])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.setChangeLogParameters(new ChangeLogParameters())
        rootChangeLog.getChangeLogParameters().set("loginUser", "testUser")
        def topLevel =
                new ParsedNode(null, "databaseChangeLog")
                        .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
        def modifyNode =
                new ParsedNode(null, "modifyChangeSets").addChildren([runWith: "psql"])
        modifyNode
                .addChildren([includeAll: [path: "com/example", resourceComparator: "liquibase.changelog.ReversedChangeLogNamesComparator"]])
        topLevel.addChild(modifyNode)
        rootChangeLog.load(topLevel, resourceAccessor)

        then:

        rootChangeLog.changeSets.size() == 4
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/root.xml", "nvoxland", "1").changes[0]).tableName == "test_table"
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/test1.xml", "nvoxland", "1").changes[0]).tableName == "person"
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/test2.xml", "nvoxland", "1").changes[0]).tableName == "person2"
        rootChangeLog.getChangeSet("com/example/test1.xml", "nvoxland", "1").getRunWith() == "psql"
        rootChangeLog.getChangeSet("com/example/test2.xml", "nvoxland", "1").getRunWith() == "psql"
        ((RawSQLChange) rootChangeLog.getChangeSet("com/example/test.sql", "includeAll", "raw").changes[0]).sql == testSql

        // assert reversed order
        ((CreateTableChange) rootChangeLog.getChangeSets().get(0).changes[0]).tableName == "test_table"
        ((CreateTableChange) rootChangeLog.getChangeSets().get(2).changes[0]).tableName == "person"
        ((CreateTableChange) rootChangeLog.getChangeSets().get(1).changes[0]).tableName == "person2"
        ((RawSQLChange) rootChangeLog.getChangeSets().get(3).changes[0]).sql == testSql
    }

    def "included changelogs inherit contexts, labels, and ignores via load()"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/test1.xml": test1Xml, "com/example/test2.xml": test1Xml.replace("testUser", "otherUser").replace("person", "person2")])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChild(new ParsedNode(null, "preConditions").addChildren([runningAs: [username: "user1"]]))
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([include: [file: "com/example/test1.xml", context: "context1", labels: "label1"]])
                .addChildren([include: [file: "com/example/test2.xml", context: "context2", labels: "label2", ignore: true]])
                , resourceAccessor)

        def test1ChangeLog = rootChangeLog.getChangeSet("com/example/test1.xml", "nvoxland", "1").getChangeLog()
        def test2ChangeLog = rootChangeLog.getChangeSet("com/example/test2.xml", "nvoxland", "1").getChangeLog()

        then:
        test1ChangeLog.getIncludeLabels().getLabels().size() == 1
        test1ChangeLog.getIncludeLabels().getLabels()[0] == "label1"
        test1ChangeLog.getIncludeContextFilter().getContexts().size() == 1
        test1ChangeLog.getIncludeContextFilter().getContexts()[0] == "context1"
        test1ChangeLog.isIncludeIgnore() == false

        test2ChangeLog.getIncludeLabels().getLabels().size() == 1
        test2ChangeLog.getIncludeLabels().getLabels()[0] == "label2"
        test2ChangeLog.getIncludeContextFilter().getContexts().size() == 1
        test2ChangeLog.getIncludeContextFilter().getContexts()[0] == "context2"
        test2ChangeLog.isIncludeIgnore() == true

    }

    def "included changelogs inherit contexts, labels, and ignores via include()"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/test1.xml": test1Xml,
                "com/example/test2.xml": test1Xml.replace("testUser", "otherUser").replace("person", "person2")
        ])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.include("com/example/test1.xml", false, true, resourceAccessor, new ContextExpression("context1"), new Labels("label1"), false, false)
        rootChangeLog.include("com/example/test2.xml", false, true, resourceAccessor, new ContextExpression("context2"), new Labels("label2"), true, false)

        def test1ChangeLog = rootChangeLog.getChangeSet("com/example/test1.xml", "nvoxland", "1").getChangeLog()
        def test2ChangeLog = rootChangeLog.getChangeSet("com/example/test2.xml", "nvoxland", "1").getChangeLog()

        then:
        test1ChangeLog.getIncludeLabels().getLabels().size() == 1
        test1ChangeLog.getIncludeLabels().getLabels()[0] == "label1"
        test1ChangeLog.getIncludeContextFilter().getContexts().size() == 1
        test1ChangeLog.getIncludeContextFilter().getContexts()[0] == "context1"
        test1ChangeLog.isIncludeIgnore() == false

        test2ChangeLog.getIncludeLabels().getLabels().size() == 1
        test2ChangeLog.getIncludeLabels().getLabels()[0] == "label2"
        test2ChangeLog.getIncludeContextFilter().getContexts().size() == 1
        test2ChangeLog.getIncludeContextFilter().getContexts()[0] == "context2"
        test2ChangeLog.isIncludeIgnore() == true
    }

    def "includeAll executes include in alphabetical order"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/children/file2.sql": "file 2",
                "com/example/children/file3.sql": "file 3",
                "com/example/children/file1.sql": "file 1",
                "com/example/not/fileX.sql"     : "file X",
        ])
        def changeLogFile = new DatabaseChangeLog("com/example/root.xml")
        changeLogFile
                .includeAll("com/example/children", false, null, true, changeLogFile.getStandardChangeLogComparator(), resourceAccessor, new ContextExpression(), new Labels(), false, 0,
                        Integer.MAX_VALUE)

        then:
        changeLogFile.changeSets.collect { it.filePath } == ["com/example/children/file1.sql",
                                                             "com/example/children/file2.sql",
                                                             "com/example/children/file3.sql"]
    }

    def "includeAll empty relative path"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/children/file2.sql": "file 2",
                "com/example/children/file3.sql": "file 3",
                "com/example/children/file1.sql": "file 1",
                "com/example/not/fileX.sql"     : "file X",
        ]) {
            private callingPath

            @Override
            List<Resource> search(String path, ResourceAccessor.SearchOptions searchOption) throws IOException {
                callingPath = path
                return super.search(path, searchOption.getRecursive())
            }
        }
        def changeLogFile = new DatabaseChangeLog("com/example/children/root.xml")
        changeLogFile.includeAll("", true, { r -> r != changeLogFile.physicalFilePath }, true, changeLogFile.getStandardChangeLogComparator(), resourceAccessor, new ContextExpression(), new LabelExpression(), false)

        then:
        resourceAccessor.callingPath == "com/example/children/"
    }

    @Unroll("#featureName: #changeSets")
    def "addChangeSet works with first/last combinations"() {
        when:
        def changeLog = new DatabaseChangeLog()
        for (def changeSetDef : changeSets) {
            def changeSet = new ChangeSet(changeSetDef["id"], "test", false, false, "path.txt", null, null, true, changeLog)
            changeSet.runOrder = changeSetDef["runOrder"]
            changeLog.addChangeSet(changeSet)
        }

        then:
        changeLog.getChangeSets()*.getId() == order

        where:
        changeSets                                                                                                                                              | order
        [[id: "1"], [id: "2"], [id: "3"]]                                                                                                                       | ["1", "2", "3"]
        [[id: "1", runOrder: "first"]]                                                                                                                          | ["1"]
        [[id: "1", runOrder: "first"], [id: "2"]]                                                                                                               | ["1", "2"]
        [[id: "1"], [id: "2", runOrder: "first"]]                                                                                                               | ["2", "1"]
        [[id: "1"], [id: "2", runOrder: "first"], [id: "3"]]                                                                                                    | ["2", "1", "3"]
        [[id: "1"], [id: "2", runOrder: "first"], [id: "3", runOrder: "first"], [id: "4"]]                                                                      | ["2", "3", "1", "4"]
        [[id: "1"], [id: "2", runOrder: "first"], [id: "3"], [id: "4", runOrder: "first"], [id: "5"]]                                                           | ["2", "4", "1", "3", "5"]
        [[id: "1", runOrder: "last"]]                                                                                                                           | ["1"]
        [[id: "1", runOrder: "last"], [id: "2"]]                                                                                                                | ["2", "1"]
        [[id: "1", runOrder: "last"], [id: "2"]]                                                                                                                | ["2", "1"]
        [[id: "1"], [id: "2", runOrder: "last"]]                                                                                                                | ["1", "2"]
        [[id: "1", runOrder: "last"], [id: "2"], [id: "3", runOrder: "last"], [id: "4"]]                                                                        | ["2", "4", "1", "3"]
        [[id: "1", runOrder: "last"], [id: "2"], [id: "3", runOrder: "first"], [id: "4"], [id: "5", runOrder: "last"], [id: "6"], [id: "7", runOrder: "first"]] | ["3", "7", "2", "4", "6", "1", "5"]
    }

    def "includeAll throws exception when directory not found"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/children/file2.sql": "file 2",
                "com/example/children/file3.sql": "file 3",
                "com/example/children/file1.sql": "file 1",
                "com/example/not/fileX.sql"     : "file X",
        ])
        def changeLogFile = new DatabaseChangeLog("com/example/root.xml")
        changeLogFile.includeAll("com/example/missing", false, null, true, changeLogFile.getStandardChangeLogComparator(), resourceAccessor, new ContextExpression(), new Labels(), false, 0, Integer.MAX_VALUE)

        then:
        def e = thrown(SetupException)
        assert e.getMessage().startsWith("Could not find directory, directory was empty, or no changelogs matched the provided search criteria for includeAll '")
    }

    def "includeAll throws exception when circular reference is detected"() {
        when:
        def changelogText = """<?xml version="1.1" encoding="UTF-8" standalone="no"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog" 
xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext" 
xmlns:pro="http://www.liquibase.org/xml/ns/pro" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext 
http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd 
http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-pro-latest.xsd
http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

        <includeAll path="include-all-dir" labels="none" context="none"/>

</databaseChangeLog>
""".trim()

        def resourceAccessor = new MockResourceAccessor([
                "include-all.xml"                : changelogText,
                "include-all-dir/include-all.xml": changelogText,
        ])
        def changeLogFile = new DatabaseChangeLog("com/example/root.xml")
        changeLogFile.includeAll("include-all-dir", false, null, true, changeLogFile.getStandardChangeLogComparator(), resourceAccessor, new ContextExpression(), new Labels(), false, 0, Integer.MAX_VALUE)

        then:
        SetupException e = thrown()
        assert e.getMessage().startsWith("liquibase.exception.SetupException:") &&
               e.getMessage().contains("Circular reference detected in 'include-all-dir/'. Set liquibase.errorOnCircularIncludeAll if you'd like to ignore this error.")
    }

    def "includeAll throws no exception when directory not found and errorIfMissing is false"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/children/file2.sql": "file 2",
                "com/example/children/file3.sql": "file 3",
                "com/example/children/file1.sql": "file 1",
                "com/example/not/fileX.sql"     : "file X",
        ])
        def changeLogFile = new DatabaseChangeLog("com/example/root.xml")
        changeLogFile.includeAll("com/example/missing", false, null, false, changeLogFile.getStandardChangeLogComparator(), resourceAccessor, new ContextExpression(), new Labels(), false, 0, Integer.MAX_VALUE)
        then:
        changeLogFile.changeSets.collect { it.filePath } == []

    }

    def "include fails if no parser supports the file"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/test1.invalid": test1Xml])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")

        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChild(new ParsedNode(null, "preConditions").addChildren([runningAs: [username: "user1"]]))
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([include: [file: "com/example/test1.invalid"]])
                , resourceAccessor)


        then:
        def e = thrown(SetupException)
        e.message == "Cannot find parser that supports com/example/test1.invalid"
    }

    def "include fails if XML file is empty"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/test1.xml": ""])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")

        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([include: [file: "com/example/test1.xml"]])
                , resourceAccessor)


        then:
        def e = thrown(SetupException)
        e.getMessage().contains("Premature end of file.")
    }

    def "include fails if SQL file is empty"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/test1.sql": ""])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")

        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([include: [file: "com/example/test1.sql"]])
                , resourceAccessor)


        then:
        def e = thrown(SetupException)
        e.getMessage().contains("Unable to parse empty file")
    }

    def "include fails if JSON file is empty"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/test1.json": ""])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")

        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([include: [file: "com/example/test1.json"]])
                , resourceAccessor)


        then:
        def e = thrown(SetupException)
        e.getMessage().contains("Empty file com/example/test1.json")
    }

    def "include fails if file is empty"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/test1.xml": test1Xml])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")

        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChild(new ParsedNode(null, "preConditions").addChildren([runningAs: [username: "user1"]]))
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([include: [file: "com/example/invalid.xml"]])
                , resourceAccessor)


        then:
        ChangeLogParserConfiguration.ON_MISSING_INCLUDE_CHANGELOG.getCurrentValue() == ChangeLogParserConfiguration.MissingIncludeConfiguration.FAIL
        def e = thrown(SetupException)
        e.message.startsWith("The file com/example/invalid.xml was not found in")
    }

    def "properties values are correctly loaded and stored when properties file is relative to changelog"() {
        when:
        def propertiesResourceAccessor = new MockResourceAccessor(["com/example/file.properties": testProperties])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.setChangeLogParameters(new ChangeLogParameters())

        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([property: [file: "file.properties", relativeToChangelogFile: "true", errorIfMissing: "true"]]),
                propertiesResourceAccessor)

        then:
        rootChangeLog.getChangeLogParameters().hasValue("context", rootChangeLog)
        rootChangeLog.getChangeLogParameters().getValue("context", rootChangeLog) == "test"
    }

    def "properties values are not loaded and stored when file it's not relative to changelog"() {
        when:
        def propertiesResourceAccessor = new MockResourceAccessor(["com/example/file.properties": testProperties])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.setChangeLogParameters(new ChangeLogParameters())

        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([property: [file: "file.properties", errorIfMissing: "false"]]),
                propertiesResourceAccessor)

        then:
        rootChangeLog.getChangeLogParameters().hasValue("context", rootChangeLog) == false
    }

    @Unroll
    def "an error is thrown when properties file is not found and is set to error"() {
        when:
        def propertiesResourceAccessor = new MockResourceAccessor(["com/example/file.properties": testProperties])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.setChangeLogParameters(new ChangeLogParameters())

        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([property: [errorIfMissing: errorIfMissingDef, relativeToChangelogFile: relativeToChangelogFileDef, file: fileDef]]),
                propertiesResourceAccessor)

        then:
        def e = thrown(UnexpectedLiquibaseException)
        assert e.getMessage() == FileUtil.getFileNotFoundMessage(fileDef)

        where:
        errorIfMissingDef | relativeToChangelogFileDef | fileDef
        null              | null                       | "file.properties"
        null              | false                      | "file.properties"
        null              | true                       | "com/example/file.properties"
        true              | null                       | "file.properties"
        true              | false                      | "file.properties"
        true              | true                       | "com/example/file.properties"
    }

    @Unroll
    def "no error is thrown when properties file is not found and errorIfMissing flag is either set as false or null"() {
        when:
        def propertiesResourceAccessor = new MockResourceAccessor(["com/example/file.properties": testProperties])

        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.setChangeLogParameters(new ChangeLogParameters())

        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([changeSet: [id: "1", author: "nvoxland", createTable: [tableName: "test_table", schemaName: "test_schema"]]])
                .addChildren([property: [errorIfMissing: errorIfMissingDef, relativeToChangelogFile: relativeToChangelogFileDef, file: fileDef]]),
                propertiesResourceAccessor)

        then:
        rootChangeLog.getChangeLogParameters().hasValue("context", rootChangeLog)
        rootChangeLog.getChangeLogParameters().getValue("context", rootChangeLog) == "test"

        where:
        errorIfMissingDef | relativeToChangelogFileDef | fileDef
        null              | null                       | "com/example/file.properties"
        null              | false                      | "com/example/file.properties"
        null              | true                       | "file.properties"
        true              | null                       | "com/example/file.properties"
        true              | false                      | "com/example/file.properties"
        true              | true                       | "file.properties"
        false             | null                       | "com/example/file.properties"
        false             | false                      | "com/example/file.properties"
        false             | true                       | "file.properties"
    }

    @Unroll
    def "normalizePath: #path"() {
        expect:
        DatabaseChangeLog.normalizePath(path) == expected

        where:
        path                                  | expected
        "changelog.xml"                       | "changelog.xml"
        "path/to/changelog.xml"               | "path/to/changelog.xml"
        "/path/to/changelog.xml"              | "path/to/changelog.xml"
        "./path/to/changelog.xml"             | "path/to/changelog.xml"
        "classpath:./path/to/changelog.xml"   | "path/to/changelog.xml"
        "classpath:path/to/changelog.xml"     | "path/to/changelog.xml"
        "classpath:/path/to/changelog.xml"    | "path/to/changelog.xml"
        "\\path\\to\\changelog.xml"           | "path/to/changelog.xml"
        ".\\path\\to\\changelog.xml"          | "path/to/changelog.xml"
        "path\\to\\changelog.xml"             | "path/to/changelog.xml"
        "path\\.\\to\\.\\changelog.xml"       | "path/to/changelog.xml"
        "c:\\path\\to\\changelog.xml"         | "path/to/changelog.xml"
        "c:/path/to/changelog.xml"            | "path/to/changelog.xml"
        "D:\\a\\liquibase\\DBDocTaskTest.xml" | "a/liquibase/DBDocTaskTest.xml"
        "..\\path\\to\\changelog.xml"         | "../path/to/changelog.xml"
        "../path/changelog.xml"               | "../path/changelog.xml"
        "..\\..\\path\\changelog.xml"         | "../../path/changelog.xml"
        "../../path/changelog.xml"            | "../../path/changelog.xml"
        "path/../path/changelog.xml"          | "path/changelog.xml"
    }

    def "relative paths for changelog include are resolved as normalized path"() {
        given:
        def changelog = new DatabaseChangeLog("com/example/root1.xml")
        def resourceAccessor = new MockResourceAccessor(["com/example/root1.xml"               : test1Xml,
                                                         "path/changelog.xml": test2Xml])

        when:
        boolean result = changelog.include("../../path/changelog.xml", true, false,
                resourceAccessor, null, new Labels(), false, DatabaseChangeLog.OnUnknownFileFormat.WARN)

        then:
        result
    }


    def "warning message is logged when changelog include fails because file does not exist"() {
        when:
        def rootChangeLogPath = "com/example/root.xml"
        def includedChangeLogPath = "com/example/test1.xml"
        def resourceAccessor = new MockResourceAccessor([(rootChangeLogPath): test1Xml])

        def rootChangeLog = new DatabaseChangeLog(rootChangeLogPath)
        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog"), resourceAccessor)

        BufferedLogService bufferLog = new BufferedLogService()

        Scope.child([
                (Scope.Attr.logService.name())                                      : bufferLog,
                (ChangeLogParserConfiguration.ON_MISSING_INCLUDE_CHANGELOG.getKey()): ChangeLogParserConfiguration.MissingIncludeConfiguration.WARN,
        ], new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                rootChangeLog
                        .include(includedChangeLogPath, false, true, resourceAccessor, null, null, false, null, null);
            }
        })

        then:
        bufferLog.getLogAsString(Level.WARNING).contains(FileUtil.getFileNotFoundMessage(includedChangeLogPath));
    }

    @Unroll
    def "includeAll finds all expected changelogs with MinDepth: #minDepth and MaxDepth: #maxDepth"() {
        when:
        def rootChangeLogPath = "com/example/root.xml"
        def includedAllChangeLogPath = "changelogs"
        def resourceAccessor = new MockResourceAccessor(["com/example/root.xml"                              : "",
                                                         "changelogs/changelog-1.xml"                        : test2Xml,
                                                         "changelogs/morechangelogs/changelog-2.xml"         : test2Xml,
                                                         "changelogs/morechangelogs/withMore/changelog-3.xml": test2Xml,
                                                         "changelogs/morechangelogs/AndMore/changelog-4.xml" : test2Xml])

        def rootChangeLog = new DatabaseChangeLog(rootChangeLogPath)
        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([includeAll: [path: includedAllChangeLogPath, minDepth: minDepth, maxDepth: maxDepth, errorIfMissingOrEmpty: false]]), resourceAccessor)

        then:
        rootChangeLog.getChangeSets().size() == expectedIncludeAllChangesetsToDeploy

        where:
        minDepth | maxDepth          | expectedIncludeAllChangesetsToDeploy
        0        | 0                 | 0
        0        | 1                 | 1
        1        | 1                 | 1
        1        | 3                 | 4
        0        | 2                 | 2
        0        | Integer.MAX_VALUE | 4
    }

    @Unroll
    def "includeAll (various scenarios) finds all expected changelogs with MinDepth: #minDepth and MaxDepth: #maxDepth"() {
        when:
        def relativeToken = "#RELATIVE_TO_CHANGELOG_FILE#"
        def pathToken = "#PATH#"
        def childChangelogXml = '''<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:pro="http://www.liquibase.org/xml/ns/pro" xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
    http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-latest.xsd ">       

    <includeAll errorIfMissingOrEmpty="false" relativeToChangelogFile="''' + relativeToken + '''" path="''' + pathToken + '''" minDepth="''' + minDepth + '''" maxDepth="''' + maxDepth + '''" />
</databaseChangeLog>'''

        // Multiple scenarios are included in the same test because all scenarios should produce the same results and
        // consistency needs to be tested across scenarios

        // Scenario 1: includeAll in root changelog, relativeToChangelogFile false, path from root
        def s1RootPath = "com/example"
        def s1RootChangelog = s1RootPath + "/s1.xml"
        def s1ChangelogPathRelative = "s1"
        def s1ChangelogPath = s1ChangelogPathRelative
        def s1ResourceMap = new HashMap<String, String>()
        s1ResourceMap.put(s1RootChangelog,                              "")
        s1ResourceMap.put(s1ChangelogPath + "/changelog.xml",           test2Xml)
        s1ResourceMap.put(s1ChangelogPath + "/a/changelog.xml",         test2Xml)
        s1ResourceMap.put(s1ChangelogPath + "/a/b/changelog-1.xml",     test2Xml)
        s1ResourceMap.put(s1ChangelogPath + "/a/b/changelog-2.xml",     test2Xml)
        s1ResourceMap.put(s1ChangelogPath + "/a/b/c/changelog.xml",     test2Xml)
        s1ResourceMap.put(s1ChangelogPath + "/a/b/c/d/changelog.xml",   test2Xml)
        s1ResourceMap.put(s1ChangelogPath + "/a/b/c/d/e/changelog.xml", test2Xml)
        def s1ResourceAccessor = new MockResourceAccessor(s1ResourceMap)

        def s1DatabaseChangeLog = new DatabaseChangeLog(s1RootChangelog)
        s1DatabaseChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([includeAll: [relativeToChangelogFile: false, path: s1ChangelogPath, minDepth: minDepth, maxDepth: maxDepth, errorIfMissingOrEmpty: false]]), s1ResourceAccessor)


        // Scenario 2: includeAll in root changelog, relativeToChangelogFile false, path from child
        def s2RootPath = "com/example"
        def s2RootChangelog = s2RootPath + "/s2.xml"
        def s2ChangelogPathRelative = "s2"
        def s2ChangelogPath = s2RootPath + "/" + s2ChangelogPathRelative
        def s2ResourceMap = new HashMap<String, String>()
        s2ResourceMap.put(s2RootChangelog,                              "")
        s2ResourceMap.put(s2ChangelogPath + "/changelog.xml",           test2Xml)
        s2ResourceMap.put(s2ChangelogPath + "/a/changelog.xml",         test2Xml)
        s2ResourceMap.put(s2ChangelogPath + "/a/b/changelog-1.xml",     test2Xml)
        s2ResourceMap.put(s2ChangelogPath + "/a/b/changelog-2.xml",     test2Xml)
        s2ResourceMap.put(s2ChangelogPath + "/a/b/c/changelog.xml",     test2Xml)
        s2ResourceMap.put(s2ChangelogPath + "/a/b/c/d/changelog.xml",   test2Xml)
        s2ResourceMap.put(s2ChangelogPath + "/a/b/c/d/e/changelog.xml", test2Xml)
        def s2ResourceAccessor = new MockResourceAccessor(s2ResourceMap)

        def s2DatabaseChangeLog = new DatabaseChangeLog(s2RootChangelog)
        s2DatabaseChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([includeAll: [relativeToChangelogFile: false, path: s2ChangelogPath, minDepth: minDepth, maxDepth: maxDepth, errorIfMissingOrEmpty: false]]), s2ResourceAccessor)

        // Scenario 3: includeAll in root changelog, relativeToChangelogFile true, path single child
        def s3RootPath = "com/example"
        def s3RootChangelog = s3RootPath + "/s3.xml"
        def s3ChangelogPathRelative = "s3"
        def s3ChangelogPath = s3RootPath + "/" + s3ChangelogPathRelative
        def s3ResourceMap = new HashMap<String, String>()
        s3ResourceMap.put(s3RootChangelog,                              "")
        s3ResourceMap.put(s3ChangelogPath + "/changelog.xml",           test2Xml)
        s3ResourceMap.put(s3ChangelogPath + "/a/changelog.xml",         test2Xml)
        s3ResourceMap.put(s3ChangelogPath + "/a/b/changelog-1.xml",     test2Xml)
        s3ResourceMap.put(s3ChangelogPath + "/a/b/changelog-2.xml",     test2Xml)
        s3ResourceMap.put(s3ChangelogPath + "/a/b/c/changelog.xml",     test2Xml)
        s3ResourceMap.put(s3ChangelogPath + "/a/b/c/d/changelog.xml",   test2Xml)
        s3ResourceMap.put(s3ChangelogPath + "/a/b/c/d/e/changelog.xml", test2Xml)
        def s3ResourceAccessor = new MockResourceAccessor(s3ResourceMap)

        def s3DatabaseChangeLog = new DatabaseChangeLog(s3RootChangelog)
        s3DatabaseChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([includeAll: [relativeToChangelogFile: true, path: s3ChangelogPathRelative, minDepth: minDepth, maxDepth: maxDepth, errorIfMissingOrEmpty: false]]), s3ResourceAccessor)


        // Scenario 4: includeAll in root changelog, relativeToChangelogFile true, path multi child
        def s4RootPath = "com/example"
        def s4RootChangelog = s4RootPath + "/s4.xml"
        def s4ChangelogPathRelative = "s4_1/s4_2/s4_3"
        def s4ChangelogPath = s4RootPath + "/" + s4ChangelogPathRelative
        def s4ResourceMap = new HashMap<String, String>()
        s4ResourceMap.put(s4RootChangelog,                              "")
        s4ResourceMap.put(s4ChangelogPath + "/changelog.xml",           test2Xml)
        s4ResourceMap.put(s4ChangelogPath + "/a/changelog.xml",         test2Xml)
        s4ResourceMap.put(s4ChangelogPath + "/a/b/changelog-1.xml",     test2Xml)
        s4ResourceMap.put(s4ChangelogPath + "/a/b/changelog-2.xml",     test2Xml)
        s4ResourceMap.put(s4ChangelogPath + "/a/b/c/changelog.xml",     test2Xml)
        s4ResourceMap.put(s4ChangelogPath + "/a/b/c/d/changelog.xml",   test2Xml)
        s4ResourceMap.put(s4ChangelogPath + "/a/b/c/d/e/changelog.xml", test2Xml)
        def s4ResourceAccessor = new MockResourceAccessor(s4ResourceMap)

        def s4DatabaseChangeLog = new DatabaseChangeLog(s4RootChangelog)
        s4DatabaseChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([includeAll: [relativeToChangelogFile: true, path: s4ChangelogPathRelative, minDepth: minDepth, maxDepth: maxDepth, errorIfMissingOrEmpty: false]]), s4ResourceAccessor)

        // Scenario 5: includeAll in child changelog, relativeToChangelogFile false, path from root
        def s5RootPath = "com/example"
        def s5RootChangelog = s5RootPath + "/s5.xml"
        def s5ChildChangelogPathRelative = "s5Child"
        def s5ChildChangelog = s5RootPath + "/" + s5ChildChangelogPathRelative + "/changelog.xml"
        def s5ChangelogPathRelative = "s5"
        def s5ChangelogPath = s5ChangelogPathRelative
        def s5ResourceMap = new HashMap<String, String>()
        s5ResourceMap.put(s5RootChangelog,                              "")
        s5ResourceMap.put(s5ChildChangelog,                             childChangelogXml.replace(relativeToken, "false").replace(pathToken, s5ChangelogPath))
        s5ResourceMap.put(s5ChangelogPath + "/changelog.xml",           test2Xml)
        s5ResourceMap.put(s5ChangelogPath + "/a/changelog.xml",         test2Xml)
        s5ResourceMap.put(s5ChangelogPath + "/a/b/changelog-1.xml",     test2Xml)
        s5ResourceMap.put(s5ChangelogPath + "/a/b/changelog-2.xml",     test2Xml)
        s5ResourceMap.put(s5ChangelogPath + "/a/b/c/changelog.xml",     test2Xml)
        s5ResourceMap.put(s5ChangelogPath + "/a/b/c/d/changelog.xml",   test2Xml)
        s5ResourceMap.put(s5ChangelogPath + "/a/b/c/d/e/changelog.xml", test2Xml)
        def s5ResourceAccessor = new MockResourceAccessor(s5ResourceMap)

        def s5DatabaseChangeLog = new DatabaseChangeLog(s5RootChangelog)
        s5DatabaseChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([include: [relativeToChangelogFile: false, file: s5ChildChangelog]]), s5ResourceAccessor)

        // Scenario 6: includeAll in child changelog, relativeToChangelogFile false, path from child
        def s6RootPath = "com/example"
        def s6RootChangelog = s6RootPath + "/s6.xml"
        def s6ChildChangelogPathRelative = "s6Child"
        def s6ChildChangelog = s6RootPath + "/" + s6ChildChangelogPathRelative + "/changelog.xml"
        def s6ChangelogPathRelative = "s6"
        def s6ChangelogPath = s6RootPath + "/" + s6ChildChangelogPathRelative + "/" + s6ChangelogPathRelative
        def s6ResourceMap = new HashMap<String, String>()
        s6ResourceMap.put(s6RootChangelog,                              "")
        s6ResourceMap.put(s6ChildChangelog,                             childChangelogXml.replace(relativeToken, "false").replace(pathToken, s6ChangelogPath))
        s6ResourceMap.put(s6ChangelogPath + "/changelog.xml",           test2Xml)
        s6ResourceMap.put(s6ChangelogPath + "/a/changelog.xml",         test2Xml)
        s6ResourceMap.put(s6ChangelogPath + "/a/b/changelog-1.xml",     test2Xml)
        s6ResourceMap.put(s6ChangelogPath + "/a/b/changelog-2.xml",     test2Xml)
        s6ResourceMap.put(s6ChangelogPath + "/a/b/c/changelog.xml",     test2Xml)
        s6ResourceMap.put(s6ChangelogPath + "/a/b/c/d/changelog.xml",   test2Xml)
        s6ResourceMap.put(s6ChangelogPath + "/a/b/c/d/e/changelog.xml", test2Xml)
        def s6ResourceAccessor = new MockResourceAccessor(s6ResourceMap)

        def s6DatabaseChangeLog = new DatabaseChangeLog(s6RootChangelog)
        s6DatabaseChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([include: [relativeToChangelogFile: false, file: s6ChildChangelog]]), s6ResourceAccessor)

        // Scenario 7: includeAll in child changelog, relativeToChangelogFile true, path single child
        def s7RootPath = "com/example"
        def s7RootChangelog = s7RootPath + "/s7.xml"
        def s7ChildChangelogPathRelative = "s7Child"
        def s7ChildChangelog = s7RootPath + "/" + s7ChildChangelogPathRelative + "/changelog.xml"
        def s7ChangelogPathRelative = "s7"
        def s7ChangelogPath = s7RootPath + "/" + s7ChildChangelogPathRelative + "/" + s7ChangelogPathRelative
        def s7ResourceMap = new HashMap<String, String>()
        s7ResourceMap.put(s7RootChangelog,                              "")
        s7ResourceMap.put(s7ChildChangelog,                             childChangelogXml.replace(relativeToken, "true").replace(pathToken, s7ChangelogPathRelative))
        s7ResourceMap.put(s7ChangelogPath + "/changelog.xml",           test2Xml)
        s7ResourceMap.put(s7ChangelogPath + "/a/changelog.xml",         test2Xml)
        s7ResourceMap.put(s7ChangelogPath + "/a/b/changelog-1.xml",     test2Xml)
        s7ResourceMap.put(s7ChangelogPath + "/a/b/changelog-2.xml",     test2Xml)
        s7ResourceMap.put(s7ChangelogPath + "/a/b/c/changelog.xml",     test2Xml)
        s7ResourceMap.put(s7ChangelogPath + "/a/b/c/d/changelog.xml",   test2Xml)
        s7ResourceMap.put(s7ChangelogPath + "/a/b/c/d/e/changelog.xml", test2Xml)
        def s7ResourceAccessor = new MockResourceAccessor(s7ResourceMap)

        def s7DatabaseChangeLog = new DatabaseChangeLog(s7RootChangelog)
        s7DatabaseChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([include: [relativeToChangelogFile: false, file: s7ChildChangelog]]), s7ResourceAccessor)

        // Scenario 8: includeAll in child changelog, relativeToChangelogFile true, path multi child
        def s8RootPath = "com/example"
        def s8RootChangelog = s8RootPath + "/s8.xml"
        def s8ChildChangelogPathRelative = "s8Child"
        def s8ChildChangelog = s8RootPath + "/" + s8ChildChangelogPathRelative + "/changelog.xml"
        def s8ChangelogPathRelative = "s8_1/s8_2/s8_3"
        def s8ChangelogPath = s8RootPath + "/" + s8ChildChangelogPathRelative + "/" + s8ChangelogPathRelative
        def s8ResourceMap = new HashMap<String, String>()
        s8ResourceMap.put(s8RootChangelog,                              "")
        s8ResourceMap.put(s8ChildChangelog,                             childChangelogXml.replace(relativeToken, "true").replace(pathToken, s8ChangelogPathRelative))
        s8ResourceMap.put(s8ChangelogPath + "/changelog.xml",           test2Xml)
        s8ResourceMap.put(s8ChangelogPath + "/a/changelog.xml",         test2Xml)
        s8ResourceMap.put(s8ChangelogPath + "/a/b/changelog-1.xml",     test2Xml)
        s8ResourceMap.put(s8ChangelogPath + "/a/b/changelog-2.xml",     test2Xml)
        s8ResourceMap.put(s8ChangelogPath + "/a/b/c/changelog.xml",     test2Xml)
        s8ResourceMap.put(s8ChangelogPath + "/a/b/c/d/changelog.xml",   test2Xml)
        s8ResourceMap.put(s8ChangelogPath + "/a/b/c/d/e/changelog.xml", test2Xml)
        def s8ResourceAccessor = new MockResourceAccessor(s8ResourceMap)

        def s8DatabaseChangeLog = new DatabaseChangeLog(s8RootChangelog)
        s8DatabaseChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([include: [relativeToChangelogFile: false, file: s8ChildChangelog]]), s8ResourceAccessor)

        then:
        s1DatabaseChangeLog.getChangeSets().size() == expectedIncludeAllChangesetsToDeploy
        s2DatabaseChangeLog.getChangeSets().size() == expectedIncludeAllChangesetsToDeploy
        s3DatabaseChangeLog.getChangeSets().size() == expectedIncludeAllChangesetsToDeploy
        s4DatabaseChangeLog.getChangeSets().size() == expectedIncludeAllChangesetsToDeploy
        s5DatabaseChangeLog.getChangeSets().size() == expectedIncludeAllChangesetsToDeploy
        s6DatabaseChangeLog.getChangeSets().size() == expectedIncludeAllChangesetsToDeploy
        s7DatabaseChangeLog.getChangeSets().size() == expectedIncludeAllChangesetsToDeploy
        s8DatabaseChangeLog.getChangeSets().size() == expectedIncludeAllChangesetsToDeploy

        where:
        minDepth    | maxDepth          | expectedIncludeAllChangesetsToDeploy
        0           | Integer.MAX_VALUE | 7
        0           | 0                 | 0
        0           | 1                 | 1
        1           | 1                 | 1
        1           | 2                 | 2
        2           | 2                 | 1
        2           | 3                 | 3
        3           | 3                 | 2
        3           | 4                 | 3
        4           | 4                 | 1
        4           | 5                 | 2
        5           | 5                 | 1
        5           | 6                 | 2
        6           | 6                 | 1
        6           | 7                 | 1
        7           | 7                 | 0
        7           | 8                 | 0
        7           | Integer.MAX_VALUE | 0
        1           | 3                 | 4
        0           | 2                 | 2
        3           | 8                 | 5
    }

    @Unroll
    def "includeAll finds all expected changelogs with EndsWithFilter: #endsWithFilter"() {
        when:
        def rootChangeLogPath = "com/example/root.xml"
        def includedAllChangeLogPath = "changelogs"
        def resourceAccessor = new MockResourceAccessor(["com/example/root.xml": "",
                                                         "changelogs/changelog-1.xml": test3Xml,
                                                         "changelogs/morechangelogs/changelog-2.xml": test3Xml,
                                                         "changelogs/morechangelogs/withMore/changelog-3.xml": test3Xml,
                                                         "changelogs/morechangelogs/withMore/changelog-4.xml": test3Xml,
                                                         "changelogs/morechangelogs/AndMore/changelog-4.xml": test3Xml])

        def rootChangeLog = new DatabaseChangeLog(rootChangeLogPath)
        rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                .addChildren([includeAll: [path: includedAllChangeLogPath, endsWithFilter:endsWithFilter, errorIfMissingOrEmpty:false]]), resourceAccessor)

        then:
        rootChangeLog.getChangeSets().size() == expectedIncludeAllChangesetsToDeploy

        where:
        endsWithFilter  | expectedIncludeAllChangesetsToDeploy
        null            | 5
        ""              | 5
        "1.XML"         | 1
        "2.XML"         | 1
        "3.XML"         | 1
        "4.XML"         | 2
        "5.XML"         | 0
        "1.xml"         | 1
        "2.xml"         | 1
        "3.xml"         | 1
        "4.xml"         | 2
        "5.xml"         | 0
    }

}
