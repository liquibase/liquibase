package liquibase.changelog.filter;

import liquibase.GlobalConfiguration;
import liquibase.changelog.ChangeSet;
import org.apache.commons.lang3.StringUtils;

public class RunWithChangeSetFilter implements ChangeSetFilter {
    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        String runWith = changeSet.getRunWith();

        boolean strictValue = GlobalConfiguration.STRICT.getCurrentValue();
        if((strictValue && StringUtils.isBlank(runWith))) {
            return new ChangeSetFilterResult(false, "runWith value cannot be empty while on Strict mode", this.getClass(), "runWithOnStrictMode", "runWith");
        }
        return new ChangeSetFilterResult(true, "runWith correctly set", this.getClass(), "validRunWith", "runWith");

    }
}
