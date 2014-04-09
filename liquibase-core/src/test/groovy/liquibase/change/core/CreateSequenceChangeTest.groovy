package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import static org.junit.Assert.*;
import org.junit.Test;

public class CreateSequenceChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new CreateSequenceChange();
        change.setSequenceName("SEQ_NAME");

        then:
        "Sequence SEQ_NAME created" == change.getConfirmationMessage()
    }
}
