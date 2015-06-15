package liquibase.snapshot.transformer

import liquibase.JUnitScope
import liquibase.database.core.UnsupportedDatabaseSupplier
import liquibase.snapshot.TestSnapshotFactory
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class LimitTransformerTest extends Specification {

    @Unroll("#featureName: #limit")
    def "limits all types if none set"() {
        def scope = JUnitScope.getInstance(new UnsupportedDatabaseSupplier())
        when:
        def snapshot = scope.getSingleton(TestSnapshotFactory).createSnapshot(new LimitTransformer(limit), scope)

        then:
        snapshot.get(Table).groupBy({ it.name.container }).values()*.size().unique() == (limit == 0 ? [] : [limit])
        snapshot.get(Column).groupBy({ it.name.container }).values()*.size().unique() == (limit == 0 ? [] : [limit])

        where:
        limit << [1, 2, 5, 0]
    }

    @Unroll("#featureName: #limit")
    def "limits correct with all types"() {
        def scope = JUnitScope.getInstance(new UnsupportedDatabaseSupplier())
        when:
        def snapshot = scope.getSingleton(TestSnapshotFactory).createSnapshot(new LimitTransformer(limit, DatabaseObject), scope)

        then:
        snapshot.get(Table).groupBy({ it.name.container }).values()*.size().unique() == (limit == 0 ? [] : [limit])
        snapshot.get(Column).groupBy({ it.name.container }).values()*.size().unique() == (limit == 0 ? [] : [limit])

        where:
        limit << [1, 2, 5, 0]
    }

    @Unroll("#featureName: #limit")
    def "limits correct with set type"() {
        def scope = JUnitScope.getInstance(new UnsupportedDatabaseSupplier())
        when:
        def snapshot = scope.getSingleton(TestSnapshotFactory).createSnapshot(new LimitTransformer<Column>(limit, Column), scope)

        then:
        snapshot.get(Table).size() > limit * 2 //one for each schema
        snapshot.get(Column).groupBy({ it.name.container }).values()*.size().unique() == (limit == 0 ? [] : [limit])

        where:
        limit << [1, 2, 5, 0]
    }

    @Unroll("#featureName: #limit")
    def "limits correct with multiple types"() {
        def scope = JUnitScope.getInstance(new UnsupportedDatabaseSupplier())
        when:
        def snapshot = scope.getSingleton(TestSnapshotFactory).createSnapshot(new LimitTransformer(limit, Column, Table), scope)

        then:
        snapshot.get(Table).groupBy({ it.name.container }).values()*.size().unique() == (limit == 0 ? [] : [limit])
        snapshot.get(Column).groupBy({ it.name.container }).values()*.size().unique() == (limit == 0 ? [] : [limit])

        where:
        limit << [1, 2, 5, 0]
    }
}
