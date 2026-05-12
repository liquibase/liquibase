package liquibase.datatype;

import liquibase.database.core.MariaDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.core.BooleanType;
import liquibase.exception.UnexpectedLiquibaseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

// DataTypeFactory is needed so that finishInitialization() populates parameters
// (finishInitialization() alone only stores rawDefinition; it does NOT parse
// parameters — only fromDescription() / fromObject() do).

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
    
    /**
     * MySQL Connector/J with tinyInt1isBit=true (the driver default) reports TINYINT(1)
     * columns as TYPE_NAME="BIT", COLUMN_SIZE=1.  Without the fix, a snapshot of such a
     * column produces columnType="BIT(1)" in the changelog; re-applying it creates BIT(1)
     * instead of TINYINT(1), and every subsequent generateChangeLog reports a spurious diff.
     *
     * Uses DataTypeFactory so that parameters are properly parsed from the type string.
     */
    @Test
    public void mysqlBit1FromJdbcMapsToTinyInt1() {
        BooleanType bt = (BooleanType) DataTypeFactory.getInstance()
                .fromDescription("bit(1)", new MySQLDatabase());
        assertEquals(
            "MySQL TINYINT(1) snapshotted via JDBC as BIT(1) must round-trip back to TINYINT(1)",
            "TINYINT(1)",
            bt.toDatabaseDataType(new MySQLDatabase()).toString()
        );
    }

    @Test
    public void mysqlBooleanKeywordMapsToTinyInt1() {
        BooleanType bt = (BooleanType) DataTypeFactory.getInstance()
                .fromDescription("boolean", new MySQLDatabase());
        assertEquals(
            "MySQL boolean should emit TINYINT(1) — the de-facto MySQL boolean convention — "
                + "eliminating representation drift across changelog round-trips",
            "TINYINT(1)",
            bt.toDatabaseDataType(new MySQLDatabase()).toString()
        );
    }

    /**
     * A changeset with type="bit(8)" on MySQL explicitly wants a BIT field, not a boolean.
     * The fix must preserve BIT(n) for n > 1.
     */
    @Test
    public void mysqlBitNPreservedAsBitN() {
        BooleanType bt = (BooleanType) DataTypeFactory.getInstance()
                .fromDescription("bit(8)", new MySQLDatabase());
        assertEquals(
            "MySQL BIT(8) must not be collapsed to TINYINT(1) — only bit(1) is a TINYINT(1) alias",
            "BIT(8)",
            bt.toDatabaseDataType(new MySQLDatabase()).toString()
        );
    }

    @Test
    public void mariadbBit1PreservedAsBit() {
        BooleanType bt = (BooleanType) DataTypeFactory.getInstance()
                .fromDescription("bit(1)", new MariaDBDatabase());
        assertEquals(
            "MariaDB BIT(1) should stay as BIT(1), not be converted to TINYINT(1)",
            "BIT(1)",
            bt.toDatabaseDataType(new MariaDBDatabase()).toString()
        );
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void postgresqlBitStringError() {
        BooleanType bt = new BooleanType();
        bt.finishInitialization("bit(12)");

        assertNotEquals("b'1'", bt.objectToSql("'12'", new PostgresDatabase()));
    }
}
