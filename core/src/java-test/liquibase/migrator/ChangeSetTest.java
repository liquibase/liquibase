package liquibase.migrator;

import liquibase.ChangeSet;
import liquibase.change.CreateTableChange;
import liquibase.change.InsertDataChange;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link ChangeSet}
 */
public class ChangeSetTest {

    @Test
    public void getDescriptions() {
        ChangeSet changeSet = new ChangeSet("testId", "testAuthor", false, false, null, null, null);

        assertEquals("Empty", changeSet.getDescription());

        changeSet.addChange(new InsertDataChange());
        assertEquals("Insert Row", changeSet.getDescription());

        changeSet.addChange(new InsertDataChange());
        assertEquals("Insert Row (x2)", changeSet.getDescription());

        changeSet.addChange(new CreateTableChange());
        assertEquals("Insert Row (x2), Create Table", changeSet.getDescription());
    }
}