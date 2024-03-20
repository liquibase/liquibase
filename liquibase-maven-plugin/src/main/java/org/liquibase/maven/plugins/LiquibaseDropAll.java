package org.liquibase.maven.plugins;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.liquibase.maven.property.PropertyElement;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Drops all database objects in the configured schema(s). Note that functions, procedures and packages are not dropped.</p>
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
     * @parameter property="liquibase.schemas"
     */
    @PropertyElement
    protected String schemas;

    /**
     * If true, the database changelog history table will be dropped. Requires pro license.
     *
     * @parameter property="liquibase.dropDbclhistory"
     */
    @PropertyElement
    protected Boolean dropDbclhistory;

    protected String catalog;

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        if (schemas != null) {
            List<CatalogAndSchema> schemaObjs = new ArrayList<>();
            for (String name : schemas.split(",")) {
                schemaObjs.add(new CatalogAndSchema(catalog, name));
            }
            liquibase.dropAll(dropDbclhistory, schemaObjs.toArray(new CatalogAndSchema[0]));
        } else {
            liquibase.dropAll(dropDbclhistory);
        }
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "schemas: " + schemas);
    }
}
