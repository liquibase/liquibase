package liquibase.sqlgenerator;

import liquibase.exception.ValidationErrors;
import liquibase.database.core.MockDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.MockSqlStatement;
import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class SqlGeneratorChainTest {

    @Test
    public void generateSql_nullGenerators() {
        SqlGeneratorChain chain = new SqlGeneratorChain(null);

        assertNull(chain.generateSql(new MockSqlStatement(), new MockDatabase()));
    }

    @Test
    public void generateSql_noGenerators() {
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new SqlGeneratorComparator());
        SqlGeneratorChain chain = new SqlGeneratorChain(generators);

        assertEquals(0, chain.generateSql(new MockSqlStatement(), new MockDatabase()).length);
    }

    @Test
    public void generateSql_oneGenerators() {
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new SqlGeneratorComparator());
        generators.add(new MockSqlGenerator(1, "A1", "A2"));
        SqlGeneratorChain chain = new SqlGeneratorChain(generators);

        Sql[] sql = chain.generateSql(new MockSqlStatement(), new MockDatabase());
        assertEquals(2, sql.length);
        assertEquals("A1", sql[0].toSql());
        assertEquals("A2", sql[1].toSql());
    }

    @Test
    public void generateSql_twoGenerators() {
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new SqlGeneratorComparator());
        generators.add(new MockSqlGenerator(2, "B1", "B2"));
        generators.add(new MockSqlGenerator(1, "A1", "A2"));
        SqlGeneratorChain chain = new SqlGeneratorChain(generators);

        Sql[] sql = chain.generateSql(new MockSqlStatement(), new MockDatabase());
        assertEquals(4, sql.length);
        assertEquals("B1", sql[0].toSql());
        assertEquals("B2", sql[1].toSql());
        assertEquals("A1", sql[2].toSql());
        assertEquals("A2", sql[3].toSql());
    }

    @Test
    public void generateSql_threeGenerators() {
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new SqlGeneratorComparator());
        generators.add(new MockSqlGenerator(2, "B1", "B2"));
        generators.add(new MockSqlGenerator(1, "A1", "A2"));
        generators.add(new MockSqlGenerator(3, "C1", "C2"));
        SqlGeneratorChain chain = new SqlGeneratorChain(generators);

        Sql[] sql = chain.generateSql(new MockSqlStatement(), new MockDatabase());
        assertEquals(6, sql.length);
        assertEquals("C1", sql[0].toSql());
        assertEquals("C2", sql[1].toSql());
        assertEquals("B1", sql[2].toSql());
        assertEquals("B2", sql[3].toSql());
        assertEquals("A1", sql[4].toSql());
        assertEquals("A2", sql[5].toSql());
    }

    @Test
    public void validate_nullGenerators() {
        SqlGeneratorChain chain = new SqlGeneratorChain(null);
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase());
        assertFalse(validationErrors.hasErrors());
    }

    @Test
    public void validate_oneGenerators_noErrors() {
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new SqlGeneratorComparator());
        generators.add(new MockSqlGenerator(1, "A1", "A2"));

        SqlGeneratorChain chain = new SqlGeneratorChain(generators);
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase());
        assertFalse(validationErrors.hasErrors());
    }

    @Test
    public void validate_oneGenerators_hasErrors() {
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new SqlGeneratorComparator());
        generators.add(new MockSqlGenerator(1, "A1", "A2").addValidationError("E1"));

        SqlGeneratorChain chain = new SqlGeneratorChain(generators);
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase());
        assertTrue(validationErrors.hasErrors());
    }

    @Test
    public void validate_twoGenerators_noErrors() {
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new SqlGeneratorComparator());
        generators.add(new MockSqlGenerator(2, "B1", "B2"));
        generators.add(new MockSqlGenerator(1, "A1", "A2"));

        SqlGeneratorChain chain = new SqlGeneratorChain(generators);
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase());
        assertFalse(validationErrors.hasErrors());
    }

    @Test
    public void validate_twoGenerators_firstHasErrors() {
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new SqlGeneratorComparator());
        generators.add(new MockSqlGenerator(2, "B1", "B2").addValidationError("E1"));
        generators.add(new MockSqlGenerator(1, "A1", "A2"));

        SqlGeneratorChain chain = new SqlGeneratorChain(generators);
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase());
        assertTrue(validationErrors.hasErrors());
    }

    @Test
    public void validate_twoGenerators_secondHasErrors() {
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new SqlGeneratorComparator());
        generators.add(new MockSqlGenerator(2, "B1", "B2"));
        generators.add(new MockSqlGenerator(1, "A1", "A2").addValidationError("E1"));

        SqlGeneratorChain chain = new SqlGeneratorChain(generators);
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase());
        assertTrue(validationErrors.hasErrors());
    }
}
