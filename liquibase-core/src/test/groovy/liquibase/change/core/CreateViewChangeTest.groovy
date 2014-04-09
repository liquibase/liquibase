package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import static org.junit.Assert.*;

import org.junit.Test;

public class CreateViewChangeTest extends StandardChangeTest {


    def getConfirmationMessage() throws Exception {
        when:
        CreateViewChange change = new CreateViewChange();
        change.setViewName("VIEW_NAME");

        then:
        "View VIEW_NAME created" == change.getConfirmationMessage()
    }
}
