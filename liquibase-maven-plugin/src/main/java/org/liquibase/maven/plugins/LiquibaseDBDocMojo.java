package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.liquibase.maven.property.PropertyElement;

/**
 * <p>Generates dbDocs against the database.</p>
 *
 * @author Ryan Connolly
 * @goal dbDoc
 */
public class LiquibaseDBDocMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     * @parameter
     *      property="liquibase.outputDirectory"
     *      default-value="${project.build.directory}/liquibase/dbDoc"
     */
    @PropertyElement
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
