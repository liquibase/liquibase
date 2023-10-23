package org.liquibase.maven.plugins;

/**
 * Validate a series of commands contained in one or more stages, as configured in a liquibase flow-file.
 *
 * @goal flow.validate
 */
public class LiquibaseFlowValidateMojo extends AbstractLiquibaseFlowMojo{
    @Override
    public String[] getCommandName() {
        return new String[]{"flow", "validate"};
    }
}
