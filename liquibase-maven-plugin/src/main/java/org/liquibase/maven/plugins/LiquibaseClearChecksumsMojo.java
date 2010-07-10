package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * A Maven Mojo for performing cleanup of the check sums.
 * @author Nathan Voxland
 * @goal clearCheckSums
 */
public class LiquibaseClearChecksumsMojo extends AbstractLiquibaseMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        liquibase.clearCheckSums();
    }
}