package liquibase.sqlgenerator.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.RenameTableStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenameTableGeneratorSnowflakeTest {

    private RenameTableGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    private RenameTableStatement statement;

    @BeforeEach
    void setUp() {
        generator = new RenameTableGeneratorSnowflake();
        database = new SnowflakeDatabase();
        statement = new RenameTableStatement(null, null, "old_table", "new_table");
    }

    @Test
    void testSupports() {
        assertTrue(generator.supports(statement, database));
        assertFalse(generator.supports(statement, new PostgresDatabase()));
    }

    @Test
    void testPriority() {
        assertEquals(generator.PRIORITY_DATABASE, generator.getPriority());
    }

    @Test
    void testGenerateSqlBasic() {
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertNotNull(sql);
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER TABLE old_table RENAME TO new_table", sqlText);
    }

    @Test
    void testGenerateSqlWithSchema() {
        statement = new RenameTableStatement(null, "test_schema", "old_table", "new_table");
        
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER TABLE test_schema.old_table RENAME TO test_schema.new_table", sqlText);
    }

    @Test
    void testGenerateSqlWithCatalogAndSchema() {
        statement = new RenameTableStatement("test_catalog", "test_schema", "old_table", "new_table");
        
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER TABLE test_catalog.test_schema.old_table RENAME TO test_catalog.test_schema.new_table", sqlText);
    }

    @Test
    void testGenerateSqlWithSpecialCharacters() {
        statement = new RenameTableStatement(null, null, "old-table", "new table");
        
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        String sqlText = sql[0].toSql();
        assertEquals("ALTER TABLE \"old-table\" RENAME TO \"new table\"", sqlText);
    }

    @Test
    void testAffectedTables() {
        Sql[] sql = generator.generateSql(statement, database, null);
        
        assertEquals(1, sql.length);
        assertNotNull(sql[0].getAffectedDatabaseObjects());
        // Generator may return 2 or 3 affected objects depending on implementation
        assertTrue(sql[0].getAffectedDatabaseObjects().size() >= 2);
    }
}