package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * Liquibase dbDoc Maven2 MOJO. This goal allows for dbDocs to be generated as part of a Maven build process.
 *
 * User: rynam0
 * Date: Jul 21, 2009
 * Time: 8:10:44 PM
 *
 * @author Ryan Connolly
 * @version $Id: LiquibaseDBDocMojo.java 1043 2009-08-06 18:36:36Z nvoxland $
 *
 * @goal dbDoc
 */
public class LiquibaseDBDocMojo extends AbstractLiquibaseMojo
{

    /**
     * @parameter
     *      expression="${liquibase.outputDirectory}"
     *      default-value="${project.build.directory}/liquibase/dbDoc"
     */
    private String outputDirectory;


    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException
    {
        liquibase.generateDocumentation(outputDirectory);
    }


    public String getOutputDirectory()
    {
        return outputDirectory;
    }

}
