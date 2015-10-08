package liquibase.integration.commandline;

import java.util.Properties;

import liquibase.changelog.visitor.AbstractChangeExecListener;

public class ChangeExecListenerWithProperties extends AbstractChangeExecListener {
	private final Properties properties;
	
	public ChangeExecListenerWithProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}
}
