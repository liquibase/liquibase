package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.Action
import liquibase.actionlogic.ObjectBasedQueryResult
import liquibase.database.ConnectionSupplier
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.Snapshot
import liquibase.structure.ObjectReference
import liquibase.structure.ObjectNameStrategy
import liquibase.structure.TestColumnSupplier
import liquibase.structure.TestForeignKeySupplier
import liquibase.structure.TestTableSupplier
import liquibase.structure.core.*
import liquibase.util.CollectionUtil
import spock.lang.Unroll

class SnapshotDatabaseObjectsActionForeignKeysTest extends AbstractActionTest {

    @Unroll("#featureName: #fkRef on #conn")
    def "can find fully qualified complex foreign key names"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(fkRef)

        runStandardTest([
                fkName_asTable: fkRef
        ], action, conn, scope, { plan, results ->
            assert results instanceof ObjectBasedQueryResult
            assert results.size() == 1;

            def foundFk = results.asObject(ForeignKey)
            assert foundFk.name == fkRef
        })

        where:
        [conn, scope, fkRef] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    getObjectNames(TestForeignKeySupplier, ObjectNameStrategy.COMPLEX_NAMES, scope),
            ])
        }
    }

    @Unroll("#featureName: #tableName on #conn")
    def "can find all foreignKeys in a fully qualified complex table name"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(ForeignKey, new Table(tableName))

        runStandardTest([
                tableName_asTable: tableName
        ], action, conn, scope, { plan, results ->
            assert results instanceof ObjectBasedQueryResult
            assert results.size() > 1; //found more than one object

            for (ForeignKey fk : results.asList(ForeignKey)) {
                assert fk.columnChecks.get(0).baseColumn.container == tableName
                assert !fk.name.name.endsWith("_other")
            }

        })


        where:
        [conn, scope, tableName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    getObjectNames(TestTableSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope),
            ])
        }
    }

    @Unroll("#featureName: #schemaName on #conn")
    def "can find all foreignKeys in a schema"() {
        expect:
        def action = new SnapshotDatabaseObjectsAction(ForeignKey, new Schema(schemaName))

        runStandardTest([
                schemaName_asTable: schemaName
        ], action, conn, scope, { plan, results ->
            assert results instanceof ObjectBasedQueryResult
            assert results.size() > 1; //found more than one object

            for (ForeignKey fk : results.asList(ForeignKey)) {
                assert fk.container == schemaName
            }

        })


        where:
        [conn, scope, schemaName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)

            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    it.allContainers
            ])
        }
    }

    @Override
    protected Snapshot createSnapshot(Action action, ConnectionSupplier connectionSupplier, Scope scope) {
        Snapshot snapshot = new Snapshot(scope)
        def seenTables = new HashSet<ObjectReference>()
        if (((SnapshotDatabaseObjectsAction) action).relatedTo instanceof ForeignKey) {
            ForeignKey fk = ((SnapshotDatabaseObjectsAction) action).relatedTo
            for (def check : fk.columnChecks) {
                def refTableName = check.referencedColumn.container
                def baseTableName = check.baseColumn.container
                if (seenTables.add(refTableName)) {
                    snapshot.add(new Table(refTableName))
                }
                if (seenTables.add(baseTableName)) {
                    snapshot.add(new Table(baseTableName))
                }
                snapshot.add(new Column(check.referencedColumn, "int"))
                snapshot.add(new Column(check.baseColumn, "int"))
                snapshot.add(new PrimaryKey(new ObjectReference(check.referencedColumn.container), check.referencedColumn.name))
            }
            snapshot.add(fk)

            //add other FKs to ensure only the desired one(s) are fetched
            def table1Name = (seenTables as List)[0]
            for (def i = 0; i < 5; i++) {
                def baseCol = new Column(new ObjectReference(table1Name, "base_col$i"), "int")
                def refCol = new Column(new ObjectReference(table1Name.container, "ref_table$i", "ref_col$i"), "int")

                snapshot.add(new Table(refCol.name.container))
                snapshot.add(baseCol)
                snapshot.add(refCol)
                snapshot.add(new ForeignKey(new ObjectReference(table1Name.container, correctObjectName("fk_$i", ForeignKey, scope.getDatabase())), [baseCol.name], [refCol.name]))
                snapshot.add(new PrimaryKey(new ObjectReference(refCol.name.container, null), refCol.name.name))
            }
        } else if (((SnapshotDatabaseObjectsAction) action).relatedTo instanceof Table) {
            Table table = ((SnapshotDatabaseObjectsAction) action).relatedTo

            def tableNames = getObjectNames(TestTableSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)
            tableNames.remove(table.name)

            def columnNames = getObjectNames(TestColumnSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)
            def fkNames = getObjectNames(TestForeignKeySupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)

            int numItems = GroovyCollections.min([columnNames.size(), tableNames.size(), fkNames.size()])

            snapshot.add(table)

            for (def i = 0; i < numItems; i++) {
                snapshot.add(new Table(tableNames[i]))
                snapshot.add(new Column(new ObjectReference(table.name, columnNames[i].name), "int"))
                snapshot.add(new Column(new ObjectReference(tableNames[i], columnNames[i].name), "int"))
                snapshot.add(new PrimaryKey(new ObjectReference(tableNames[i], null), columnNames[i].name))
                snapshot.add(new ForeignKey(new ObjectReference(table.name.container, fkNames[i].name), [new ObjectReference(table.name, columnNames[i].name)], [new ObjectReference(tableNames[i], columnNames[i].name)]))

                //and another FK on the other table that shouldn't match
                snapshot.add(new ForeignKey(new ObjectReference(table.name.container, correctObjectName(fkNames[i].name + "_other", ForeignKey, scope.getDatabase())), [new ObjectReference(tableNames[i], columnNames[i].name)], [new ObjectReference(tableNames[i], columnNames[i].name)]))
            }
        } else if (((SnapshotDatabaseObjectsAction) action).relatedTo instanceof Schema) {
            def tableNames = getObjectNames(TestTableSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)
            def columnNames = getObjectNames(TestColumnSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)
            def fkNames = getObjectNames(TestForeignKeySupplier, ObjectNameStrategy.COMPLEX_NAMES, scope)

            int numItems = GroovyCollections.min([columnNames.size(), tableNames.size(), fkNames.size()])

            for (def i = 0; i < numItems; i++) {
                snapshot.add(new Table(tableNames[i]))
                snapshot.add(new Column(new ObjectReference(tableNames[i], columnNames[i].name), "int"))
                snapshot.add(new Index(new ObjectReference(tableNames[i], null), columnNames[i].name))
                snapshot.add(new ForeignKey(new ObjectReference(tableNames[i].container, fkNames[i].name), [new ObjectReference(tableNames[i], columnNames[i].name)], [new ObjectReference(tableNames[i], columnNames[i].name)]))
            }
        } else {
            throw new RuntimeException("Unexpected relatedTo type: "+((SnapshotDatabaseObjectsAction) action).relatedTo.class.name)
        }
        return snapshot;

    }
}
