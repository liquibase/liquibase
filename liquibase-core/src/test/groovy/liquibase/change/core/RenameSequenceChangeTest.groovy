package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.sdk.database.MockDatabase
import liquibase.statement.SqlStatement
import liquibase.statement.core.RenameSequenceStatement;

/**
 * Tests for {@link RenameSequenceChange}
 */
public class RenameSequenceChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new RenameSequenceChange()
        change.setOldSequenceName("OLD_NAME");
        change.setNewSequenceName("NEW_NAME");

        then:
        "Sequence OLD_NAME renamed to NEW_NAME" == change.getConfirmationMessage()
    }

   def generateStatement() throws Exception {
        when:
        def RenameSequenceChange change = new RenameSequenceChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setOldSequenceName("OLD_NAME");
        change.setNewSequenceName("NEW_NAME");
        def SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());

        then:
        assert 1 == sqlStatements.length
        assert sqlStatements[0] instanceof RenameSequenceStatement
        assert "SCHEMA_NAME" == ((RenameSequenceStatement) sqlStatements[0]).getSchemaName()
        assert "OLD_NAME" == ((RenameSequenceStatement) sqlStatements[0]).getOldSequenceName()
        assert "NEW_NAME" == ((RenameSequenceStatement) sqlStatements[0]).getNewSequenceName()
    }
}
