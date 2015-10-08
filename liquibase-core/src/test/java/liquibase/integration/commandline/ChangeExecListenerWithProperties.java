package liquibase.integration.commandline;

import java.util.Properties;

import liquibase.changelog.visitor.ChangeExecListenerAdaptor;

public class ChangeExecListenerWithProperties extends ChangeExecListenerAdaptor {
	private final Properties properties;
	
	public ChangeExecListenerWithProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}
}
