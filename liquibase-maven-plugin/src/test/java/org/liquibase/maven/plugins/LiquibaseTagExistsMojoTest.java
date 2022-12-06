package org.liquibase.maven.plugins;

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Some basic tests that validate the setting of properties for the
 * LiquibaseTagExistsMojo.
 */
public class LiquibaseTagExistsMojoTest extends AbstractLiquibaseMojoTest {

	// reusing dropAll/plugin_config.xml since the LiguibaseTagExists mojo does
	// not have its own config values
	private static final String CONFIG_FILE = "dropAll/plugin_config.xml";

	private static final Map<String, Object> DEFAULT_PROPERTIES;
	private static final String TEST_TAG_VALUE = "some_tag";

	static {
		DEFAULT_PROPERTIES = new HashMap<String, Object>();
		DEFAULT_PROPERTIES.put("driver", "com.mysql.cj.jdbc.Driver");
		DEFAULT_PROPERTIES.put("url", "jdbc:mysql://localhost/eformat");
		DEFAULT_PROPERTIES.put("username", "root");
		DEFAULT_PROPERTIES.put("password", null);
		DEFAULT_PROPERTIES.put("verbose", true);
		DEFAULT_PROPERTIES.put("tag", TEST_TAG_VALUE);
	}

	public void testTag() throws Exception {
		LiquibaseTagExistsMojo mojo = createLiquibaseTagExistsMojo();
		// Clear out any settings for the property file that may be set
		setVariableValueToObject(mojo, "tag", TEST_TAG_VALUE);

		Map values = getVariablesAndValuesFromObject(mojo);
		checkValues(DEFAULT_PROPERTIES, values);
	}

	public void testNoTag() throws Exception {
		LiquibaseTagExistsMojo mojo = createLiquibaseTagExistsMojo();
		// Clear out any settings for the property file that may be set
		try {
			mojo.checkRequiredParametersAreSpecified();
			fail("There should be no 'tag' property value");
		}
		catch (MojoFailureException mfe) {
		    // Consume for success
		}
	}

	private LiquibaseTagExistsMojo createLiquibaseTagExistsMojo() throws Exception {
		LiquibaseTagExistsMojo mojo = new LiquibaseTagExistsMojo();
		PlexusConfiguration config = loadConfiguration(CONFIG_FILE);
		configureMojo(mojo, config);
		return mojo;
	}
}
