package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropIndexStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropIndexChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");

        then:
        "Index IDX_NAME dropped from table TABLE_NAME" == refactoring.getConfirmationMessage()
    }
}