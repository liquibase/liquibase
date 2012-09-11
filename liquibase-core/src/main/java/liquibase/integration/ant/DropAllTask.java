package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.util.List;

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
    public void execute() throws BuildException {

        super.execute();

        if (!shouldRun()) {
            return;
        }

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase();

            if (StringUtils.trimToNull(schemas) != null) {
                List<String> schemaNames = StringUtils.splitAndTrim(this.schemas, ",");
                List<Schema> schemas = new ArrayList<Schema>();
                for (String name : schemaNames) {
                    schemas.add(new Schema(catalog,  name));
                }
                liquibase.dropAll(schemas.toArray(new Schema[schemas.size()]));
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