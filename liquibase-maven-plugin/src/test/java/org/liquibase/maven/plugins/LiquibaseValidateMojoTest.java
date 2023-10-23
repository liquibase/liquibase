package org.liquibase.maven.plugins;

import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 *
 * @author Balazs Desi
 */
public class LiquibaseValidateMojoTest extends AbstractLiquibaseMojoTest
{

    private static final String CONFIG_FILE = "validate/plugin_config.xml";


    public void testValidateMojo() throws Exception
    {
        LiquibaseValidate mojo = createValidateMojo(CONFIG_FILE);
        assertNotNull(mojo);
    }


    private LiquibaseValidate createValidateMojo(String configFileName) throws Exception
    {
        LiquibaseValidate mojo = new LiquibaseValidate();
        PlexusConfiguration config = loadConfiguration(configFileName);
        configureMojo(mojo, config);

        return mojo;
    }
}
