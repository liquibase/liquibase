package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameViewStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class RenameViewChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new RenameViewChange()
        change.setOldViewName("OLD_NAME");
        change.setNewViewName("NEW_NAME");

        then:
        "View OLD_NAME renamed to NEW_NAME" == change.getConfirmationMessage()
    }
}