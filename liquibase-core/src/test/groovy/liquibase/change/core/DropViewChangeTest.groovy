package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropViewStatement
import liquibase.structure.core.Table
import liquibase.structure.core.View;

import static org.junit.Assert.*;
import org.junit.Test;

public class DropViewChangeTest  extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropViewChange change = new DropViewChange();
        change.setViewName("VIEW_NAME");

        then:
        "View VIEW_NAME dropped" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def view = new View(null, null, "test_view")

        def change = new DropViewChange()
        change.viewName = view.name

        then: "view is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "view exists"
        snapshotFactory.addObjects(view)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied
    }
}
