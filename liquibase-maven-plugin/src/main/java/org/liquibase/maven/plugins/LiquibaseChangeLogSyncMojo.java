package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * Marks all unapplied changes to the database as applied in the change log.
 * 
 * @author JAmes Atwill
 * @goal changelogSync
 */
public class LiquibaseChangeLogSyncMojo extends AbstractLiquibaseChangeLogMojo {

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(labels));
	}

}
