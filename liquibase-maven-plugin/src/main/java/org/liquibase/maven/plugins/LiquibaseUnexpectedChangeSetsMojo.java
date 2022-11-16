package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.GlobalConfiguration;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 *
 * <p>Print a list of changesets that have been executed but are not in the current changelog</p>
 * 
 * @author Wesley Willard
 * @goal   unexpectedChangeSets
 *
 */
public class LiquibaseUnexpectedChangeSetsMojo extends AbstractLiquibaseChangeLogMojo {
    @Override
    protected void performLiquibaseTask(Liquibase liquibase)
        throws LiquibaseException {
        try {
            liquibase.reportUnexpectedChangeSets(true, new Contexts(contexts), new LabelExpression((getLabelFilter())), new OutputStreamWriter(System.out, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
        }
        catch (UnsupportedEncodingException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
