package org.liquibase.maven.plugins;

import java.io.File;

/**
 * Test class for the Liquibase Update Mojo.
 * <p>
 * Note that we need to run mvn test once to generate the Plugin artifacts
 * before we can run the UnitTest in the IDE.
 * 
 * @author Martin Meyer <martin.meyer@inftec.ch>
 *
 */
public class LiquibaseUpdateMojoExecutionTest extends AbstractLiquibaseMojoTest {
	/**
	 * Test the lookup of relative path names for changeLog 
	 */
	public void testRelativeClobFiles() throws Exception {
	  File pom = getTestFile("src/test/resources/update/relativeClobFiles/plugin_config.xml");

		if (!pom.exists()) {
			return;
		}
	  LiquibaseUpdate update = (LiquibaseUpdate) lookupMojo("update", pom);
	  update.execute();
  }
}
