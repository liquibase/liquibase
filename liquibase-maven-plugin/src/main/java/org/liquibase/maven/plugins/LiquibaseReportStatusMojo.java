package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.GlobalConfiguration;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * <p>Prints which changesets need to be applied to the database.</p>
 * 
 * @author JAmes Atwill
 * @goal status
 */
public class LiquibaseReportStatusMojo extends AbstractLiquibaseChangeLogMojo {

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		try {
			liquibase.reportStatus(true, new Contexts(contexts), new LabelExpression(getLabelFilter()), new OutputStreamWriter(System.out, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
		} catch (UnsupportedEncodingException e) {
			throw new UnexpectedLiquibaseException(e);
		}
	}

}
