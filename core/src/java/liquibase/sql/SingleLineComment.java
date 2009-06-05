package liquibase.sql;

import java.util.Collection;
import java.util.HashSet;

import liquibase.database.structure.DatabaseObject;

public class SingleLineComment implements Sql {

	final private String sql;
	final private String lineCommentToken;
	
	public SingleLineComment(String sql, String lineCommentToken) {
		this.sql = sql;
		this.lineCommentToken = lineCommentToken;
	}
	
	public Collection<? extends DatabaseObject> getAffectedDatabaseObjects() {
		return new HashSet<DatabaseObject>();
	}

	public String getEndDelimiter() {
		return "\n";
	}

	public String toSql() {
		return lineCommentToken + ' ' + sql;
	}

}
