package liquibase.parser.visitor;

import liquibase.ChangeSet;
import liquibase.RanChangeSet;
import liquibase.change.CreateTableChange;
import liquibase.change.ColumnConfig;
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
        CreateTableChange change1 = new CreateTableChange();
        change1.setTableName("table1");
        ColumnConfig column1 = new ColumnConfig();
        change1.addColumn(column1);
        column1.setName("col1");
        column1.setType("int");

        CreateTableChange change2 = new CreateTableChange();
        change2.setTableName("table2");
        ColumnConfig column2 = new ColumnConfig();
        change2.addColumn(column2);
        column2.setName("col2");
        column2.setType("int");

        changeSet1.addChange(change1);
        changeSet2.addChange(change2);

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>());
        handler.visit(changeSet1, null);
        handler.visit(changeSet2, null);

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
        handler.visit(changeSet1, null);

        assertEquals(1, handler.getSetupExceptions().size());
        assertEquals("Test message", handler.getSetupExceptions().get(0).getMessage());

        assertFalse(handler.validationPassed());
    }

    @Test
    public void visit_duplicate() {

        ValidatingVisitor handler = new ValidatingVisitor(new ArrayList<RanChangeSet>());
        handler.visit(changeSet1, null);
        handler.visit(changeSet1, null);

        assertEquals(1, handler.getDuplicateChangeSets().size());

        assertFalse(handler.validationPassed());
    }
}
