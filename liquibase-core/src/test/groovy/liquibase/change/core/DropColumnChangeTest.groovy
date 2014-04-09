package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropColumnStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropColumnChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        then:
        "Column TABLE_NAME.COL_HERE dropped" == change.getConfirmationMessage()
    }

}