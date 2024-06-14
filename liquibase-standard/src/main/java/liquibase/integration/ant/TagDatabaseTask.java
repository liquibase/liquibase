package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.tools.ant.BuildException;

@Getter
@Setter
public class TagDatabaseTask extends BaseLiquibaseTask {
    private String tag;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        try {
            liquibase.tag(tag);
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to tag database: " + e.getMessage(), e);
        }
    }

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(StringUtil.trimToNull(tag) == null) {
            throw new BuildException("Unable to tag database. The tag attribute is required.");
        }
    }

}
