package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropUniqueConstraintStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DropUniqueConstraintChangeTest  extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new DropUniqueConstraintChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setConstraintName("UQ_CONSTRAINT");

        then:
        "Unique constraint UQ_CONSTRAINT dropped from TAB_NAME" == change.getConfirmationMessage()
    }
}
