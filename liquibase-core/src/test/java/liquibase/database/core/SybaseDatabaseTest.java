package liquibase.database.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class SybaseDatabaseTest {

	
	@Test
	public void testIsSybaseProductName() {
		SybaseDatabase database = new SybaseDatabase();
		assertTrue("Sybase SQL Server is a valid product name", database.isSybaseProductName("Sybase SQL Server"));
		assertTrue("sql server is a valid product name", database.isSybaseProductName("sql server"));
		assertTrue("ASE is a valid product name", database.isSybaseProductName("ASE"));
		assertTrue("Adaptive Server Enterprise is a valid product name", database.isSybaseProductName("Adaptive Server Enterprise"));
	}

}
