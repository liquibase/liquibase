package org.liquibase.maven.plugins;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Some basic tests that validate the setting of properties for the LiquibaseUpdate mojo.
 * @author Peter Murray
 */
public class LiquibaseUpdateMojoTest extends AbstractLiquibaseMojoTest {

  private static final String CONFIG_FILE = "update/plugin_config.xml";

  private static final Map<String, Object> DEFAULT_PROPERTIES;

  static {
    DEFAULT_PROPERTIES = new HashMap<String, Object>();
    DEFAULT_PROPERTIES.put("changeLogFile", "org/liquibase/changelog.xml");
    DEFAULT_PROPERTIES.put("driver", "com.mysql.cj.jdbc.Driver");
    DEFAULT_PROPERTIES.put("url", "jdbc:mysql://localhost/eformat");
    DEFAULT_PROPERTIES.put("username", "root");
    DEFAULT_PROPERTIES.put("password", null);
    DEFAULT_PROPERTIES.put("verbose", true);
    DEFAULT_PROPERTIES.put("outputDefaultSchema", false);
    DEFAULT_PROPERTIES.put("outputDefaultCatalog", false);
    DEFAULT_PROPERTIES.put("outputFileEncoding", "UTF-8");
  }

  public void testNoPropertiesFile() throws Exception {
    LiquibaseUpdate mojo = createUpdateMojo();
    // Clear out any settings for the property file that may be set
    setVariableValueToObject(mojo, "propertyFile", null);
    setVariableValueToObject(mojo, "propertyFileWillOverride", false);
    
    loadPropertiesFileIfPresent(mojo);

    Map values = getVariablesAndValuesFromObject(mojo);
    checkValues(DEFAULT_PROPERTIES, values);
  }

  public void testOverrideAllWithPropertiesFile() throws Exception {
    // Create the properties file for this test
    Properties props = new Properties();
    props.setProperty("driver", "properties_driver_value");
    props.setProperty("url", "properties_url_value");
    props.setProperty("username", "properties_user_value");
    props.setProperty("password", "properties_password_value");
    props.setProperty("changeLogFile", "properties_changeLogFile_value");
    createPropertiesFile("update/test.properties", props);

    LiquibaseUpdate mojo = createUpdateMojo();
    setVariableValueToObject(mojo, "propertyFile", "update/test.properties");
    setVariableValueToObject(mojo, "propertyFileWillOverride", true);
    loadPropertiesFileIfPresent(mojo);

    Map values = super.getVariablesAndValuesFromObject(mojo);
    checkValues(props, values);
  }

  public void testOverrideAllButDriverWithPropertiesFile() throws Exception {
    // Create the properties file for this test
    Properties props = new Properties();
    props.setProperty("url", "properties_url_value");
    props.setProperty("username", "properties_user_value");
    props.setProperty("password", "properties_password_value");
    props.setProperty("changeLogFile", "properties_changeLogFile_value");
    createPropertiesFile("update/test.properties", props);

    LiquibaseUpdate mojo = createUpdateMojo();
    setVariableValueToObject(mojo, "propertyFile", "update/test.properties");
    setVariableValueToObject(mojo, "propertyFileWillOverride", true);
    loadPropertiesFileIfPresent(mojo);

    Map values = getVariablesAndValuesFromObject(mojo);
    checkValues(props, values);

    // Ensure that the properties file has not overridden the driver value as it was not
    // specified in the properties.
    assertEquals("Driver should be set to the default value",
                 DEFAULT_PROPERTIES.get("driver"),
                 values.get("driver"));
  }

  public void testPropertiesFilePresentWithNoOverrideAndMissingProperty() throws Exception {
    // Create the properties file for this test
    Properties props = new Properties();
    props.setProperty("url", "properties_url_value");
    props.setProperty("username", "properties_user_value");
    props.setProperty("password", "properties_password_value");
    props.setProperty("changeLogFile", "properties_changeLogFile_value");
    createPropertiesFile("update/test.properties", props);

    LiquibaseUpdate mojo = createUpdateMojo();
    setVariableValueToObject(mojo, "propertyFile", "update/test.properties");
    setVariableValueToObject(mojo, "propertyFileWillOverride", false);
    loadPropertiesFileIfPresent(mojo);

    Map values = super.getVariablesAndValuesFromObject(mojo);
    // The password is not specified in the configuration XML so we expect the password
    // from the properties file to be injected into the mojo.
    Map expected = new HashMap<String, Object>(DEFAULT_PROPERTIES);
    expected.put("password", props.getProperty("password"));
    checkValues(expected, values);
  }


  /*-------------------------------------------------------------------------*\
   * PRIVATE METHODS
  \*-------------------------------------------------------------------------*/

  private LiquibaseUpdate createUpdateMojo() throws Exception {
    LiquibaseUpdate mojo = new LiquibaseUpdate();
    PlexusConfiguration config = loadConfiguration(CONFIG_FILE);
    configureMojo(mojo, config);
    return mojo;
  }
}
