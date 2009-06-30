package liquibase.change.core;

import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.TagDatabaseStatement;
import liquibase.change.core.TagDatabaseChange;
import liquibase.change.AbstractChangeTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class TagDatabaseChangeTest extends AbstractChangeTest {

    private TagDatabaseChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new TagDatabaseChange();
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Tag Database", refactoring.getChangeMetaData().getDescription());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        TagDatabaseChange refactoring = new TagDatabaseChange();
        refactoring.setTag("TAG_NAME");

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof TagDatabaseStatement);
        assertEquals("TAG_NAME", ((TagDatabaseStatement) sqlStatements[0]).getTag());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        refactoring.setTag("TAG_NAME");

        assertEquals("Tag 'TAG_NAME' applied to database", refactoring.getConfirmationMessage());
    }   
}