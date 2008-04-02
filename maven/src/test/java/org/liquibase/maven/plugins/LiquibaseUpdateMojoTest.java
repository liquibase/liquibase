package org.liquibase.maven.plugins;

import java.io.*;
import java.util.*;
import liquibase.FileOpener;
import liquibase.FileSystemFileOpener;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * Some basis tests that validate the setting of properties for the LiquibaseUpdate mojo.
 * @author Peter Murray
 */
public class LiquibaseUpdateMojoTest extends AbstractMojoTestCase {

  private static final String CONFIG_FILE = "update/plugin_config.xml";

  private static final Map<String, Object> DEFAULT_PROPERTIES;

  static {
    DEFAULT_PROPERTIES = new HashMap<String, Object>();
    DEFAULT_PROPERTIES.put("changeLogFile", "org/liquibase/changelog.xml");
    DEFAULT_PROPERTIES.put("driver", "com.mysql.jdbc.Driver");
    DEFAULT_PROPERTIES.put("url", "jdbc:mysql://localhost/eformat");
    DEFAULT_PROPERTIES.put("username", "root");
    DEFAULT_PROPERTIES.put("password", null);
    DEFAULT_PROPERTIES.put("verbose", true);
  }

  public void testNoPropertiesFile() throws Exception {
    LiquibaseUpdate mojo = createUpdateMojo();
    // Clear out any settings for the property file that may be set
    super.setVariableValueToObject(mojo, "propertyFile", null);
    super.setVariableValueToObject(mojo, "propertyFileWillOverride", false);
    
    loadPropertiesFileIfPresent(mojo);

    Map values = super.getVariablesAndValuesFromObject(mojo);
    checkValues(DEFAULT_PROPERTIES, values);
  }

  public void testOverideAllWithPropertiesFile() throws Exception {
    // Create the properties file for this test
    Properties props = new Properties();
    props.setProperty("driver", "properties_driver_value");
    props.setProperty("url", "properties_url_value");
    props.setProperty("username", "properties_user_value");
    props.setProperty("password", "properties_password_value");
    props.setProperty("changeLogFile", "properties_changeLogFile_value");
    createPropertiesFile("update/test.properties", props);

    LiquibaseUpdate mojo = createUpdateMojo();
    super.setVariableValueToObject(mojo, "propertyFile", "update/test.properties");
    super.setVariableValueToObject(mojo, "propertyFileWillOverride", true);
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
    super.setVariableValueToObject(mojo, "propertyFile", "update/test.properties");
    super.setVariableValueToObject(mojo, "propertyFileWillOverride", true);
    loadPropertiesFileIfPresent(mojo);

    Map values = super.getVariablesAndValuesFromObject(mojo);
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
    super.setVariableValueToObject(mojo, "propertyFile", "update/test.properties");
    super.setVariableValueToObject(mojo, "propertyFileWillOverride", false);
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

  private void loadPropertiesFileIfPresent(AbstractLiquibaseMojo mojo)
          throws MojoExecutionException, MojoFailureException {

    File rootDir = new File(getBasedir(), "target/test-classes");
    FileOpener fo = new FileSystemFileOpener(rootDir.getAbsolutePath());
    mojo.configureFieldsAndValues(fo);
  }

  private PlexusConfiguration loadConfiguration(String configFile) throws Exception {
    File testPom = new File(getBasedir(), "target/test-classes/" + configFile);
    assertTrue("The configuration pom could not be found, " + testPom.getAbsolutePath(),
               testPom.exists());

    PlexusConfiguration config = super.extractPluginConfiguration("liquibase-plugin",
                                                                  testPom);
    assertNotNull("There should be a configuration for the plugin in the pom", config);
    return config;
  }

  private void checkValues(Map expected, Map values) {
    for (Object key : expected.keySet()) {
      Object expectedValue = expected.get(key);
      Object actualValue = values.get(key);
      assertEquals("The values do not match for property '" + key + "'",
                   expectedValue,
                   actualValue);
    }
  }

  private void checkValues(Properties expected, Map values) {
    for (Object key : expected.keySet()) {
      Object expectedValue = expected.get(key);
      Object actualValue = values.get(key);
      assertEquals("The values do not match for property '" + key + "'",
                   expectedValue,
                   actualValue);
    }
  }

  private void createPropertiesFile(String filename, Properties p) throws IOException {
    File output = new File(getBasedir(), "/target/test-classes/" + filename);
    if (!output.exists()) {
      System.out.println("Creating file: " + output.getPath());
      if (!output.createNewFile()) {
        throw new IOException("Unable to create the properties file, "
                              + output.getAbsolutePath());
      }
    }

    FileWriter fw = null;
    try {
      fw = new FileWriter(output);
      p.store(fw, "generated by unit test");
    }
    finally {
      if (fw != null) {
        fw.close();
      }
    }
  }

  private void dumpValues(Map values) {
    for (Object key : values.keySet()) {
      System.out.println(key + " :: " + values.get(key));
    }
  }
}
