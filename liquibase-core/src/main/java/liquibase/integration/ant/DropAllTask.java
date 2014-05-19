package liquibase.integration.ant;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.util.List;

public class DropAllTask extends BaseLiquibaseTask {
    private String schemas;
    private String catalog;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        try {
            if (StringUtils.trimToNull(schemas) != null) {
                List<String> schemaNames = StringUtils.splitAndTrim(this.schemas, ",");
                List<CatalogAndSchema> schemas = new ArrayList<CatalogAndSchema>();
                for (String name : schemaNames) {
                    schemas.add(new CatalogAndSchema(catalog,  name));
                }
                liquibase.dropAll(schemas.toArray(new CatalogAndSchema[schemas.size()]));
            } else {
                liquibase.dropAll();
            }
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to drop all objects from database.", e);
        }
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchemas() {
        return schemas;
    }

    public void setSchemas(String schemas) {
        this.schemas = schemas;
    }
}