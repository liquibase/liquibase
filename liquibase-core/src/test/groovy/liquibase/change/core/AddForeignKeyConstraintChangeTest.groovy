package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddForeignKeyConstraintChangeTest  extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddForeignKeyConstraintChange();
        change.setConstraintName("FK_NAME");
        change.setBaseTableSchemaName("SCHEMA_NAME");
        change.setBaseTableName("TABLE_NAME");
        change.setBaseColumnNames("COL_NAME");

        then: change.getConfirmationMessage() == "Foreign key contraint added to TABLE_NAME (COL_NAME)"
    }
}
