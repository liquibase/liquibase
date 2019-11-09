package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

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
		try {
			liquibase.reportStatus(true, new Contexts(contexts), new LabelExpression(labels), new OutputStreamWriter(System.out, LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding()));
		} catch (UnsupportedEncodingException e) {
			throw new UnexpectedLiquibaseException(e);
		}
	}

}
