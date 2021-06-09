package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * produces a list of changesets that were run in the database but do not exist in the current changelog.
 * @goal unexpectedChangesets
 */
public class LiquibaseUnexpectedChangeSetsMojo extends AbstractLiquibaseChangeLogMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
        liquibase.listUnexpectedChangeSets(new Contexts(contexts), new LabelExpression(labels));
    }
}
