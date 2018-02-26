package liquibase.sqlgenerator.core;

import static org.junit.Assert.assertEquals;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import liquibase.database.core.InformixDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorInformixTest {
	@Test
	public void shouldGenerateINT8TypeForBIGINTAndDefaultValue() throws Exception {
		AddDefaultValueGeneratorInformix informix = new AddDefaultValueGeneratorInformix();

		AddDefaultValueStatement statement = new AddDefaultValueStatement(null, null, "tbl1", "id", "BIGINT", 1);
		InformixDatabase database = new InformixDatabase();
		SortedSet<SqlGenerator> sqlGenerators = new TreeSet<SqlGenerator>();
		SqlGeneratorChain sqlGenerationChain = new SqlGeneratorChain(sqlGenerators);
		Sql[] sqls = informix.generateSql(statement, database, sqlGenerationChain);
		assertEquals("ALTER TABLE tbl1 MODIFY (id INT8 DEFAULT 1);", sqls[0].toSql());

	}
}
