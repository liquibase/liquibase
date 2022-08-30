package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * <p>Lists all Liquibase updater locks on the current database.</p>
 * 
 * @author JAmes Atwill
 * @goal listLocks
 */
public class LiquibaseListLocksMojo extends AbstractLiquibaseMojo {

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		liquibase.reportLocks(System.out);
	}

}
