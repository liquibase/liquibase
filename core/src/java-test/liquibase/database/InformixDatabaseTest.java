package liquibase.database;

import junit.framework.TestCase;

public class InformixDatabaseTest extends TestCase {
	
	private InformixDatabase database;
	
	@Override
	protected void setUp() throws Exception {
		database = new InformixDatabase();
	}
	
	public void testGetColumnType() {
		String type;
		
		type = database.getColumnType("int", true);
		assertEquals("SERIAL", type);

		type = database.getColumnType("INT", true);
		assertEquals("SERIAL", type);
		
		type = database.getColumnType("integer", true);
		assertEquals("SERIAL", type);

		type = database.getColumnType("INTEGER", true);
		assertEquals("SERIAL", type);
		
		type = database.getColumnType("BIGINT", true);
		assertEquals("SERIAL8", type);
		
		type = database.getColumnType("bigint", true);
		assertEquals("SERIAL8", type);

		type = database.getColumnType("int8", true);
		assertEquals("SERIAL8", type);

		try {
			type = database.getColumnType("integ", true);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Unknown autoincrement type: integ", e.getMessage());
		}
		
		try {
			type = database.getColumnType("varchar(10)", true);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Unknown autoincrement type: varchar(10)", e.getMessage());
		}
	}
	
	public void testGetDateLiteral() {
		String d;
		
		d = database.getDateLiteral("2010-11-12 13:14:15");
		assertEquals("DATETIME (2010-11-12 13:14:15) YEAR TO FRACTION(5)", d);
		
		d = database.getDateLiteral("2010-11-12");
		assertEquals("'2010-11-12'", d);
		
		d = database.getDateLiteral("13:14:15");
		assertEquals("INTERVAL (13:14:15) HOUR TO FRACTION(5)", d);
	}

	public void testConvertJavaObjectToStringWithBoolean() {
		String s;
		
		s = database.convertJavaObjectToString(Boolean.TRUE);
		assertEquals("'t'", s);
		
		s = database.convertJavaObjectToString(Boolean.FALSE);
		assertEquals("'f'", s);
	}
	
	public void testGetDefaultDriver() {
		assertEquals("com.informix.jdbc.IfxDriver",
				database.getDefaultDriver("jdbc:informix-sqli://localhost:9088/liquibase:informixserver=ol_ids_1150_1"));
	}
}
