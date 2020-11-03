package liquibase.change.core

import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest
import liquibase.database.core.MockDatabase
import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNodeException
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.View

public class CreateViewChangeTest extends StandardChangeTest {


    def getConfirmationMessage() throws Exception {
        when:
        CreateViewChange change = new CreateViewChange();
        change.setViewName("VIEW_NAME");

        then:
        "View VIEW_NAME created" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def view = new View(null, null, "test_view")

        def change = new CreateViewChange()
        change.viewName = view.name

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "view created"
        snapshotFactory.addObjects(view)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.complete
    }

    def "load works with nested query"() {
        when:
        def change = new CreateViewChange()
        try {
            change.load(new liquibase.parser.core.ParsedNode(null, "createView").addChild(null, "viewName", "my_view").setValue("select * from test"), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        change.viewName == "my_view"
        change.selectQuery == "select * from test"
    }
}
