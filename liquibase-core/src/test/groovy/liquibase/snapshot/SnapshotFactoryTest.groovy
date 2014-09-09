package liquibase.snapshot

import liquibase.ExecutionEnvironment
import liquibase.action.MockMetaDataAction
import liquibase.action.QueryAction
import liquibase.database.core.OracleDatabase
import liquibase.sdk.database.MockDatabase
import liquibase.servicelocator.LiquibaseService
import liquibase.snapshot.core.SchemaLookup
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Catalog
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import static spock.util.matcher.HamcrestSupport.*
import static org.hamcrest.Matchers.*
import spock.lang.Specification

import static spock.util.matcher.HamcrestSupport.expect
import static spock.util.matcher.HamcrestSupport.that

class SnapshotFactoryTest extends Specification {

    def cleanup() {
        SnapshotFactory.instance.reset();
    }

    def "singleton works"() {
        SnapshotFactory.instance.is(SnapshotFactory.instance)
    }


    def "registry works as expected"() {
        when:
        def originalLogicSize = SnapshotFactory.instance.lookupLogic.size()
        def originalRelateSize = SnapshotFactory.instance.relateLogic.size()
        def originalDetailsSize = SnapshotFactory.instance.detailsLogic.size()

        then: "Generators are initially found"
        originalLogicSize > 0
        originalRelateSize > 0
        originalDetailsSize == 0  //no built in details in liquibase, only extensions

        when: "we unregister a lookup logic"
        def lookupToRemove = SnapshotFactory.instance.lookupLogic[1]
        SnapshotFactory.instance.unregister(lookupToRemove as SnapshotLookupLogic)
        then: "it is gone"
        SnapshotFactory.instance.lookupLogic.size() == originalLogicSize - 1

        when: "we unregister a relate logic"
        def relateToRemove = SnapshotFactory.instance.relateLogic[1]
        SnapshotFactory.instance.unregister(relateToRemove as SnapshotRelateLogic)
        then: "it is gone"
        SnapshotFactory.instance.relateLogic.size() == originalRelateSize - 1

        when: "we add back the lookup logic"
        SnapshotFactory.instance.register(lookupToRemove as SnapshotLookupLogic)
        then:
        SnapshotFactory.instance.lookupLogic.size() == originalLogicSize

        when: "we add back the relate logic"
        SnapshotFactory.instance.register(relateToRemove as SnapshotRelateLogic)
        then:
        SnapshotFactory.instance.relateLogic.size() == originalRelateSize
    }

    def "getLookupLogic"() {
        expect:
        SnapshotFactory.instance.getLookupLogic(Schema.class, new ExecutionEnvironment(new MockDatabase())).class == SchemaLookup.class

        when:
        SnapshotFactory.instance.register(new MySchemaLookup())
        then:
        SnapshotFactory.instance.getLookupLogic(Schema.class, new ExecutionEnvironment(new MockDatabase())).class == MySchemaLookup.class
        SnapshotFactory.instance.getLookupLogic(Schema.class, new ExecutionEnvironment(new OracleDatabase())).class == SchemaLookup.class
    }

    def "createSnapshot(examples[])"() {
        when: "We create logic that can return objects from cata.schema1 ... CatB.Schema2 but only snapshot for CatA.Schema1 and CatB.schema2"
        def database = new MockDatabase()
        def factory = SnapshotFactory.instance
        factory.lookupLogic.clear()
        factory.relateLogic.clear()
        factory.detailsLogic.clear()

        factory.register(new MockSnapshotLookupLogic([
                (new Schema("CatA", "Schema1")): [new Schema("CatA", "Schema1"), new Catalog("CatA")],
                (new Schema("CatA", "Schema2")): [new Schema("CatA", "Schema2"), new Catalog("CatA")],
                (new Schema("CatB", "Schema1")): [new Schema("CatB", "Schema1"), new Catalog("CatB")],
                (new Schema("CatB", "Schema2")): [new Schema("CatB", "Schema2"), new Catalog("CatB")],
        ]))

        factory.register(new MockSnapshotLookupLogic([
                (new Schema("CatA", "Schema1")): [new Table("CatA", "Schema1", "TableA1X"), new Table("CatA", "Schema1", "TableA1Y")],
                (new Schema("CatA", "Schema2")): [new Table("CatA", "Schema2", "TableA2")],
                (new Schema("CatB", "Schema1")): [new Table("CatB", "Schema1", "TableB1")],
                (new Schema("CatB", "Schema2")): [new Table("CatB", "Schema2", "TableB2")],
        ]))

        factory.register(new SnapshotRelateLogic() {
            @Override
            boolean supports(ExecutionEnvironment environment) {
                return true;
            }

            @Override
            void relate(NewDatabaseSnapshot snapshot) {
                for (Table table : snapshot.get(Table)) {
                    snapshot.get(table.getSchema()).addDatabaseObject(table);
                }
            }
        })

        factory.register(new SnapshotDetailsLogic() {
            @Override
            boolean supports(ExecutionEnvironment environment) {
                return true;
            }

            @Override
            void addDetails(NewDatabaseSnapshot snapshot) {
                for (Table table : snapshot.get(Table)) {
                    table.setAttribute("mock-details", "Table name "+table.getName());
                }
            }
        })

        def snapshot = factory.createSnapshot([new Schema("CatA", "Schema1"), new Schema("CatB", "Schema2")] as DatabaseObject[], new SnapshotControl(database), new ExecutionEnvironment(database));

        then: "the correct objects were found"
        expect snapshot.get(Table).collect({it.toString()}), containsInAnyOrder(["TableA1X", "TableA1Y", "TableB2"] as String[]);
        expect snapshot.get(Schema).collect({it.toString()}), containsInAnyOrder(["CatA.Schema1", "CatB.Schema2"] as String[]);
        expect snapshot.get(Catalog).collect({it.toString()}), containsInAnyOrder(["CatA", "CatB"] as String[]);

        and: "objects were related"
        expect snapshot.get(new Schema("CatA", "Schema1")).getDatabaseObjects(Table).collect({it.toString()}), containsInAnyOrder(["TableA1X", "TableA1Y"] as String[]);
        expect snapshot.get(new Schema("CatB", "Schema2")).getDatabaseObjects(Table).collect({it.toString()}), containsInAnyOrder(["TableB2"] as String[]);

        and: "there are additional details"
        snapshot.get(Table).each {assert it.getAttribute("mock-details", String) == "Table name "+it.getName()}

    }

    @LiquibaseService(skip = true)
    private static class MySchemaLookup extends SchemaLookup {
        @Override
        int getPriority(Class<? extends DatabaseObject> objectType, ExecutionEnvironment environment) {
            if (environment.getTargetDatabase() instanceof MockDatabase) {
                return 150;
            }
            return PRIORITY_NONE;
        }
    }
}
