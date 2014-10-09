package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.util.StringUtils;
import org.apache.tools.ant.BuildException;

public class TagDatabaseTask extends BaseLiquibaseTask {
    private String tag;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        try {
            liquibase.tag(tag);
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to tag database.", e);
        }
    }

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(StringUtils.trimToNull(tag) == null) {
            throw new BuildException("Unable to tag database. The tag attribute is required.");
        }
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}