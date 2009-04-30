package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.DropUniqueConstraintStatement;
import liquibase.database.statement.SqlStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class DropUniqueConstraintChangeTest  extends AbstractChangeTest {
    private DropUniqueConstraintChange change;

    @Before
    public void setUp() throws Exception {
        change = new DropUniqueConstraintChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setConstraintName("UQ_CONSTRAINT");
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Unique Constraint", change.getChangeMetaData().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropUniqueConstraintStatement);
        assertEquals("SCHEMA_NAME", ((DropUniqueConstraintStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TAB_NAME", ((DropUniqueConstraintStatement) sqlStatements[0]).getTableName());
        assertEquals("UQ_CONSTRAINT", ((DropUniqueConstraintStatement) sqlStatements[0]).getConstraintName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Unique constraint UQ_CONSTRAINT dropped from TAB_NAME", change.getConfirmationMessage());
    }

}
