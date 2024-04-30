package liquibase.integration.ant;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DropAllTask extends BaseLiquibaseTask {
    private String schemas;
    private String catalog;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        try {
            if (StringUtil.trimToNull(schemas) != null) {
                List<String> schemaNames = StringUtil.splitAndTrim(this.schemas, ",");
                List<CatalogAndSchema> schemas = new ArrayList<>();
                for (String name : schemaNames) {
                    schemas.add(new CatalogAndSchema(catalog,  name));
                }
                liquibase.dropAll(schemas.toArray(new CatalogAndSchema[0]));
            } else {
                liquibase.dropAll();
            }
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to drop all objects from database: " + e.getMessage(), e);
        }
    }

}
