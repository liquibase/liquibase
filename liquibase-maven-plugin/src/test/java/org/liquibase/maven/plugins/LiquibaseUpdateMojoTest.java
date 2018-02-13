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
  private static final String DIRECTORY_CONFIG_FILE = "update/plugin_config_directory.xml";

  private static final Map<String, Object> DEFAULT_PROPERTIES;
  private static final Map<String, Object> DIRECTORY_PROPERTIES;

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

    DIRECTORY_PROPERTIES = new HashMap<String, Object>();
    DIRECTORY_PROPERTIES.put("changeLogDirectory", "org/liquibase/");
    DIRECTORY_PROPERTIES.put("changeLogFile", "changelog.xml");
    DIRECTORY_PROPERTIES.put("driver", "com.mysql.jdbc.Driver");
    DIRECTORY_PROPERTIES.put("url", "jdbc:mysql://localhost/eformat");
    DIRECTORY_PROPERTIES.put("username", "root");
    DIRECTORY_PROPERTIES.put("password", null);
    DIRECTORY_PROPERTIES.put("verbose", true);
    DIRECTORY_PROPERTIES.put("outputDefaultSchema", false);
    DIRECTORY_PROPERTIES.put("outputDefaultCatalog", false);
    DIRECTORY_PROPERTIES.put("outputFileEncoding", "UTF-8");
  }

  public void testNoPropertiesFile() throws Exception {
    testCommonNoPropertiesFile(CONFIG_FILE, DEFAULT_PROPERTIES);
  }

  public void testDirectoryNoPropertiesFile() throws Exception {
    testCommonNoPropertiesFile(DIRECTORY_CONFIG_FILE, DIRECTORY_PROPERTIES);
  }

  private void testCommonNoPropertiesFile(String configFileName, Map<String, Object> properties) throws Exception {
    LiquibaseUpdate mojo = createUpdateMojo(configFileName);
    // Clear out any settings for the property file that may be set
    setVariableValueToObject(mojo, "propertyFile", null);
    setVariableValueToObject(mojo, "propertyFileWillOverride", false);
    
    loadPropertiesFileIfPresent(mojo);

    Map values = getVariablesAndValuesFromObject(mojo);
    checkValues(properties, values);
  }


  public void testOverrideAllWithPropertiesFile() throws Exception {
    testCommonOverideAllWithPropertiesFile(CONFIG_FILE);
  }

  public void testDirectoryOverideAllWithPropertiesFile() throws Exception {
    testCommonOverideAllWithPropertiesFile(DIRECTORY_CONFIG_FILE);
  }

  private void testCommonOverideAllWithPropertiesFile(String configFileName) throws Exception {
    // Create the properties file for this test
    Properties props = new Properties();
    props.setProperty("driver", "properties_driver_value");
    props.setProperty("url", "properties_url_value");
    props.setProperty("username", "properties_user_value");
    props.setProperty("password", "properties_password_value");
    props.setProperty("changeLogFile", "properties_changeLogFile_value");
    createPropertiesFile("update/test.properties", props);

    LiquibaseUpdate mojo = createUpdateMojo(configFileName);
    setVariableValueToObject(mojo, "propertyFile", "update/test.properties");
    setVariableValueToObject(mojo, "propertyFileWillOverride", true);
    loadPropertiesFileIfPresent(mojo);

    Map values = super.getVariablesAndValuesFromObject(mojo);
    checkValues(props, values);
  }

  public void testOverrideAllButDriverWithPropertiesFile() throws Exception {
    testCommonOverrideAllButDriverWithPropertiesFile(CONFIG_FILE, DEFAULT_PROPERTIES);
  }

  public void testDirectoryOverrideAllButDriverWithPropertiesFile() throws Exception {
    testCommonOverrideAllButDriverWithPropertiesFile(DIRECTORY_CONFIG_FILE, DIRECTORY_PROPERTIES);
  }

  public void testCommonOverrideAllButDriverWithPropertiesFile(String configFileName, Map<String, Object> properties) throws Exception {
    // Create the properties file for this test
    Properties props = new Properties();
    props.setProperty("url", "properties_url_value");
    props.setProperty("username", "properties_user_value");
    props.setProperty("password", "properties_password_value");
    props.setProperty("changeLogDirectory", "properties_changeLogDirectory_value");
    props.setProperty("changeLogFile", "properties_changeLogFile_value");
    createPropertiesFile("update/test.properties", props);

    LiquibaseUpdate mojo = createUpdateMojo(configFileName);
    setVariableValueToObject(mojo, "propertyFile", "update/test.properties");
    setVariableValueToObject(mojo, "propertyFileWillOverride", true);
    loadPropertiesFileIfPresent(mojo);

    Map values = getVariablesAndValuesFromObject(mojo);
    checkValues(props, values);

    // Ensure that the properties file has not overridden the driver value as it was not
    // specified in the properties.
    assertEquals("Driver should be set to the default value",
        properties.get("driver"),
                 values.get("driver"));
  }

  public void testPropertiesFilePresentWithNoOverrideAndMissingProperty() throws Exception {
    testCommonPropertiesFilePresentWithNoOverrideAndMissingProperty(CONFIG_FILE, DEFAULT_PROPERTIES);
  }

  public void testDirectoryPropertiesFilePresentWithNoOverrideAndMissingProperty() throws Exception {
    testCommonPropertiesFilePresentWithNoOverrideAndMissingProperty(DIRECTORY_CONFIG_FILE, DIRECTORY_PROPERTIES);
  }

  public void testCommonPropertiesFilePresentWithNoOverrideAndMissingProperty(String configFileName, Map<String, Object> properties) throws Exception {
    // Create the properties file for this test
    Properties props = new Properties();
    props.setProperty("url", "properties_url_value");
    props.setProperty("username", "properties_user_value");
    props.setProperty("password", "properties_password_value");
    props.setProperty("changeLogDirectory", "properties_changeLogDirectory_value");
    props.setProperty("changeLogFile", "properties_changeLogFile_value");
    createPropertiesFile("update/test.properties", props);

    LiquibaseUpdate mojo = createUpdateMojo(configFileName);
    setVariableValueToObject(mojo, "propertyFile", "update/test.properties");
    setVariableValueToObject(mojo, "propertyFileWillOverride", false);
    loadPropertiesFileIfPresent(mojo);

    Map values = super.getVariablesAndValuesFromObject(mojo);
    // The password is not specified in the configuration XML so we expect the password
    // from the properties file to be injected into the mojo.
    Map expected = new HashMap<String, Object>(properties);
    expected.put("password", props.getProperty("password"));
    checkValues(expected, values);
  }


  /*-------------------------------------------------------------------------*\
   * PRIVATE METHODS
  \*-------------------------------------------------------------------------*/

  private LiquibaseUpdate createUpdateMojo(String configFileName) throws Exception {
    LiquibaseUpdate mojo = new LiquibaseUpdate();
    PlexusConfiguration config = loadConfiguration(configFileName);
    configureMojo(mojo, config);
    return mojo;
  }
}
