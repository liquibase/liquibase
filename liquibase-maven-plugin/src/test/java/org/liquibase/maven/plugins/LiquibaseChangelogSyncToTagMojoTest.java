package org.liquibase.maven.plugins;

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Some basic tests that validate the setting of properties for the
 * ChangelogSyncToTag mojo.
 * 
 * @author Ferenc Gratzer
 * @since 2.0.2
 */
public class LiquibaseChangelogSyncToTagMojoTest extends AbstractLiquibaseMojoTest {

	private static final String CONFIG_FILE = "dropAll/plugin_config.xml";

	private static final Map<String, Object> DEFAULT_PROPERTIES;

	private static final Map<String, Object> SCHEMAS_DEFAULT_PROPERTIES;

	static {
		DEFAULT_PROPERTIES = new HashMap<String, Object>();
		DEFAULT_PROPERTIES.put("driver", "com.mysql.cj.jdbc.Driver");
		DEFAULT_PROPERTIES.put("url", "jdbc:mysql://localhost/eformat");
		DEFAULT_PROPERTIES.put("username", "root");
		DEFAULT_PROPERTIES.put("password", null);
		DEFAULT_PROPERTIES.put("verbose", true);

		SCHEMAS_DEFAULT_PROPERTIES = new HashMap<String, Object>();
		SCHEMAS_DEFAULT_PROPERTIES.putAll(DEFAULT_PROPERTIES);
		SCHEMAS_DEFAULT_PROPERTIES.put("schemas", "1,2,3");
	}

	public void testTag() throws Exception {
		LiquibaseChangeLogSyncToTagMojo mojo = createChangelogSyncToTagMojo();
		// Clear out any settings for the property file that may be set
		setVariableValueToObject(mojo, "toTag", "25-Feb-2021");

		Map values = getVariablesAndValuesFromObject(mojo);
		checkValues(DEFAULT_PROPERTIES, values);
	}

	public void testNoTag() throws Exception {
		LiquibaseChangeLogSyncToTagMojo mojo = createChangelogSyncToTagMojo();
		// Clear out any settings for the property file that may be set
		try {
			mojo.checkRequiredParametersAreSpecified();
			fail("There should be no 'toTag' property value");
		}
		catch (MojoFailureException mfe) {
		    // Consume for success
		}
	}

	/*-------------------------------------------------------------------------*\
	 * PRIVATE METHODS
	\*-------------------------------------------------------------------------*/

	private LiquibaseChangeLogSyncToTagMojo createChangelogSyncToTagMojo() throws Exception {
		LiquibaseChangeLogSyncToTagMojo mojo = new LiquibaseChangeLogSyncToTagMojo();
		PlexusConfiguration config = loadConfiguration(CONFIG_FILE);
		configureMojo(mojo, config);
		return mojo;
	}
}
