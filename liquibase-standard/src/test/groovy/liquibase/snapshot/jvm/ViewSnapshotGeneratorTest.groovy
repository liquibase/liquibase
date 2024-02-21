package liquibase.snapshot.jvm

import liquibase.database.Database
import liquibase.snapshot.DatabaseSnapshot
import liquibase.snapshot.SnapshotControl
import liquibase.structure.core.Schema
import liquibase.structure.core.View
import spock.lang.Specification

class ViewSnapshotGeneratorTest extends Specification {

    def "does not add any views to snapshot if database does not support views"() {
        given:
        def database = Stub(Database.class)
        database.supportsViews() >> false
        def snapshotControl = Stub(SnapshotControl.class)
        snapshotControl.shouldInclude(View.class) >> true
        def snapshot = Stub(DatabaseSnapshot.class)
        snapshot.getDatabase() >> database
        snapshot.getSnapshotControl() >> snapshotControl
        def schema = Mock(Schema.class)

        when:
        new ViewSnapshotGenerator().addTo(schema, snapshot)

        then:
        0 * _
    }
}
