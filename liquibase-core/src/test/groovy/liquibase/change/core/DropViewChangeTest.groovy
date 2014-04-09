package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropViewStatement;
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
}
