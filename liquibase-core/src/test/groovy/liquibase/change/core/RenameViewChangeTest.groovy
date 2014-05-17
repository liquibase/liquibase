package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.View

public class RenameViewChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new RenameViewChange()
        change.setOldViewName("OLD_NAME");
        change.setNewViewName("NEW_NAME");

        then:
        "View OLD_NAME renamed to NEW_NAME" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def oldView = new View(null, null, "test_view")
        def newView = new View(null, null, "new_view")

        def change = new RenameViewChange()
        change.oldViewName= oldView.name
        change.newViewName= newView.name

        then: "neither view is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "old view is there"
        snapshotFactory.addObjects(oldView)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "old and new views are there"
        snapshotFactory.addObjects(newView)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "just new view is there"
        snapshotFactory.removeObjects(oldView)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete
    }
}