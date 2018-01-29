package liquibase.changelog

import liquibase.ContextExpression;
import liquibase.change.core.CreateTableChange
import liquibase.change.core.RawSQLChange
import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNode
import liquibase.precondition.core.OrPrecondition
import liquibase.precondition.core.PreconditionContainer
import liquibase.precondition.core.RunningAsPrecondition
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

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

    def testSql = '''-- an included raw sql file
create table sql_table (id int);
create view sql_view as select * from sql_table;'''


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
        changeLogFromChildren.preconditions.nestedPreconditions.size() == 2
        changeLogFromValue.preconditions.nestedPreconditions.size() == 2

        ((RunningAsPrecondition) changeLogFromChildren.preconditions.nestedPreconditions[0]).username == "user1"
        ((RunningAsPrecondition) changeLogFromValue.preconditions.nestedPreconditions[0]).username == "user1"

        ((RunningAsPrecondition) changeLogFromChildren.preconditions.nestedPreconditions[1]).username == "user2"
        ((RunningAsPrecondition) changeLogFromValue.preconditions.nestedPreconditions[1]).username == "user2"

        changeLogFromChildren.changeSets.size() == 2
        changeLogFromValue.changeSets.size() == 2

        ((CreateTableChange) changeLogFromChildren.changeSets[0].changes[0]).tableName == "my_table"
        ((CreateTableChange) changeLogFromValue.changeSets[0].changes[0]).tableName == "my_table"

        ((CreateTableChange) changeLogFromChildren.changeSets[1].changes[0]).tableName == "my_other_table"
        ((CreateTableChange) changeLogFromValue.changeSets[1].changes[0]).tableName == "my_other_table"
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
        ((RunningAsPrecondition) rootChangeLog.preconditions.nestedPreconditions[0]).username == "user1"

        ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[1]).nestedPreconditions.size() == 2
        ((RunningAsPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[1]).nestedPreconditions[0]).username == "testUser"
        ((OrPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[1]).nestedPreconditions[1]).nestedPreconditions.size() == 2

        ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions.size() == 2
        ((RunningAsPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions[0]).username == "otherUser"
        ((OrPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions[1]).nestedPreconditions.size() == 2

        rootChangeLog.changeSets.size() == 3
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/root.xml", "nvoxland", "1").changes[0]).tableName == "test_table"
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/test1.xml", "nvoxland", "1").changes[0]).tableName == "person"
        ((CreateTableChange) rootChangeLog.getChangeSet("com/example/test2.xml", "nvoxland", "1").changes[0]).tableName == "person2"
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
                .addChildren([includeAll: [path: "com/example", resourceComparator : "liquibase.changelog.ReversedChangeLogNamesComparator"]])
                , resourceAccessor)

        then:
        rootChangeLog.preconditions.nestedPreconditions.size() == 4
        ((RunningAsPrecondition) rootChangeLog.preconditions.nestedPreconditions[0]).username == "user1"

        ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[1]).nestedPreconditions.size() == 2

        ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions.size() == 2
        ((RunningAsPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions[0]).username == "testUser"
        ((OrPrecondition) ((PreconditionContainer) rootChangeLog.preconditions.nestedPreconditions[2]).nestedPreconditions[1]).nestedPreconditions.size() == 2

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

    def "includeAll executes include in alphabetical order"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/children/file2.sql": "file 2",
                "com/example/children/file3.sql": "file 3",
                "com/example/children/file1.sql": "file 1",
                "com/example/not/fileX.sql"     : "file X",
        ])
        def changeLogFile = new DatabaseChangeLog("com/example/root.xml")
        changeLogFile.includeAll("com/example/children", false, null, true, changeLogFile.getStandardChangeLogComparator(), resourceAccessor, new ContextExpression())

        then:
        changeLogFile.changeSets.collect { it.filePath } == ["com/example/children/file1.sql",
                                                             "com/example/children/file2.sql",
                                                             "com/example/children/file3.sql"]
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
                "com/example/not/fileX.sql": "file X",
        ])
        def changeLogFile = new DatabaseChangeLog("com/example/root.xml")
        changeLogFile.includeAll("com/example/missing", false, null, true, changeLogFile.getStandardChangeLogComparator(), resourceAccessor, new ContextExpression())

        then:
        SetupException e = thrown()
        assert e.getMessage().startsWith("Could not find directory or directory was empty for includeAll '");

    }

    def "includeAll throws no exception when directory not found and errorIfMissingOrEmpty is false"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/children/file2.sql": "file 2",
                "com/example/children/file3.sql": "file 3",
                "com/example/children/file1.sql": "file 1",
                "com/example/not/fileX.sql": "file X",
        ])
        def changeLogFile = new DatabaseChangeLog("com/example/root.xml")
        changeLogFile.includeAll("com/example/missing", false, null, false, changeLogFile.getStandardChangeLogComparator(), resourceAccessor, new ContextExpression())
        then:
        changeLogFile.changeSets.collect {it.filePath } == []

    }

}
