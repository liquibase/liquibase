package org.liquibase.maven.plugins;

import java.io.OutputStreamWriter;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * Prints which changesets need to be applied to the database.
 * 
 * @author JAmes Atwill
 * @goal status
 */
public class LiquibaseReportStatusMojo extends AbstractLiquibaseChangeLogMojo {

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		liquibase.reportStatus(true, contexts, new OutputStreamWriter(
				System.out));
	}

}
