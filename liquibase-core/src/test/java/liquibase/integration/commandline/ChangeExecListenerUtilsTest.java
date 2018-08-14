package liquibase.integration.commandline;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class ChangeExecListenerUtilsTest extends Assert {
//	private Database database = createMock(Database.class);
    private ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();

    private File tmpFile;

    @Before
    public void setup() throws Exception {
        tmpFile = File.createTempFile("changeExecListener", ".properties");
        FileOutputStream out = new FileOutputStream(tmpFile);

        Properties properties = new Properties();
        properties.put("test", "value");
        properties.put("exclusions", "table.name, table2.name2");
        properties.store(out, "");
    }

    @After
    public void tearDown() throws Exception {
        tmpFile.delete();
    }

//	@Test
//	public void testWithProperties() throws Exception {
//		ChangeExecListenerWithProperties listener =
//				(ChangeExecListenerWithProperties) ChangeExecListenerUtils.getChangeExecListener(database, resourceAccessor,
//			ChangeExecListenerWithProperties.class.getName(),
//			tmpFile.getAbsolutePath());
//
//		assertEquals("value", listener.getProperties().get("test"));
//		assertEquals("table.name, table2.name2", listener.getProperties().get("exclusions"));
//	}
//
//	@Test
//	public void testWithPropertiesAndEmptyConstructor() throws Exception {
//		ChangeExecListenerNoPropertiesOrDatabase listener =
//				(ChangeExecListenerNoPropertiesOrDatabase) ChangeExecListenerUtils.getChangeExecListener(database, resourceAccessor,
//						ChangeExecListenerNoPropertiesOrDatabase.class.getName(),
//			tmpFile.getAbsolutePath());
//		assertNotNull(listener);
//	}
//
//	@Test
//	public void testNoPropertiesOrDatabase() throws Exception {
//		ChangeExecListenerNoPropertiesOrDatabase listener = (ChangeExecListenerNoPropertiesOrDatabase)
//		ChangeExecListenerUtils.getChangeExecListener(database, resourceAccessor,
//				ChangeExecListenerNoPropertiesOrDatabase.class.getName(), null);
//		assertNotNull(listener);
//	}
//
//	@Test
//	public void testWithDatabaseNoProperties() throws Exception {
//		ChangeExecListenerWithDatabase listener = (ChangeExecListenerWithDatabase)
//				ChangeExecListenerUtils.getChangeExecListener(database, resourceAccessor,
//				ChangeExecListenerWithDatabase.class.getName(), null);
//
//		assertEquals(database, listener.getDatabase());
//	}
//
//	@Test
//	public void testWithPropertiesAndDatabase() throws Exception {
//		ChangeExecListenerWithPropertiesAndDatabase listener =
//				(ChangeExecListenerWithPropertiesAndDatabase) ChangeExecListenerUtils.getChangeExecListener(
//						database, resourceAccessor,
//						ChangeExecListenerWithPropertiesAndDatabase.class.getName(),
//			tmpFile.getAbsolutePath());
//
//		assertEquals("value", listener.getProperties().get("test"));
//		assertEquals("table.name, table2.name2", listener.getProperties().get("exclusions"));
//		assertEquals(database, listener.getDatabase());
//	}
//
//	// make it a bit simpler by handling both combinations of Database and Properties
//	@Test
//	public void testWithDatabaseAndProperties() throws Exception {
//		ChangeExecListenerWithDatabaseAndProperties listener =
//		(ChangeExecListenerWithDatabaseAndProperties) ChangeExecListenerUtils.getChangeExecListener(
//				database, resourceAccessor,
//				ChangeExecListenerWithDatabaseAndProperties.class.getName(),
//				tmpFile.getAbsolutePath());
//
//		assertEquals("value", listener.getProperties().get("test"));
//		assertEquals("table.name, table2.name2", listener.getProperties().get("exclusions"));
//		assertEquals(database, listener.getDatabase());
//	}
//
//	@Test
//	public void testWithDatabaseAndNullProperties() throws Exception {
//		ChangeExecListenerWithDatabaseAndProperties listener =
//				(ChangeExecListenerWithDatabaseAndProperties) ChangeExecListenerUtils.getChangeExecListener(
//						database, resourceAccessor,
//						ChangeExecListenerWithDatabaseAndProperties.class.getName(),
//			null);
//
//		assertNull(listener.getProperties());
//		assertEquals(database, listener.getDatabase());
//	}
}
