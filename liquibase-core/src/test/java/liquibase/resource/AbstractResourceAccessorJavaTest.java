package liquibase.resource;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.junit.Test;

public class AbstractResourceAccessorJavaTest {

	@Test
	public void testConvertToPathIsInsensibleToTrailingSlashOnRootPath() throws MalformedURLException {
		{
			AbstractResourceAccessor ara = new MyARA();
			ara.addRootPath(new URL("file:/a/"));
			String path = ara.convertToPath("file:/a/b");
			assertEquals("b", path);
		}
		{
			AbstractResourceAccessor ara = new MyARA();
			ara.addRootPath(new URL("file:/a"));
			String path = ara.convertToPath("file:/a/b");
			//path must not be '/b'
			assertEquals("b", path);
		}
	}
	
	@Test
	public void testConvertToPathIsConsistentGivenTheRootPathInsertionOrder() throws MalformedURLException {
		//Fixed the order of insertion of the rootPaths the resolution convertToPath() must
		//work always in the same way. Otherwise the content of the rootPath strings will influence
		//the path generation and the identity of the changesets.
		//This is a problem when you deploy on multiple machines with the same file structure but
		//path different (e.g. because every country has a specific folder).
		checkConvertToPathIsConsistentGivenTheRootPathInsertionOrder("file:/th/");
		checkConvertToPathIsConsistentGivenTheRootPathInsertionOrder("file:/sa/");
	}

	@Test
	public void testConvertToPathRelativeDoesntGenerateDoubleSlahes() {
		AbstractResourceAccessor ara = new MyARA();
		URL rootPathURL = ara.toClassLoader().getResource("liquibase/resource/");
		ara.addRootPath(rootPathURL);
		String path = ara.convertToPath("liquibase/resource/empty.txt", "changelogs/");
		//liquibase.resource.AbstractResourceAccessor.convertToPath(String, String) introduces a double slash
		//then in liquibase.resource.AbstractResourceAccessor.convertToPath(String), if it matches the part
		//before the double slash, then an absolute path is generated instead of a relative one (E.g. '/changelogs/'
		//instead of 'changelogs/').
		assertEquals("changelogs/", path);		
	}

	private void checkConvertToPathIsConsistentGivenTheRootPathInsertionOrder(
			String prefix) throws MalformedURLException {
		AbstractResourceAccessor ara = new MyARA();
		ara.addRootPath(new URL(prefix + "logs/"));
		ara.addRootPath(new URL(prefix));
		//System.out.println(ara.getRootPaths());
		String path = ara.convertToPath(prefix + "logs/cs-1.0.xml");
		assertEquals("cs-1.0.xml", path);
	}

	private final static class MyARA extends AbstractResourceAccessor {
		
		@Override
		protected void init() {
			//We don't pollute the tests with external rootPaths
		}
	
		@Override
		public Set<InputStream> getResourcesAsStream(String path)
				throws IOException {
			return null;
		}
	
		@Override
		public Set<String> list(String relativeTo, String path,
				boolean includeFiles, boolean includeDirectories, boolean recursive)
				throws IOException {
			return null;
		}
	
		@Override
		public ClassLoader toClassLoader() {
			return Thread.currentThread().getContextClassLoader();
		}
	
	}
	
}
