package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.*;

public class AddLookupTableChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddLookupTableChange();
        change.setExistingTableName("OLD_TABLE_NAME");
        change.setExistingColumnName("OLD_COLUMN_NAME");

        then:
        change.getConfirmationMessage() == "Lookup table added for OLD_TABLE_NAME.OLD_COLUMN_NAME"
    }
}
