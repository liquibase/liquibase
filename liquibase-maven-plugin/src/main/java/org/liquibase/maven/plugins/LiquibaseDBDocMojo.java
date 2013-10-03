package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * Generates dbDocs against the database.
 *
 * @author Ryan Connolly
 * @goal dbDoc
 */
public class LiquibaseDBDocMojo extends AbstractLiquibaseChangeLogMojo {

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
