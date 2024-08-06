package liquibase.integration.commandline;

import liquibase.changelog.visitor.AbstractChangeExecListener;
import lombok.Getter;

import java.util.Properties;

@Getter
public class ChangeExecListenerWithProperties extends AbstractChangeExecListener {
    private final Properties properties;

    public ChangeExecListenerWithProperties(Properties properties) {
        this.properties = properties;
    }

}
