package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.jvm.JdbcConnection;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SnowflakeDatabaseTest {

    SnowflakeDatabase database;

    @Before
    public void setup() {
        database = new SnowflakeDatabase();
    }

    @Test
    public void testGetShortName() {
        assertEquals("snowflake", database.getShortName());
    }

    @Test
    public void testGetDefaultDatabaseProductName() {
        assertEquals("Snowflake", database.getDefaultDatabaseProductName());
    }

    @Test
    public void testGetDefaultPort() {
        assertNull(database.getDefaultPort());
    }

    @Test
    public void testGetCurrentTimeFunction() {
        assertEquals("current_timestamp::timestamp_ntz", database.getCurrentDateTimeFunction());
    }

    @Test
    public void testGetPriority() {
        assertEquals(PRIORITY_DATABASE, database.getPriority());
    }

    @Test
    public void testSupportsInitiallyDeferrableColumns() {
        assertFalse(database.supportsInitiallyDeferrableColumns());
    }

    @Test
    public void testSupportsDropTableCascadeConstraints() {
        assertTrue(database.supportsDropTableCascadeConstraints());
    }

    @Test
    public void testIsCorrectDatabaseImplementation() throws Exception {
        JdbcConnection jdbcConnection = mock(JdbcConnection.class);
        when(jdbcConnection.getDatabaseProductName()).thenReturn("Snowflake");
        assertTrue(database.isCorrectDatabaseImplementation(jdbcConnection));
    }

    @Test
    public void testGetDefaultDriver() {
        assertEquals("net.snowflake.client.jdbc.SnowflakeDriver", database.getDefaultDriver("jdbc:snowflake:"));
        assertNull(database.getDefaultDriver("jdbc:wrong-name:"));
    }

    @Test
    public void testSupportsSchemas() {
        assertTrue(database.supportsSchemas());
        assertTrue(database.supports(Schema.class));
    }

    @Test
    public void testSupportsCatalogs() {
        assertTrue(database.supportsCatalogs());
        assertTrue(database.supports(Catalog.class));
    }

    @Test
    public void testSupportsCatalogInObjectName() {
        assertFalse(database.supportsCatalogInObjectName(null));
    }

    @Test
    public void testSupportsSequences() {
        assertTrue(database.supportsSequences());
        assertTrue(database.supports(Sequence.class));
    }

    @Test
    public void testGetDatabaseChangeLogTableName() {
        assertEquals("DATABASECHANGELOG", database.getDatabaseChangeLogTableName());
    }

    @Test
    public void testGetDatabaseChangeLogLockTableName() {
        assertEquals("DATABASECHANGELOGLOCK", database.getDatabaseChangeLogLockTableName());
    }

    @Test
    public void testSupportsTablespaces() {
        assertFalse(database.supportsTablespaces());
    }

    @Test
    public void testSupportsAutoIncrementClause() {
        assertTrue(database.supportsAutoIncrement());
    }

    @Test
    public void testGetAutoIncrementClause() {
        assertEquals("AUTOINCREMENT", database.getAutoIncrementClause());
        assertEquals("AUTOINCREMENT (1, 1)", database.getAutoIncrementClause(null, null, null, null));
        assertEquals("AUTOINCREMENT (1, 1)", database.getAutoIncrementClause(new BigInteger("1"), new BigInteger("1"), null, null));
        assertEquals("AUTOINCREMENT (7, 1)", database.getAutoIncrementClause(new BigInteger("7"), new BigInteger("1"), null, null));
        assertEquals("AUTOINCREMENT (1, 7)", database.getAutoIncrementClause(new BigInteger("1"), new BigInteger("7"), null, null));
        assertEquals("AUTOINCREMENT (7, 1)", database.getAutoIncrementClause(new BigInteger("7"), null, null, null));
        assertEquals("AUTOINCREMENT (1, 7)", database.getAutoIncrementClause(null, new BigInteger("7"), null, null));
    }

    @Test
    public void testGenerateAutoIncrementStartWith() {
        assertTrue(database.generateAutoIncrementStartWith(new BigInteger("1")));
    }

    @Test
    public void testGenerateAutoIncrementBy() {
        assertTrue(database.generateAutoIncrementBy(new BigInteger("1")));
    }

    @Test
    public void testSupportsRestrictForeignKeys() {
        assertTrue(database.supportsRestrictForeignKeys());
    }

    @Test
    public void testIsReservedWord() {
        assertTrue(database.isReservedWord("table"));
    }

    @Test
    public void defaultCatalogNameIsNullWhenConnectionIsNull() throws Exception {
        assertNull(database.getDefaultCatalogName());
    }

    @Test
    public void defaultSchemaNameIsNullWhenConnectionIsNull() throws Exception {
        assertNull(database.getDefaultSchemaName());
    }


    @Test
    public void jdbcCatalogNameIsNullWhenCatalogAndSchemaAreNull() {
        assertNull(database.getJdbcCatalogName(new CatalogAndSchema(null, null)));
    }

    @Test
    public void jdbcSchemaNameIsNullWhenCatalogAndSchemaAreNull() {
        assertNull(database.getJdbcSchemaName(new CatalogAndSchema(null, null)));
    }

}
