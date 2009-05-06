package liquibase;

import liquibase.change.AddDefaultValueChange;
import liquibase.change.CreateTableChange;
import liquibase.change.InsertDataChange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 * Tests for {@link liquibase.ChangeSet}
 */
public class ChangeSetTest {

    @Test
    public void getDescriptions() {
        ChangeSet changeSet = new ChangeSet("testId", "testAuthor", false, false,null,  null, null, null);

        assertEquals("Empty", changeSet.getDescription());

        changeSet.addChange(new InsertDataChange());
        assertEquals("Insert Row", changeSet.getDescription());

        changeSet.addChange(new InsertDataChange());
        assertEquals("Insert Row (x2)", changeSet.getDescription());

        changeSet.addChange(new CreateTableChange());
        assertEquals("Insert Row (x2), Create Table", changeSet.getDescription());
    }
    
    @Test
    public void getMd5Sum() {
        ChangeSet changeSet1 = new ChangeSet("testId", "testAuthor", false, false,null,  null, null, null);
        ChangeSet changeSet2 = new ChangeSet("testId", "testAuthor", false, false,null,  null, null, null);

        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setDefaultValue("DEF STRING");
        change.setDefaultValueNumeric("42");
        change.setDefaultValueBoolean(true);
        change.setDefaultValueDate("2007-01-02");

        changeSet1.addChange(change);
        changeSet2.addChange(change);

        String md5Sum1 = changeSet1.getMd5sum();

        change.setSchemaName("SCHEMA_NAME2");
        String md5Sum2 = changeSet2.getMd5sum();
        assertFalse(md5Sum1.equals(md5Sum2));
    }
}