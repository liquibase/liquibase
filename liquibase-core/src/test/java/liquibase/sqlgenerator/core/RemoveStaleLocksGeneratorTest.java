package liquibase.sqlgenerator.core;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import liquibase.database.core.MySQLDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.core.RemoveStaleLocksStatement;

public class RemoveStaleLocksGeneratorTest extends AbstractSqlGeneratorTest<RemoveStaleLocksStatement> {

    public RemoveStaleLocksGeneratorTest() throws Exception {
        super(new RemoveStaleLocksGenerator());
    }

    @Override
    protected RemoveStaleLocksStatement createSampleSqlStatement() {
        return new RemoveStaleLocksStatement();
    }

    @Test
    public void testValidation() {

        assertFalse(generatorUnderTest
            .validate(createSampleSqlStatement(), new MySQLDatabase(), new MockSqlGeneratorChain())
            .hasErrors());

    }

    @Test
    public void testGeneratedSQL() {

        Sql[] results = generatorUnderTest
            .generateSql(new RemoveStaleLocksStatement(), new MySQLDatabase(),
                new MockSqlGeneratorChain());

        assertThat(results,
            is(arrayWithSize(1)));

        String sql = results[0].toSql();

        assertThat(sql, startsWith("UPDATE DATABASECHANGELOGLOCK"));
        assertThat(sql, containsString("ID = 1"));
        assertThat(sql, containsString("LOCKED = 1"));
        assertThat(sql, containsString("LOCKEXPIRES < NOW()"));
        assertThat(sql, containsString("LOCKEDBYID IS NOT NULL"));

    }
}
