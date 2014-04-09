package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.AddDefaultValueStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddDefaultValueChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddDefaultValueChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");

        then:
        change.getConfirmationMessage() == "Default value added to TABLE_NAME.COLUMN_NAME"
    }
}
