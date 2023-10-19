package liquibase.report;

import liquibase.plugin.AbstractPluginFactory;

public class ShowSummaryGeneratorFactory extends AbstractPluginFactory<ShowSummaryGenerator> {

    private ShowSummaryGeneratorFactory() {
    }

    @Override
    protected Class<ShowSummaryGenerator> getPluginClass() {
        return ShowSummaryGenerator.class;
    }

    @Override
    protected int getPriority(ShowSummaryGenerator obj, Object... args) {
        return obj.getPriority();
    }

    public ShowSummaryGenerator getShowSummaryGenerator() {
        return getPlugin();
    }
}
