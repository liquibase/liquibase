package liquibase.changelog;

import liquibase.change.CheckSum;
import liquibase.change.core.AddDefaultValueChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.InsertDataChange;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link liquibase.changelog.ChangeSet}
 */
public class ChangeSetTest {

    @Test
    public void getDescriptions() {
        ChangeSet changeSet = new ChangeSet("testId", "testAuthor", false, false,null, null, null, null);

        assertEquals("Empty", changeSet.getDescription());

        String insertDescription = "insert";

        changeSet.addChange(new InsertDataChange());
        assertEquals(insertDescription, changeSet.getDescription());

        changeSet.addChange(new InsertDataChange());
        assertEquals(insertDescription + " (x2)", changeSet.getDescription());

        changeSet.addChange(new CreateTableChange());
        assertEquals(insertDescription + " (x2), createTable", changeSet.getDescription());
    }
    
    @Test
    public void generateCheckSum() {
        ChangeSet changeSet1 = new ChangeSet("testId", "testAuthor", false, false,null, null, null, null);
        ChangeSet changeSet2 = new ChangeSet("testId", "testAuthor", false, false,null, null, null, null);

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

    @Test
    public void isCheckSumValid_validCheckSum() {
        ChangeSet changeSet = new ChangeSet("1", "2",false, false, "/test.xml",null, null, null);
        CheckSum checkSum = changeSet.generateCheckSum();

        assertTrue(changeSet.isCheckSumValid(checkSum));
    }

    @Test
    public void isCheckSumValid_invalidCheckSum() {
        CheckSum checkSum = CheckSum.parse("2:asdf");

        ChangeSet changeSet = new ChangeSet("1", "2",false, false, "/test.xml",null, null, null);
        assertFalse(changeSet.isCheckSumValid(checkSum));
    }

    @Test
    public void isCheckSumValid_differentButValidCheckSum() {
        CheckSum checkSum = CheckSum.parse("2:asdf");

        ChangeSet changeSet = new ChangeSet("1", "2",false, false, "/test.xml",null, null, null);
        changeSet.addValidCheckSum(changeSet.generateCheckSum().toString());

        assertTrue(changeSet.isCheckSumValid(checkSum));
    }
}