package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import static org.junit.Assert.*;

import org.junit.Test;

public class CreateIndexChangeTest extends StandardChangeTest {
    def getConfirmationMessage() throws Exception {
        when:
        CreateIndexChange refactoring = new CreateIndexChange();
        refactoring.setIndexName("IDX_TEST");

        then:
        "Index IDX_TEST created" == refactoring.getConfirmationMessage()
    }
}