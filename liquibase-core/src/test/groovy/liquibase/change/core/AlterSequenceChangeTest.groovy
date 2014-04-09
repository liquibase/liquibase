package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterSequenceStatement;
import static org.junit.Assert.*;
import org.junit.Test;

import java.math.BigInteger;

public class AlterSequenceChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def refactoring = new AlterSequenceChange();
        refactoring.setSequenceName("SEQ_NAME");

        then:
        refactoring.getConfirmationMessage() == "Sequence SEQ_NAME altered"
    }
}
