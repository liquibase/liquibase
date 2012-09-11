package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.structure.core.Schema;
import liquibase.exception.LiquibaseException;

import java.util.ArrayList;
import java.util.List;

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
    
    protected String catalog;

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		if (schemas != null) {
            List<Schema> schemaObjs = new ArrayList<Schema>();
            for (String name : schemas.split(",")) {
                schemaObjs.add(new Schema(catalog, name));
            }
			liquibase.dropAll(schemaObjs.toArray(new Schema[schemaObjs.size()]));
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
