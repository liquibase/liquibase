package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * <p>Marks all unapplied changes to the database as applied in the change log.</p>
 * 
 * @author James Atwill
 * @goal   changelogSync
 */
public class LiquibaseChangeLogSyncMojo extends AbstractLiquibaseChangeLogMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase)
  			throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
	    	liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(getLabelFilter()));
    }
}
