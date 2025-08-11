package liquibase.io;

import liquibase.plugin.AbstractPluginFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OutputFileHandlerFactory extends AbstractPluginFactory<OutputFileHandler> {
    @Override
    protected Class<OutputFileHandler> getPluginClass() {
        return OutputFileHandler.class;
    }

    @Override
    protected int getPriority(OutputFileHandler obj, Object... args) {
        return obj.getPriority();
    }

    public OutputFileHandler getOutputFileHandler(String outputFile) {
        return getPlugin(outputFile);
    }
}
