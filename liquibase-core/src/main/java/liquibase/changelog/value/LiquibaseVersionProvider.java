package liquibase.changelog.value;

import liquibase.database.Database;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.util.LiquibaseUtil;

public class LiquibaseVersionProvider implements ChangeLogColumnValueProvider {
    @Override
    public Object getValue(MarkChangeSetRanStatement statement, Database database) {
       return LiquibaseUtil.getBuildVersion()
               .replaceAll("SNAPSHOT", "SNP");
    }
}
