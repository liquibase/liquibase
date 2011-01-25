package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * Removes any Liquibase updater locks from the current database.
 * 
 * @author JAmes Atwill
 * @goal releaseLocks
 */
public class LiquibaseReleaseLocksMojo extends AbstractLiquibaseMojo {

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		liquibase.forceReleaseLocks();
	}

}
