package liquibase.sqlgenerator.core;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import liquibase.database.core.MySQLDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.core.ProlongDatabaseChangeLogLockStatement;

public class ProlongDatabaseChangeLogLockGeneratorTest extends AbstractSqlGeneratorTest<ProlongDatabaseChangeLogLockStatement> {

    private static final String LOCKED_BY_ID = UUID.randomUUID().toString().toLowerCase();
    private static final Date LOCK_EXPIRES_ON_SERVER = new Date(1540639852624L);

    public ProlongDatabaseChangeLogLockGeneratorTest() throws Exception {
        super(new ProlongDatabaseChangeLogLockGenerator());
    }

    @Override
    protected ProlongDatabaseChangeLogLockStatement createSampleSqlStatement() {
        return new ProlongDatabaseChangeLogLockStatement(LOCKED_BY_ID, LOCK_EXPIRES_ON_SERVER);
    }

    @Test
    public void testValidation() {

        assertFalse(generatorUnderTest
            .validate(createSampleSqlStatement(), new MySQLDatabase(), new MockSqlGeneratorChain())
            .hasErrors());

    }

    @Test
    public void testGeneratedSQL() {

        System.out.println(new Date().getTime());

        Sql[] results = generatorUnderTest
            .generateSql(createSampleSqlStatement(), new MySQLDatabase(),
                new MockSqlGeneratorChain());

        assertThat(results,
            is(arrayWithSize(1)));

        String sql = results[0].toSql();

        assertThat(sql, startsWith("UPDATE DATABASECHANGELOGLOCK"));
        assertThat(sql, containsString("SET LOCKEXPIRES = '2018-10-27 13:30:52.624'"));
        assertThat(sql, containsString("ID = 1"));
        assertThat(sql, containsString("`LOCKED` = 1"));
        assertThat(sql, containsString("LOCKEXPIRES IS NOT NULL"));
        assertThat(sql, containsString("LOCKEDBYID = '" + LOCKED_BY_ID + "';"));

    }
}
