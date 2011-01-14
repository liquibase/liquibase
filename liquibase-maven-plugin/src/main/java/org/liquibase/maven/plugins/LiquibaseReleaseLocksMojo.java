package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * A Maven Mojo for releasing all locks.
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
