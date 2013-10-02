package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CommentStatement extends AbstractSqlStatement {
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
}
