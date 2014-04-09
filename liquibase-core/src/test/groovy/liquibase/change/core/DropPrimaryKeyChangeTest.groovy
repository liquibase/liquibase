package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropPrimaryKeyStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropPrimaryKeyChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setConstraintName("PK_NAME");

        then:
        "Primary key dropped from TABLE_NAME" == change.getConfirmationMessage()

    }
}
