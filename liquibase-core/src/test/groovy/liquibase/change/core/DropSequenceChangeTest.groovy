package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DropSequenceChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new DropSequenceChange();
        change.setSequenceName("SEQ_NAME");

        then:
        "Sequence SEQ_NAME dropped" == change.getConfirmationMessage()
    }
}