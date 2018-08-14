package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * Generates the SQL that is required to update the database to the current
 * version as specified in the DatabaseChangeLogs.
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
	 * @parameter expression="${liquibase.migrationSqlOutputFile}"
	 *            default-value=
	 *            "${project.build.directory}/liquibase/migrate.sql"
	 */
	protected File migrationSqlOutputFile;

	/** The writer for writing the migration SQL. */
	private Writer outputWriter;

	@Override
	protected boolean isPromptOnNonLocalDatabase() {
		// Always run on an non-local database as we are not actually modifying
		// the database
		// when run on it.
		return false;
	}

	@Override
	protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
		if (changesToApply > 0) {
			liquibase.update(changesToApply, new Contexts(contexts), new LabelExpression(labels), outputWriter);
		} else {
			liquibase.update(toTag, new Contexts(contexts), new LabelExpression(labels), outputWriter);
		}
	}

	@Override
	protected Liquibase createLiquibase(ResourceAccessor fo, Database db)
			throws MojoExecutionException {
		Liquibase liquibase = super.createLiquibase(fo, db);

		// Setup the output file writer
		try {
			if (!migrationSqlOutputFile.exists()) {
				// Ensure the parent directories exist
				migrationSqlOutputFile.getParentFile().mkdirs();
				// Create the actual file
				if (!migrationSqlOutputFile.createNewFile()) {
					throw new MojoExecutionException(
							"Cannot create the migration SQL file; "
									+ migrationSqlOutputFile.getAbsolutePath());
				}
			}

            outputWriter = getOutputWriter(migrationSqlOutputFile);
		} catch (IOException e) {
			getLog().error(e);
			throw new MojoExecutionException(
					"Failed to create SQL output writer", e);
		}
		getLog().info(
				"Output SQL Migration File: "
						+ migrationSqlOutputFile.getAbsolutePath());
		return liquibase;
	}

	@Override
	protected void printSettings(String indent) {
		super.printSettings(indent);
		getLog().info(
				indent + "migrationSQLOutputFile: " + migrationSqlOutputFile);
	}

	@Override
	protected void cleanup(Database db) {
		super.cleanup(db);
		if (outputWriter != null) {
			try {
				outputWriter.close();
			} catch (IOException e) {
				getLog().error(e);
			}
		}
	}
}