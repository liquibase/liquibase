package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RawSQLChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new RawSQLChange()

        then:
        "Custom SQL executed" == change.getConfirmationMessage()
    }

    @Override
    protected String getExpectedChangeName() {
        return "sql"
    }

    @Override
    protected boolean canUseStandardGenerateCheckSumTest() {
        return false;
    }

}