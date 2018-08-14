package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CommentStatement extends AbstractSqlStatement {
    private final String text;
    private final int MAX_LENGTH = 80;

    public CommentStatement(String text) {
        this.text = text;
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof CommentStatement)) {
            return false;
        }
        return this.toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        if ((text != null) && (text.length() >= MAX_LENGTH)) {
            return text.substring(0, MAX_LENGTH - 3) + "...";
        }
        return getText();
    }

    public String getText() {
        return text;
    }
}
