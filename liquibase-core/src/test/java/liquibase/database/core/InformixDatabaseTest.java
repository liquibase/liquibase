package liquibase.database.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class InformixDatabaseTest {

	private InformixDatabase database;

	@BeforeEach
	protected void setUp() throws Exception {
		database = new InformixDatabase();
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			" 2010-11-12 13:14:15 | DATETIME (2010-11-12 13:14:15) YEAR TO FRACTION(5) ",
			" 2010-11-12          | '''2010-11-12'''                                   ",
			" 2010-11-12          | '''2010-11-12'''                                   ",
			" 13:14:15            | DATETIME (13:14:15) HOUR TO FRACTION(5)            ",
	})
	public void testGetDateLiteral(String isoDate, String expected) {
		assertEquals(expected, database.getDateLiteral(isoDate));
	}

	@Test
	public void testGetDefaultDriver() {
		assertEquals("com.informix.jdbc.IfxDriver",
				database.getDefaultDriver("jdbc:informix-sqli://localhost:9088/liquibase:informixserver=ol_ids_1150_1"));
	}
}
