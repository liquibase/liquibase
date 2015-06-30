package liquibase.sqlgenerator.core;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.test.TestContext;

import org.junit.Test;

public class AlterSequenceGeneratorTest extends AbstractSqlGeneratorTest<AlterSequenceStatement> {
	
	protected static final String SEQUENCE_NAME = "SEQUENCE_NAME";
    protected static final String CATALOG_NAME = "CATALOG_NAME";
    protected static final String SCHEMA_NAME = "SCHEMA_NAME";
	
    public AlterSequenceGeneratorTest() throws Exception {
        super(new AlterSequenceGenerator());
    }

	@Test
    public void testAlterSequenceDatabase() throws Exception {
    	for (Database database : TestContext.getInstance().getAllDatabases()) {
    		if (database instanceof OracleDatabase) {
    			AlterSequenceStatement statement = new AlterSequenceStatement(CATALOG_NAME, SCHEMA_NAME, SEQUENCE_NAME);
	    		statement.setCacheSize(BigInteger.valueOf(3000L));

	    		Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

    			assertEquals("ALTER SEQUENCE CATALOG_NAME.SEQUENCE_NAME CACHE 3000", generatedSql[0].toSql());
    		}
    	}
    }

	@Override
	protected AlterSequenceStatement createSampleSqlStatement() {
		AlterSequenceStatement statement = new AlterSequenceStatement(CATALOG_NAME, SCHEMA_NAME, SEQUENCE_NAME);
        return statement;
	}
	
	@Override
	protected boolean shouldBeImplementation(Database database) {
		return database.supportsSequences();
	}
}
