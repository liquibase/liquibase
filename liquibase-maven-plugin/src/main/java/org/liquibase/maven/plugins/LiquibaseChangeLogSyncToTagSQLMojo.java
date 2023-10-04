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
import java.io.IOException;
import java.io.Writer;

/**
 *
 * <p>Generates SQL that marks all unapplied changes up to and including a specified tag
 * as applied.</p>
 * 
 * @author   James Atwill
 * @goal     changelogSyncToTagSQL
 *
 */
public class LiquibaseChangeLogSyncToTagSQLMojo extends
		AbstractLiquibaseChangeLogMojo {

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

	/**
	 * Update to the changeSet with the given tag command.
	 * @parameter property="liquibase.toTag"
	 */
	@PropertyElement
	protected String toTag;

	/** The writer for writing the migration SQL. */
	private Writer outputWriter;

	@Override
	protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
		if (toTag == null || toTag.isEmpty()) {
			throw new MojoFailureException("\nYou must specify a changelog tag.");
		}
	}

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		liquibase.changeLogSync(toTag, new Contexts(contexts), new LabelExpression(getLabelFilter()), outputWriter);
	}

	@Override
	protected void printSettings(String indent) {
		super.printSettings(indent);
		getLog().info(
				indent + "migrationSQLOutputFile: " + migrationSqlOutputFile);

	}

	@Override
	protected Liquibase createLiquibase(Database db)
			throws MojoExecutionException {
		return super.createLiquibase(db, migrationSqlOutputFile);
	}

}
