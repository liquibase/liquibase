package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;

/**
 * <p>Generates the SQL that is required to rollback the database to current state after the next update.</p>
 *
 * @description Liquibase FutureRollbackSQL Maven plugin
 * @goal futureRollbackSQL
 */
public class LiquibaseFutureRollbackSQL extends LiquibaseRollback {

    /**
     * The file to output the Rollback SQL script to, if it exists it will be
     * overwritten.
     *
     * @parameter property="liquibase.outputFile"
     *            default-value=
     *            "${project.build.directory}/liquibase/migrate.sql"
     */
    @PropertyElement
    protected File outputFile;

    @Override
    protected Liquibase createLiquibase(Database db) throws MojoExecutionException {
        return super.createLiquibase(db, outputFile);
    }
    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(
                indent + "outputFile: " + outputFile);
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        liquibase.futureRollbackSQL(new Contexts(contexts), new LabelExpression(getLabelFilter()), outputWriter);
    }

    @Override
    protected void checkRequiredRollbackParameters() throws MojoFailureException {
        //nothing to check with futureRollbackSQL
    }
}
