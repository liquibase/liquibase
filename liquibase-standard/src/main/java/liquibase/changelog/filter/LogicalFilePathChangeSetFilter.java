package liquibase.changelog.filter;

import liquibase.GlobalConfiguration;
import liquibase.changelog.ChangeSet;
import org.apache.commons.lang3.StringUtils;

public class LogicalFilePathChangeSetFilter implements ChangeSetFilter {
    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        String logicalFilePath = changeSet.getLogicalFilePath();

        boolean strictValue = GlobalConfiguration.STRICT.getCurrentValue();
        if((strictValue && StringUtils.isBlank(logicalFilePath))) {
            return new ChangeSetFilterResult(false, "logicalFilePath value cannot be empty while on Strict mode", this.getClass(), "logicalFilePathOnStrictMode", "logicalFilePath");
        }
        return new ChangeSetFilterResult(true, "logicalFilePath correctly set", this.getClass(), "validLogicalFilePath", "logicalFilePath");

    }
}
