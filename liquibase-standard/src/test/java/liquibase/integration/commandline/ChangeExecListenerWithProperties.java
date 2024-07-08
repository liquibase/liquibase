package liquibase.integration.commandline;

import liquibase.changelog.visitor.AbstractChangeExecListener;

import java.util.Properties;

public class ChangeExecListenerWithProperties extends AbstractChangeExecListener {
    private final Properties properties;

    public ChangeExecListenerWithProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }
}
