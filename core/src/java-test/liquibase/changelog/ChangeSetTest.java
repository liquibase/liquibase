package liquibase.changelog;

import liquibase.change.core.AddDefaultValueChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.InsertDataChange;
import liquibase.change.CheckSum;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 * Tests for {@link liquibase.changelog.ChangeSet}
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
    public void generateCheckSum() {
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

        CheckSum md5Sum1 = changeSet1.generateCheckSum();

        change.setSchemaName("SCHEMA_NAME2");
        CheckSum md5Sum2 = changeSet2.generateCheckSum();
        assertFalse(md5Sum1.equals(md5Sum2));
    }
}