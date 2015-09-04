package liquibase.integration.ant;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.util.StringUtils;

public class DropAllTask extends BaseLiquibaseTask {

    private String schemas;
    private String catalog;

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

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {

        if (!shouldRun()) {
            return;
        }

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase();

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

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }
}