package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddPrimaryKeyChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddPrimaryKeyChange();
        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL_HERE");

        then:
        change.getConfirmationMessage() == "Primary key added to TABLE_NAME (COL_HERE)"
    }
}
