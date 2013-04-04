package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link CreateSequenceChange}
 */
public class CreateSequenceChangeTest extends StandardChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("createSequence", new CreateSequenceChange().getChangeMetaData().getName());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                if (!database.supportsSequences()) {
//                    return;
//                }
//
//                CreateSequenceChange change = new CreateSequenceChange();
//                change.setSchemaName("SCHEMA_NAME");
//                change.setSequenceName("SEQ_NAME");
//                change.setIncrementBy(new BigInteger("1"));
//                change.setMinValue(new BigInteger("2"));
//                change.setMaxValue(new BigInteger("3"));
//                change.setOrdered(true);
//                change.setStartValue(new BigInteger("4"));
//
//                SqlStatement[] sqlStatements = change.generateStatements(database);
//                assertEquals(1, sqlStatements.length);
//                assertTrue(sqlStatements[0] instanceof CreateSequenceStatement);
//
//                assertEquals("SCHEMA_NAME", ((CreateSequenceStatement) sqlStatements[0]).getSchemaName());
//                assertEquals("SEQ_NAME", ((CreateSequenceStatement) sqlStatements[0]).getSequenceName());
//                assertEquals(new BigInteger("1"), ((CreateSequenceStatement) sqlStatements[0]).getIncrementBy());
//                assertEquals(new BigInteger("2"), ((CreateSequenceStatement) sqlStatements[0]).getMinValue());
//                assertEquals(new BigInteger("3"), ((CreateSequenceStatement) sqlStatements[0]).getMaxValue());
//                assertEquals(new BigInteger("4"), ((CreateSequenceStatement) sqlStatements[0]).getStartValue());
//                assertEquals(true, ((CreateSequenceStatement) sqlStatements[0]).getOrdered());
//            }
//        });
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName("SEQ_NAME");

        assertEquals("Sequence SEQ_NAME created", change.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return !database.supportsSequences();
    }
}
