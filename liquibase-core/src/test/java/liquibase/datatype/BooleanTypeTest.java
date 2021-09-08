package liquibase.datatype;

import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.core.BooleanType;
import liquibase.exception.UnexpectedLiquibaseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BooleanTypeTest {

    @Test
    public void postgresqlBitString() {
        BooleanType bt = new BooleanType();
        bt.finishInitialization("bit(12)");
        
        String expected = "b'111111111111'::\"bit\"";

        assertEquals(expected, bt.objectToSql("'111111111111'", new PostgresDatabase()));
        assertEquals(expected, bt.objectToSql("b'111111111111'", new PostgresDatabase()));
        assertEquals(expected, bt.objectToSql("'111111111111'::bit", new PostgresDatabase()));
        assertEquals(expected, bt.objectToSql("b'111111111111'::bit", new PostgresDatabase()));
        assertEquals(expected, bt.objectToSql("'111111111111'::\"bit\"", new PostgresDatabase()));
        assertEquals(expected, bt.objectToSql("b'111111111111'::\"bit\"", new PostgresDatabase()));
    }
    
    @Test(expected = UnexpectedLiquibaseException.class)
    public void postgresqlBitStringError() {
        BooleanType bt = new BooleanType();
        bt.finishInitialization("bit(12)");

        assertNotEquals("b'1'", bt.objectToSql("'12'", new PostgresDatabase()));
    }
}
