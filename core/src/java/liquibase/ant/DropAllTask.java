package liquibase.ant;

import liquibase.migrator.Migrator;
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

    public void execute() throws BuildException {

        Migrator migrator = null;
        try {
            migrator = createMigrator();

            if (StringUtils.trimToNull(schemas) != null) {
                List<String> schemas = StringUtils.splitAndTrim(this.schemas, ",");
                migrator.dropAll(schemas.toArray(new String[schemas.size()]));
            } else {
                migrator.dropAll();
            }

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(migrator);
        }
    }
}