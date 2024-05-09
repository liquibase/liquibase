package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryCreateDatabaseChangeLogLockTableGeneratorTest {

    private BigQueryCreateDatabaseChangeLogLockTableGenerator generator;
    private BigQueryDatabase database;
    private CreateDatabaseChangeLogLockTableStatement statement;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryCreateDatabaseChangeLogLockTableGenerator();
        statement = new CreateDatabaseChangeLogLockTableStatement();
    }

    @Test
    void generateSql() {
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("CREATE TABLE DATABASECHANGELOGLOCK (ID INT, LOCKED BOOLEAN, LOCKGRANTED datetime, LOCKEDBY STRING(255))", sql[0].toSql());
    }
}