// Version:   $Id: $
// Copyright: Copyright(c) 2008 Trace Financial Limited
package org.liquibase.maven.plugins;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic tests verifying the configuration of the {@link LiquibaseRollback} plugin.
 * @author Peter Murray
 */
public class LiquibaseRollbackMojoTest extends AbstractLiquibaseMojoTest {

  private static final String COUNT_CONFIG_FILE = "rollback/plugin_config_count.xml";

  private static final String TAG_CONFIG_FILE = "rollback/plugin_config_tag.xml";

  private static final String DATE_CONFIG_FILE = "rollback/plugin_config_date.xml";

  private static final Map<String, Object> DEFAULT_PROPERTIES;

  private static final Map<String, Object> COUNT_DEFAULT_PROPERTIES;

  private static final Map<String, Object> TAG_DEFAULT_PROPERTIES;

  private static final Map<String, Object> DATE_DEFAULT_PROPERTIES;

  static {
    DEFAULT_PROPERTIES = new HashMap<String, Object>();
    DEFAULT_PROPERTIES.put("changeLogFile", "org/liquibase/changelog.xml");
    DEFAULT_PROPERTIES.put("driver", "com.mysql.cj.jdbc.Driver");
    DEFAULT_PROPERTIES.put("url", "jdbc:mysql://localhost/eformat");
    DEFAULT_PROPERTIES.put("username", "root");
    DEFAULT_PROPERTIES.put("password", null);
    DEFAULT_PROPERTIES.put("verbose", true);

    COUNT_DEFAULT_PROPERTIES = new HashMap<String, Object>();
    COUNT_DEFAULT_PROPERTIES.putAll(DEFAULT_PROPERTIES);
    COUNT_DEFAULT_PROPERTIES.put("rollbackCount", 5);

    TAG_DEFAULT_PROPERTIES = new HashMap<String, Object>();
    TAG_DEFAULT_PROPERTIES.putAll(DEFAULT_PROPERTIES);
    TAG_DEFAULT_PROPERTIES.put("rollbackTag", "tag_to_roll back to");

    DATE_DEFAULT_PROPERTIES = new HashMap<String, Object>();
    DATE_DEFAULT_PROPERTIES.putAll(DEFAULT_PROPERTIES);
    DATE_DEFAULT_PROPERTIES.put("rollbackDate", "12-08-1977");
  }

  public void testRollbackCountNoPropertiesFile() throws Exception {
    LiquibaseRollback mojo = createUpdateMojo(COUNT_CONFIG_FILE);
    // Clear out any settings for the property file that may be set
    setVariableValueToObject(mojo, "propertyFile", null);
    setVariableValueToObject(mojo, "propertyFileWillOverride", false);

    Map values = getVariablesAndValuesFromObject(mojo);
    checkValues(COUNT_DEFAULT_PROPERTIES, values);
  }

  public void testRollbackTagNoPropertiesFile() throws Exception {
    LiquibaseRollback mojo = createUpdateMojo(TAG_CONFIG_FILE);
    // Clear out any settings for the property file that may be set
    setVariableValueToObject(mojo, "propertyFile", null);
    setVariableValueToObject(mojo, "propertyFileWillOverride", false);

    Map values = getVariablesAndValuesFromObject(mojo);
    checkValues(TAG_DEFAULT_PROPERTIES, values);
  }

  public void testRollbackDateNoPropertiesFile() throws Exception {
    LiquibaseRollback mojo = createUpdateMojo(DATE_CONFIG_FILE);
    // Clear out any settings for the property file that may be set
    setVariableValueToObject(mojo, "propertyFile", null);
    setVariableValueToObject(mojo, "propertyFileWillOverride", false);

    Map values = getVariablesAndValuesFromObject(mojo);
    checkValues(DATE_DEFAULT_PROPERTIES, values);
  }

  /*-------------------------------------------------------------------------*\
   * PRIVATE METHODS
  \*-------------------------------------------------------------------------*/

  private LiquibaseRollback createUpdateMojo(String configFileName) throws Exception {
    LiquibaseRollback mojo = new LiquibaseRollback();
    PlexusConfiguration config = loadConfiguration(configFileName);
    configureMojo(mojo, config);
    return mojo;
  }
}
