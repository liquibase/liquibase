package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropDefaultValueStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropDefaultValueChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropDefaultValueChange change = new DropDefaultValueChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        then:
        "Default value dropped from TABLE_NAME.COL_HERE" == change.getConfirmationMessage()
    }

}
