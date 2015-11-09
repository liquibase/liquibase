package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.Action
import liquibase.action.TestObjectFactory
import liquibase.database.ConnectionSupplier
import liquibase.database.ConnectionSupplierFactory
import liquibase.snapshot.Snapshot
import liquibase.structure.*
import liquibase.structure.core.*
import liquibase.util.CollectionUtil
import spock.lang.Unroll

class AddForeignKeysActionTest extends AbstractActionTest {

    @Unroll("#featureName: add #fkName on #tableName.#columnName.name to #conn")
    def "Can apply single column with standard settings but complex names"() {
        when:
        def action = new AddForeignKeysAction()

        def pkTable = new Table(new ObjectReference(tableName.container, tableName.name + "_2"))
        def fkTable = new Table(tableName)

        def foreignKey = new ForeignKey(fkName, [new ObjectReference(fkTable.name, columnName.name)], [new ObjectReference(pkTable.name, columnName.name + "_2")])

        action.foreignKeys = [foreignKey]

        then:
        runStandardTest([
                fkName_asTable    : fkName.toString(),
                tableName_asTable : tableName.toString(),
                columnName_asTable: columnName.toString()
        ], action, conn, scope)

        where:
        [conn, scope, columnName, tableName, fkName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    getObjectNames(TestColumnSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope),
                    getObjectNames(TestTableSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope),
                    getObjectNames(TestForeignKeySupplier, ObjectNameStrategy.COMPLEX_NAMES, scope),
            ])
        }

    }

    @Unroll("#featureName: add #fkName on #tableName.#columnName.name to #conn")
    def "Can apply multiple columns with standard settings but complex names"() {
        when:
        def action = new AddForeignKeysAction()

        def pkTable = new Table(new ObjectReference(tableName.container, tableName.name + "_2"))
        def fkTable = new Table(tableName)

        def foreignKey = new ForeignKey(fkName, [new ObjectReference(fkTable.name, columnName.name), new ObjectReference(fkTable.name, columnName.name + "_2")], [new ObjectReference(pkTable.name, columnName.name + "_3"), new ObjectReference(pkTable.name, columnName.name + "_4")])

        action.foreignKeys = [foreignKey]


        then:
        runStandardTest([
                fkName_asTable    : fkName.toString(),
                tableName_asTable : tableName.toString(),
                columnName_asTable: columnName.toString()
        ], action, conn, scope)

        where:
        [conn, scope, columnName, tableName, fkName] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it).child(JUnitScope.Attr.objectNameStrategy, ObjectNameStrategy.COMPLEX_NAMES)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    getObjectNames(TestColumnSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope),
                    getObjectNames(TestTableSupplier, ObjectNameStrategy.COMPLEX_NAMES, scope),
                    getObjectNames(TestForeignKeySupplier, ObjectNameStrategy.COMPLEX_NAMES, scope),
            ])
        }

    }

    @Unroll("#featureName: #foreignKeyDefinition on #conn")
    def "Valid parameter foreignKey permutations work"() {
        when:
        def baseTableName = getObjectNames(TestTableSupplier, ObjectNameStrategy.SIMPLE_NAMES, scope)[0]
        def refTableName = getObjectNames(TestTableSupplier, ObjectNameStrategy.SIMPLE_NAMES, scope)[1]

        def baseColumnName = new ObjectReference(baseTableName, getObjectNames(TestColumnSupplier, ObjectNameStrategy.SIMPLE_NAMES, scope)[0].name)
        def refColumnName = new ObjectReference(refTableName, getObjectNames(TestColumnSupplier, ObjectNameStrategy.SIMPLE_NAMES, scope)[0].name)

        ((ForeignKey) foreignKeyDefinition).columnChecks = [new ForeignKey.ForeignKeyColumnCheck(baseColumnName, refColumnName)]
        AddForeignKeysAction action = new AddForeignKeysAction(foreignKeyDefinition)

        then:
        runStandardTest([
                columnChecks_asTable     : foreignKeyDefinition.columnChecks,
                deferrable_asTable       : foreignKeyDefinition.deferrable,
                initiallyDeferred_asTable: foreignKeyDefinition.initiallyDeferred,
                updateRule_asTable       : foreignKeyDefinition.updateRule,
                deleteRule_asTable       : foreignKeyDefinition.deleteRule,
                backingIndex_asTable     : foreignKeyDefinition.backingIndex,
        ], action, conn, scope)

        where:
        [conn, scope, foreignKeyDefinition] << JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers.collectMany {
            def scope = JUnitScope.getInstance(it)
            return CollectionUtil.permutations([
                    [it],
                    [scope],
                    JUnitScope.instance.getSingleton(TestObjectFactory).createAllPermutations(ForeignKey, [
                            columnChecks: null,
                            deferrable: [null, true, false],
                            initiallyDeferred: [null, true, false],
                            updateRule: [null, ForeignKeyConstraintType.importedKeyCascade, ForeignKeyConstraintType.importedKeyNoAction, ForeignKeyConstraintType.importedKeyRestrict, ForeignKeyConstraintType.importedKeySetDefault, ForeignKeyConstraintType.importedKeySetNull],
                            deleteRule: [null, ForeignKeyConstraintType.importedKeyCascade, ForeignKeyConstraintType.importedKeyNoAction, ForeignKeyConstraintType.importedKeyRestrict, ForeignKeyConstraintType.importedKeySetDefault, ForeignKeyConstraintType.importedKeySetNull],
                    ]),
            ])
        }

    }

    @Override
    protected Snapshot createSnapshot(Action action, ConnectionSupplier connectionSupplier, Scope scope) {
        Snapshot snapshot = new Snapshot(scope)
        def seenTables = new HashSet()
        for (def fk : ((AddForeignKeysAction) action).foreignKeys) {
            for (def check : fk.columnChecks) {
                def baseTableName = check.baseColumn.container
                def refTableName = check.referencedColumn.container
                if (!seenTables.contains(baseTableName)) {
                    snapshot.add(new Table(baseTableName))
                }
                if (!seenTables.contains(refTableName)) {
                    snapshot.add(new Table(refTableName))
                }

                snapshot.add(new Column(check.baseColumn, new DataType(DataType.StandardType.INTEGER)))
                snapshot.add(new Column(check.referencedColumn, new DataType(DataType.StandardType.INTEGER)))

                def index = new Index(getObjectNames(TestIndexSupplier, ObjectNameStrategy.SIMPLE_NAMES, scope).get(0))
                index.columns.add(new Index.IndexedColumn(check.referencedColumn)) //index, not PK to support nulls
                snapshot.add(index)
            }
        }

        return snapshot
    }
}
