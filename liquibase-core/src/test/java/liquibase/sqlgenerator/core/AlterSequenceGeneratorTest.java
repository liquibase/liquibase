package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.CockroachDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.test.TestContext;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class AlterSequenceGeneratorTest extends AbstractSqlGeneratorTest<AlterSequenceStatement> {

    protected static final String SEQUENCE_NAME = "SEQUENCE_NAME";
    protected static final String CATALOG_NAME = "CATALOG_NAME";
    protected static final String SCHEMA_NAME = "SCHEMA_NAME";

    public AlterSequenceGeneratorTest() throws Exception {
        super(new AlterSequenceGenerator());
    }

    @Test
    public void testAlterSequenceDatabase(){
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                AlterSequenceStatement statement = createSampleSqlStatement();
                statement.setCacheSize(BigInteger.valueOf(3000L));

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("ALTER SEQUENCE CATALOG_NAME.SEQUENCE_NAME CACHE 3000", generatedSql[0].toSql());
            }
        }
    }

    @Test
    public void testAlterSequenceCycleDatabase() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            AlterSequenceStatement statement = createSampleSqlStatement();
            statement.setCycle(false);
            Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
            if (database instanceof OracleDatabase) {
                assertEquals("ALTER SEQUENCE CATALOG_NAME.SEQUENCE_NAME NO CYCLE", generatedSql[0].toSql());
            } else if (database instanceof PostgresDatabase || database instanceof CockroachDatabase) {
                assertEquals("ALTER SEQUENCE SCHEMA_NAME.SEQUENCE_NAME NO CYCLE", generatedSql[0].toSql());
            }
        }
    }

    @Override
    protected AlterSequenceStatement createSampleSqlStatement() {
        return new AlterSequenceStatement(CATALOG_NAME, SCHEMA_NAME, SEQUENCE_NAME);
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database.supportsSequences();
    }
}
