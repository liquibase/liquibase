package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropTableStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DropTableChangeTest extends StandardChangeTest {
    def getConfirmationMessage() throws Exception {
        when:
        def change = new DropTableChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setCascadeConstraints(true);

        then:
        "Table TAB_NAME dropped" == change.getConfirmationMessage()
    }
}