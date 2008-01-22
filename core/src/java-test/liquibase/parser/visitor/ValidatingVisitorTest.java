package liquibase.parser.visitor;

import liquibase.ChangeSet;
import liquibase.RanChangeSet;
import liquibase.change.CreateTableChange;
import liquibase.exception.SetupException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class ValidatingVisitorTest {

    private ChangeSet changeSet1;
    private ChangeSet changeSet2;

    @Before
    public void setup() {
        changeSet1 = new ChangeSet("1", "testAuthor", false, false, "path/changelog", null, null, null);
        changeSet2 = new ChangeSet("2", "testAuthor", false, false, "path/changelog", null, null, null);
    }


    @Test
    public void visit_successful() {
        changeSet1.addChange(new CreateTableChange());
        changeSet2.addChange(new CreateTableChange());

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>());
        handler.visit(changeSet1);
        handler.visit(changeSet2);

        assertTrue(handler.validationPassed());

    }

    @Test
    public void visit_setupException() {
        changeSet1.addChange(new CreateTableChange() {
            public void setUp() throws SetupException {
                throw new SetupException("Test message");
            }
        });

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>());
        handler.visit(changeSet1);

        assertEquals(1, handler.getSetupExceptions().size());
        assertEquals("Test message", handler.getSetupExceptions().get(0).getMessage());

        assertFalse(handler.validationPassed());
    }

    @Test
    public void visit_duplicate() {

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>());
        handler.visit(changeSet1);
        handler.visit(changeSet1);

        assertEquals(1, handler.getDuplicateChangeSets().size());

        assertFalse(handler.validationPassed());
    }
}
