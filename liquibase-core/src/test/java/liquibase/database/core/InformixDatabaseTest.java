package liquibase.database.core;

import junit.framework.TestCase;

public class InformixDatabaseTest extends TestCase {
	
	private InformixDatabase database;
	
	@Override
	protected void setUp() throws Exception {
		database = new InformixDatabase();
	}
	

	
	public void testGetDateLiteral() {
		String d;
		
		d = database.getDateLiteral("2010-11-12 13:14:15");
		assertEquals("DATETIME (2010-11-12 13:14:15) YEAR TO FRACTION(5)", d);
		
		d = database.getDateLiteral("2010-11-12");
		assertEquals("'2010-11-12'", d);
		
		d = database.getDateLiteral("13:14:15");
		assertEquals("DATETIME (13:14:15) HOUR TO FRACTION(5)", d);
	}


	public void testGetDefaultDriver() {
		assertEquals("com.informix.jdbc.IfxDriver",
				database.getDefaultDriver("jdbc:informix-sqli://localhost:9088/liquibase:informixserver=ol_ids_1150_1"));
	}
}
