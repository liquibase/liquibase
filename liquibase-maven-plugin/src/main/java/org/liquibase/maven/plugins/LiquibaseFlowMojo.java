package org.liquibase.maven.plugins;

/**
 * Run a series of commands contained in one or more stages, as configured in a liquibase flow-file.
 *
 * @goal flow
 */
public class LiquibaseFlowMojo extends AbstractLiquibaseFlowMojo {

    @Override
    public String[] getCommandName() {
        return new String[]{"flow"};
    }
}
