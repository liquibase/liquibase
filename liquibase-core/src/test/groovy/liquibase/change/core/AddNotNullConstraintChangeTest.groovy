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
        change.setConstraintName("COL_NN");

        then:
        change.getConfirmationMessage() == "NOT NULL constraint \"COL_NN\" has been added to TABLE_NAME.COL_HERE"
    }
}