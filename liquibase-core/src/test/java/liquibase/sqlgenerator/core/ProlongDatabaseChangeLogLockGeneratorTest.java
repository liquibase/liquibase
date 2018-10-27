package liquibase.sqlgenerator.core;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private static final Date LOCK_EXPIRES_ON_SERVER = parse("2018-10-27T14:38:01Z");

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

        Sql[] results = generatorUnderTest
            .generateSql(createSampleSqlStatement(), new MySQLDatabase(),
                new MockSqlGeneratorChain());

        assertThat(results,
            is(arrayWithSize(1)));

        String sql = results[0].toSql();

        assertThat(sql, startsWith("UPDATE DATABASECHANGELOGLOCK"));
        assertThat(sql, containsString("SET LOCKEXPIRES = '2018-10-27 14:38:01.000'"));
        assertThat(sql, containsString("ID = 1"));
        assertThat(sql, containsString("`LOCKED` = 1"));
        assertThat(sql, containsString("LOCKEXPIRES IS NOT NULL"));
        assertThat(sql, containsString("LOCKEDBYID = '" + LOCKED_BY_ID + "';"));

    }

    private static Date parse(String dateString)   {

        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .parse(dateString);

        } catch (ParseException e) {
            // not expected
            throw new RuntimeException(e);
        }
    }

}
