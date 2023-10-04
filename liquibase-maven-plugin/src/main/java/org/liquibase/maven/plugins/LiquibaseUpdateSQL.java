package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * <p>Generates the SQL that is required to update the database to the current
 * version as specified in the DatabaseChangeLogs.</p>
 * 
 * @author Peter Murray
 * @description Liquibase UpdateSQL Maven plugin
 * @goal updateSQL
 */
public class LiquibaseUpdateSQL extends AbstractLiquibaseUpdateMojo {

	/**
	 * The file to output the Migration SQL script to, if it exists it will be
	 * overwritten.
	 * 
	 * @parameter property="liquibase.migrationSqlOutputFile"
	 *            default-value=
	 *            "${project.build.directory}/liquibase/migrate.sql"
	 */
	@PropertyElement
	protected File migrationSqlOutputFile;

	/** The writer for writing the migration SQL. */
	private Writer outputWriter;

	@Override
	protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
		if (changesToApply > 0) {
			liquibase.update(changesToApply, new Contexts(contexts), new LabelExpression(getLabelFilter()), outputWriter);
		} else {
			liquibase.update(toTag, new Contexts(contexts), new LabelExpression(getLabelFilter()), outputWriter);
		}
	}

	@Override
	@java.lang.SuppressWarnings("squid:S2095")
	protected Liquibase createLiquibase(Database db)
			throws MojoExecutionException {
		return super.createLiquibase(db, migrationSqlOutputFile);
	}

	@Override
	protected void printSettings(String indent) {
		super.printSettings(indent);
		getLog().info(
				indent + "migrationSQLOutputFile: " + migrationSqlOutputFile);
	}
}
