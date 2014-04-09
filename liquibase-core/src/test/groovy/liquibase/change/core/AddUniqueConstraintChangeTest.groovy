package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddUniqueConstraintChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddUniqueConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL_HERE");

        then:
        change.getConfirmationMessage() == "Unique constraint added to TABLE_NAME(COL_HERE)"
    }

}
