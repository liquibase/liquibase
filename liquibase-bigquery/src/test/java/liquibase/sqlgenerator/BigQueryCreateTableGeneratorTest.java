package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.NumberDataTypeBigQuery;
import liquibase.sql.Sql;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.core.CreateTableStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryCreateTableGeneratorTest {

    private BigQueryCreateTableGenerator generator;
    private BigQueryDatabase database;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryCreateTableGenerator();
    }

    @Test
    void generateSql() {
        CreateTableStatement statement = new CreateTableStatement("catalog", "schema", "table");
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("CREATE TABLE schema.table ()", sql[0].toSql());
    }

    @Test
    void generateSqlWithPrimaryKey() {
        CreateTableStatement statement = new CreateTableStatement("catalog", "schema", "table");
        PrimaryKeyConstraint primaryKeyConstraint = new PrimaryKeyConstraint();
        primaryKeyConstraint.addColumns("column");
        statement.addColumn("column", new NumberDataTypeBigQuery(), primaryKeyConstraint);
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("CREATE TABLE schema.table (column NUMERIC, PRIMARY KEY (column) NOT ENFORCED)", sql[0].toSql());
    }
}