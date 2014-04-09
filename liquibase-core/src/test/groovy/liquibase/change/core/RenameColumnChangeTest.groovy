package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameColumnStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class RenameColumnChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new RenameColumnChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setOldColumnName("oldColName");
        change.setNewColumnName("newColName");

        then:
        "Column TABLE_NAME.oldColName renamed to newColName" == change.getConfirmationMessage()
    }
}
