package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;
import java.text.ParseException;

/**
 * <p>Generates the SQL that is required to rollback the database using one or more of the specified
 * attributes 'rollbackCount', 'rollbackTag' and/or 'rollbackDate'</p>
 *
 * @description Liquibase RollbackSQL Maven plugin
 * @goal rollbackSQL
 */
public class LiquibaseRollbackSQL extends LiquibaseRollback {

    /**
     * The file to output the Rollback SQL script to, if it exists it will be
     * overwritten.
     *
     * @parameter property="liquibase.migrationSqlOutputFile"
     *            default-value=
     *            "${project.build.directory}/liquibase/migrate.sql"
     */
    @PropertyElement
    protected File migrationSqlOutputFile;

    @Override
    protected Liquibase createLiquibase(Database db) throws MojoExecutionException {
       return super.createLiquibase(db, migrationSqlOutputFile);
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(
                indent + "migrationSQLOutputFile: " + migrationSqlOutputFile);
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase)
            throws LiquibaseException {
        switch (type) {
        case COUNT: {
            liquibase.rollback(rollbackCount, rollbackScript,new Contexts(contexts), new LabelExpression(getLabelFilter()), outputWriter);
            break;
        }
        case DATE: {
            try {
                liquibase.rollback(parseDate(rollbackDate), rollbackScript,new Contexts(contexts), new LabelExpression(getLabelFilter()),
                        outputWriter);
            } catch (ParseException e) {
                String message = "Error parsing rollbackDate: "
                        + e.getMessage();
                throw new LiquibaseException(message, e);
            }
            break;
        }
        case TAG: {
            liquibase.rollback(rollbackTag, rollbackScript,new Contexts(contexts), new LabelExpression(getLabelFilter()), outputWriter);
            break;
        }
        default: {
            throw new IllegalStateException("Unexpected rollback type, " + type);
        }
        }
    }
}
