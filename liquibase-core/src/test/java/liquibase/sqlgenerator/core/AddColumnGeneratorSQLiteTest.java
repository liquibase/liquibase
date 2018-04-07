package liquibase.sqlgenerator.core;

import org.junit.Test;

import liquibase.database.core.MariaDBDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.core.AddColumnStatement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AddColumnGeneratorSQLiteTest {

    @Test
    public void supportsSQLLite() {
        AddColumnStatement any = mock(AddColumnStatement.class);
        assertTrue(new AddColumnGeneratorSQLite().supports(any, new SQLiteDatabase()));
    }

    @Test
    public void doesNotSupportMariaDB() {
        AddColumnStatement any = mock(AddColumnStatement.class);
        assertFalse(new AddColumnGeneratorSQLite().supports(any, new MariaDBDatabase()));
    }

}