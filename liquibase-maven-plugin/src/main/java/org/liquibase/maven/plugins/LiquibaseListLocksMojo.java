package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * A Maven Mojo for listing all locks.
 * @author JAmes Atwill
 * @goal listLocks
 */
public class LiquibaseListLocksMojo extends AbstractLiquibaseMojo {

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		liquibase.listLocks();
	}

}
