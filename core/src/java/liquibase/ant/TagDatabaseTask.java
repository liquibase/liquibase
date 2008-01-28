package liquibase.ant;

import liquibase.migrator.Migrator;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;

public class TagDatabaseTask extends BaseLiquibaseTask {

    private String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void execute() throws BuildException {
        if (StringUtils.trimToNull(getTag()) == null) {
            throw new BuildException("tagDatabase requires tag parameter to be set");
        }

        Migrator migrator = null;
        try {
            migrator = createMigrator();
            migrator.tag(getTag());

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(migrator);
        }
    }
}