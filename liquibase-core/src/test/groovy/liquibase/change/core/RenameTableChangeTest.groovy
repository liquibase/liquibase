package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameTableStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class RenameTableChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new RenameTableChange()
        change.setOldTableName("OLD_NAME");
        change.setNewTableName("NEW_NAME");

        then:
        "Table OLD_NAME renamed to NEW_NAME" == change.getConfirmationMessage()
    }
}
