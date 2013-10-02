package org.liquibase.maven.plugins;

import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * User: rynam0
 * Date: Jul 21, 2009
 * Time: 9:21:15 PM
 *
 * @author rynam0
 * @version $Id: LiquibaseDBDocMojoTest.java 1043 2009-08-06 18:36:36Z nvoxland $
 */
public class LiquibaseDBDocMojoTest extends AbstractLiquibaseMojoTest
{

    private static final String CONFIG_FILE = "dbDoc/plugin_config.xml";


    public void testDBDocMojo() throws Exception
    {
        LiquibaseDBDocMojo mojo = createDBDocMojo(CONFIG_FILE);
        assertEquals("target/liquibase/myOutputDirectory", mojo.getOutputDirectory());
    }


    private LiquibaseDBDocMojo createDBDocMojo(String configFileName) throws Exception
    {
        LiquibaseDBDocMojo mojo = new LiquibaseDBDocMojo();
        PlexusConfiguration config = loadConfiguration(configFileName);
        configureMojo(mojo, config);

        return mojo;
    }
}
