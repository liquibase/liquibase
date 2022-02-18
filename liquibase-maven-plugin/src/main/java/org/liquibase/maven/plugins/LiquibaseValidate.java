package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Validates liquibase changelog
 *
 * @author Balazs Desi
 * @goal validate
 */
public class LiquibaseValidate extends AbstractLiquibaseChangeLogMojo{

    @Override
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        super.checkRequiredParametersAreSpecified();

    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        liquibase.validate();
    }
}


