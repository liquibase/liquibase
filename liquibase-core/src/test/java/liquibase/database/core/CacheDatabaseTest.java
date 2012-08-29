package liquibase.database.core;

import liquibase.database.AbstractDatabaseTest;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

public class CacheDatabaseTest extends AbstractDatabaseTest {

	public CacheDatabaseTest() throws Exception {
        super(new CacheDatabase());
    }
	
	@Override
    protected String getProductNameString() {
	      return "Cache";
	    }
	
	@Test
	public void supportsSequences() {
		assertFalse(database.supportsSequences());
	}


	@Test
	public void getLineComment() {
		Assert.assertEquals("--", database.getLineComment());
	}

	@Test
	public void getDefaultDriver() {
		Assert.assertEquals("com.intersys.jdbc.CacheDriver",
				database.getDefaultDriver("jdbc:Cache://127.0.0.1:56773/TESTMIGRATE"));
	}

	@Test
	public void getTypeName() {
		Assert.assertEquals("cache", database.getShortName());
	}



	@Override
    @Test
	public void getCurrentDateTimeFunction() {
		Assert.assertEquals("SYSDATE", database.getCurrentDateTimeFunction());
	}

	@Override
    @Test
	public void supportsInitiallyDeferrableColumns() {
		assertFalse(database.supportsInitiallyDeferrableColumns());
	}
}
