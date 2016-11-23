package liquibase.integration.commandline;

import liquibase.changelog.visitor.ChangeExecListenerAdaptor;
import liquibase.database.Database;

public class ChangeExecListenerWithDatabase extends ChangeExecListenerAdaptor {
	private final Database database;
	
	public ChangeExecListenerWithDatabase(Database database) {
		this.database = database;
	}
	
	public Database getDatabase() {
		return database;
	}
}
