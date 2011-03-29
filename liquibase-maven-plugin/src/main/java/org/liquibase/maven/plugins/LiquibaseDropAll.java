package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * Drops all database objects owned by the user. Note that functions, procedures and packages are not dropped.
 * 
 * @author Ferenc Gratzer
 * @description Liquibase DropAll Maven plugin
 * @goal dropAll
 * @since 2.0.2
 */
public class LiquibaseDropAll extends AbstractLiquibaseMojo {

	/**
	 * The schemas to be dropped. Comma separated list.
	 * 
	 * @parameter expression="${liquibase.schemas}"
	 */
	protected String schemas;

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		if (null != schemas) {
			liquibase.dropAll(schemas.split(","));
		} else {
			liquibase.dropAll();
		}
	}

	@Override
	protected void printSettings(String indent) {
		super.printSettings(indent);
		getLog().info(indent + "schemas: " + schemas);
	}
}
