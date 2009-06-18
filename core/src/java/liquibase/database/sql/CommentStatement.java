package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class CommentStatement implements SqlStatement {
	final private String text;
	final private int MAX_LENGTH = 80;
	
	public CommentStatement(String text) {
		this.text = text;
	}

	@Override
	public int hashCode() {
		return text.hashCode();
	}

	@Override
	public String toString() {
		if (text != null && text.length() >= MAX_LENGTH) {
			return text.substring(0, MAX_LENGTH - 3) + "..."; 
		}
		return getText();
	}

	public String getText() {
		return text;
	}

	public String getEndDelimiter(Database database) {
		return "\n";
	}

	public String getSqlStatement(Database database)
			throws StatementNotSupportedOnDatabaseException {
		return database.getLineComment() + ' ' + getText();
	}

	public boolean supportsDatabase(Database database) {
		return true;
	}
}
