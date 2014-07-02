package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

/**
 * Outputs a comment.
 */
public class CommentStatement extends AbstractStatement {
	private static final String TEXT = "text";

	public CommentStatement(String text) {
		setText(text);
	}

	@Override
	public int hashCode() {
		return getText().hashCode();
	}

	@Override
	public String toString() {
        int MAX_LENGTH = 80;
        String text = getText();
        if (text != null && text.length() >= MAX_LENGTH) {
			return text.substring(0, MAX_LENGTH - 3) + "...";
		}
		return getText();
	}

    /**
     * The text of the comment.
     */
	public String getText() {
		return getAttribute(TEXT, String.class);
	}

    public CommentStatement setText(String text) {
        return (CommentStatement) setAttribute(TEXT, text);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
