package liquibase.migrator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import liquibase.migrator.change.CreateTableChange;
import liquibase.migrator.change.InsertDataChange;

/**
 * Tests for {@link ChangeSet}
 */
public class ChangeSetTest {

    @Test
    public void getDescriptions() {
        ChangeSet changeSet = new ChangeSet("testId", "testAuthor", false, false, null, null);

        assertEquals("Empty", changeSet.getDescription());

        changeSet.addChange(new InsertDataChange());
        assertEquals("Insert Row", changeSet.getDescription());

        changeSet.addChange(new InsertDataChange());
        assertEquals("Insert Row (x2)", changeSet.getDescription());

        changeSet.addChange(new CreateTableChange());
        assertEquals("Insert Row (x2), Create Table", changeSet.getDescription());
    }
}