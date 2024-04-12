package liquibase.io;

import liquibase.plugin.AbstractPluginFactory;

public class OutputFileHandlerFactory extends AbstractPluginFactory<OutputFileHandler> {
    @Override
    protected Class<OutputFileHandler> getPluginClass() {
        return OutputFileHandler.class;
    }

    @Override
    protected int getPriority(OutputFileHandler obj, Object... args) {
        return obj.getPriority((String) args[0]);
    }

    public OutputFileHandler getOutputFileHandler(String outputFile) {
        return getPlugin(outputFile);
    }
}
