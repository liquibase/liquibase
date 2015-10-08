package liquibase.integration.commandline;

import java.util.Properties;

import liquibase.changelog.visitor.ChangeExecListenerAdaptor;
import liquibase.database.Database;

public class ChangeExecListenerWithDatabaseAndProperties extends ChangeExecListenerAdaptor {
	private final Database database;
	private final Properties properties;
	
	public ChangeExecListenerWithDatabaseAndProperties(Database database, Properties properties) {
		this.database = database;
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}

	public Database getDatabase() {
		return database;
	}
}
