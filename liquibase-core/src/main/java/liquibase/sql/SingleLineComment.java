package liquibase.sql;

import liquibase.structure.DatabaseObject;

import java.util.Collection;
import java.util.HashSet;

public class SingleLineComment implements Sql {

	final private String sql;
	final private String lineCommentToken;
	
	public SingleLineComment(String sql, String lineCommentToken) {
		this.sql = sql;
		this.lineCommentToken = lineCommentToken;
	}
	
	@Override
    public Collection<? extends DatabaseObject> getAffectedDatabaseObjects() {
		return new HashSet<DatabaseObject>();
	}

	@Override
    public String getEndDelimiter() {
		return "\n";
	}

	@Override
    public String toSql() {
		return lineCommentToken + ' ' + sql;
	}

    @Override
    public String toString() {
        return toSql();
    }
}
