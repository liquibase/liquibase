package org.liquibase.maven.plugins;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Some basic tests that validate the setting of properties for the
 * LiquibaseDropAll mojo.
 * 
 * @author Ferenc Gratzer
 * @since 2.0.2
 */
public class LiquibaseDropAllMojoTest extends AbstractLiquibaseMojoTest {

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

	public void testNoSchemas() throws Exception {
		LiquibaseDropAll mojo = createDropAllMojo();
		// Clear out any settings for the property file that may be set
		setVariableValueToObject(mojo, "schemas", null);

		loadPropertiesFileIfPresent(mojo);

		Map values = getVariablesAndValuesFromObject(mojo);
		checkValues(DEFAULT_PROPERTIES, values);
	}

	public void testWithSchemas() throws Exception {
		LiquibaseDropAll mojo = createDropAllMojo();
		setVariableValueToObject(mojo, "schemas", "1,2,3");

		loadPropertiesFileIfPresent(mojo);

		Map values = super.getVariablesAndValuesFromObject(mojo);
		checkValues(SCHEMAS_DEFAULT_PROPERTIES, values);
	}

	/*-------------------------------------------------------------------------*\
	 * PRIVATE METHODS
	\*-------------------------------------------------------------------------*/

	private LiquibaseDropAll createDropAllMojo() throws Exception {
		LiquibaseDropAll mojo = new LiquibaseDropAll();
		PlexusConfiguration config = loadConfiguration(CONFIG_FILE);
		configureMojo(mojo, config);
		return mojo;
	}
}
