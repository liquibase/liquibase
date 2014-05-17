package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.SQLiteDatabase;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddNotNullConstraintChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        then:
        change.getConfirmationMessage() == "Null constraint has been added to TABLE_NAME.COL_HERE"
    }
}