package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;

import java.util.List;

public class DropAllTask extends BaseLiquibaseTask {

    private String schemas;

    public String getSchemas() {
        return schemas;
    }

    public void setSchemas(String schemas) {
        this.schemas = schemas;
    }

    @Override
    public void execute() throws BuildException {

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase();

            if (StringUtils.trimToNull(schemas) != null) {
                List<String> schemas = StringUtils.splitAndTrim(this.schemas, ",");
                liquibase.dropAll(schemas.toArray(new String[schemas.size()]));
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