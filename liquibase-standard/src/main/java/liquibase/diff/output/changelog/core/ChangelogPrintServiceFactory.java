package liquibase.diff.output.changelog.core;


import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.plugin.AbstractPluginFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangelogPrintServiceFactory extends AbstractPluginFactory<ChangelogPrintService> {
    @Override
    protected Class<ChangelogPrintService> getPluginClass() {
        return ChangelogPrintService.class;
    }

    @Override
    protected int getPriority(ChangelogPrintService obj, Object... args) {
        return obj.getPriority();
    }

    public ChangelogPrintService getChangeLogPrintService(DiffToChangeLog diffToChangeLog) {
        return getPlugin().setDiffChangelog(diffToChangeLog);
    }
}
