package liquibase.sqlgenerator.core;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return new RemoveStaleLocksStatement(5);
    }

    @Test
    public void testValidation() {

        assertFalse(generatorUnderTest
            .validate(createSampleSqlStatement(), new MySQLDatabase(), new MockSqlGeneratorChain())
            .hasErrors());

        assertTrue(generatorUnderTest
            .validate(new RemoveStaleLocksStatement(0), new MySQLDatabase(),
                new MockSqlGeneratorChain())
            .hasErrors());

        assertTrue(generatorUnderTest
            .validate(new RemoveStaleLocksStatement(-1), new MySQLDatabase(),
                new MockSqlGeneratorChain())
            .hasErrors());

    }

    @Test
    public void testGeneratedSQL() {

        long maxTTLInSeconds = 5;

        Date nowBeforeTest = new Date();

        Sql[] results = generatorUnderTest
            .generateSql(new RemoveStaleLocksStatement(maxTTLInSeconds), new MySQLDatabase(),
                new MockSqlGeneratorChain());

        Date nowAfterTest = new Date();

        assertThat(results,
            is(arrayWithSize(1)));

        String sql = results[0].toSql();

        assertThat(sql, startsWith("DELETE FROM DATABASECHANGELOGLOCK WHERE LOCKPROLONGED < "));
        assertThat(sql, endsWith(" AND LOCKPROLONGED IS NOT NULL"));

        // parse timestamp
        Matcher matcher = Pattern.compile("'(.*)'").matcher(sql);
        assertTrue(matcher.find());

        Timestamp timestampInSql = Timestamp.valueOf(matcher.group(1));

        Timestamp beforeTestMinusTTL =
            new Timestamp(nowBeforeTest.getTime() - maxTTLInSeconds * 1000);
        Timestamp afterTestMinusTTL =
            new Timestamp(nowAfterTest.getTime() - maxTTLInSeconds * 1000);

        System.out.println(beforeTestMinusTTL);
        System.out.println(timestampInSql);
        System.out.println(afterTestMinusTTL);

        assertTrue(timestampInSql.equals(beforeTestMinusTTL) || timestampInSql.after(beforeTestMinusTTL));
        assertTrue(timestampInSql.equals(afterTestMinusTTL) || timestampInSql.before(afterTestMinusTTL));

    }
}
