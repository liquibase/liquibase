package liquibase.sqlgenerator.core;

import org.junit.Test;

import liquibase.database.core.MariaDBDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.core.AddColumnStatement
import spock.lang.Specification;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AddColumnGeneratorSQLiteTest extends Specification {

    @Test
    public void supportsSQLLite() {
        AddColumnStatement any = Mock(AddColumnStatement.class);
        assertTrue(new AddColumnGeneratorSQLite().supports(any, new SQLiteDatabase()));
    }

    @Test
    public void doesNotSupportMariaDB() {
        AddColumnStatement any = Mock(AddColumnStatement.class);
        assertFalse(new AddColumnGeneratorSQLite().supports(any, new MariaDBDatabase()));
    }

}