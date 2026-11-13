package liquibase.diff.core

import liquibase.database.core.MockDatabase
import liquibase.diff.compare.CompareControl
import liquibase.snapshot.EmptyDatabaseSnapshot
import liquibase.snapshot.SnapshotControl
import liquibase.structure.core.Table
import spock.lang.Specification

class StandardDiffGeneratorTest extends Specification {

    def "a diff narrows its own result's compared types, not those of the shared compare control"() {
        given:
        def database = new MockDatabase()
        def tableOnly = new EmptyDatabaseSnapshot(database, new SnapshotControl(database, Table))
        def allTypes = new EmptyDatabaseSnapshot(database, new SnapshotControl(database))
        def standardTypesBefore = new HashSet(CompareControl.STANDARD.getComparedTypes())

        when:
        def narrowed = new StandardDiffGenerator().compare(tableOnly, tableOnly, CompareControl.STANDARD)
        def full = new StandardDiffGenerator().compare(allTypes, allTypes, CompareControl.STANDARD)

        then:
        narrowed.getComparedTypes().contains(Table)
        narrowed.getComparedTypes() == standardTypesBefore.intersect(tableOnly.getSnapshotControl().getTypesToInclude())
        CompareControl.STANDARD.getComparedTypes() == standardTypesBefore
        full.getComparedTypes() == standardTypesBefore.intersect(allTypes.getSnapshotControl().getTypesToInclude())
        full.getComparedTypes().size() > narrowed.getComparedTypes().size()

        cleanup:
        CompareControl.STANDARD.getComparedTypes().addAll(standardTypesBefore)
    }
}
