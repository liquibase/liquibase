package liquibase.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ClassLoaderResourceAccessorJavaTest {

	/* @formatter:off
	 * test1.jar
	 * db.changelog-master.json
	 * res
	 *  ├─ db1/db.changelog-v1.json
	 *  ├─ db2/db.changelog-v2.json
	 * @formatter:on
	 */
	@Test
	public void testJarPath() throws Exception {
		final ClassLoaderResourceAccessor clra = new MyCLRA("test1.jar");
		final URL rootPathURL = clra.toClassLoader().getResource("liquibase/resource/classloader/test1.jar");
		clra.addRootPath(rootPathURL);

		final Set<String> ress = clra.list("db.changelog-master.json", "res", true, true, true);

		assertNotNull(ress);

		// results
		final Set<String> results = new HashSet<>();
		results.add("res/");
		results.add("res/db1/");
		results.add("res/db1/db.changelog-v1.json");
		results.add("res/db2/");
		results.add("res/db2/db.changelog-v2.json");
		ress.removeAll(results);

		// the last entry should be the jar
		assertEquals(1, ress.size());
		assertTrue(ress.iterator().next().startsWith("jar:file:"));
	}

	/* @formatter:off
	 * test2.jar
	 * db
	 *  ├─ db.changelog-master.json
	 * res
	 *  ├─ db1/db.changelog-v1.json
	 *  ├─ db2/db.changelog-v2.json
	 * @formatter:on
	 */
	@Test
	public void testJarPath2() throws Exception {
		final ClassLoaderResourceAccessor clra = new MyCLRA("test2.jar");
		final URL rootPathURL = clra.toClassLoader().getResource("liquibase/resource/classloader/test2.jar");
		clra.addRootPath(rootPathURL);
		final Set<String> ress = clra.list("db/db.changelog-master.json", "../res", true, true, true);

		assertNotNull(ress);

		// results
		final Set<String> results = new HashSet<>();
		results.add("res/");
		results.add("res/db1/");
		results.add("res/db1/db.changelog-v1.json");
		results.add("res/db2/");
		results.add("res/db2/db.changelog-v2.json");
		ress.removeAll(results);

		// the last entry should be the jar
		assertEquals(1, ress.size());
		assertTrue(ress.iterator().next().startsWith("jar:file:"));
	}

	/* @formatter:off
	 * test3.jar
	 * changelog
	 *   ├─ db.changelog-master.json
	 *   ├─ db1/db.changelog-v1.json
	 *   ├─ db2/db.changelog-v2.json
	 * @formatter:on
	 */
	@Test
	public void testJarPath3() throws Exception {
		final ClassLoaderResourceAccessor clra = new MyCLRA("test3.jar");
		final URL rootPathURL = clra.toClassLoader().getResource("liquibase/resource/classloader/test3.jar");
		clra.addRootPath(rootPathURL);
		final Set<String> ress = clra.list("changelog/db.changelog-master.json", "", true, true, true);

		assertNotNull(ress);

		// results
		final Set<String> results = new HashSet<>();
		results.add("changelog/");
		results.add("changelog/db.changelog-master.json");
		results.add("changelog/db1/");
		results.add("changelog/db1/db.changelog-v1.json");
		results.add("changelog/db2/");
		results.add("changelog/db2/db.changelog-v2.json");
		ress.removeAll(results);

		// the last entry should be the jar
		assertEquals(1, ress.size());
		assertTrue(ress.iterator().next().startsWith("jar:file:"));

	}

	private static final class MyCLRA extends ClassLoaderResourceAccessor {

		public MyCLRA(final String jarName) throws Exception {
			super(new URLClassLoader(new URL[] {
				new File("src/test/resources/liquibase/resource/classloader/" + jarName).toURI().toURL() //
			}));

		}

	}

}
