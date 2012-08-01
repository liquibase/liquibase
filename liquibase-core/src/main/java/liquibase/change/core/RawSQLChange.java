package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractSQLChange;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StringUtils;

/**
 * Allows execution of arbitrary SQL.  This change can be used when existing changes are either don't exist,
 * are not flexible enough, or buggy. 
 */
public class RawSQLChange extends AbstractSQLChange {

    private String comments;
    public RawSQLChange() {
        super("sql", "Custom SQL", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public RawSQLChange(String sql) {
        this();
        setSql(sql);
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getConfirmationMessage() {
        return "Custom SQL executed";
    }

	public SqlStatement[] generateStatements(Database database) {
		List<SqlStatement> returnStatements = new ArrayList<SqlStatement>();

		if (StringUtils.trimToNull(getSql()) == null) {
			return new SqlStatement[0];
		}

		String processedSQL = getSql().replaceAll("\r\n", "\n").replaceAll("\r", "\n");
		for (String statement : StringUtils.processMutliLineSQL(processedSQL, isStrippingComments(),
				isSplittingStatements(), getEndDelimiter())) {
			if (database instanceof MSSQLDatabase) {
				statement = statement.replaceAll("\n", "\r\n");
			}
			
			String escapedStatement;
			try {
				escapedStatement = database.getConnection().nativeSQL(statement);
			} catch (DatabaseException e) {
				escapedStatement = statement;
			}

			returnStatements.add(new RawSqlStatement(escapedStatement, getEndDelimiter()));
		}

		return returnStatements.toArray(new SqlStatement[returnStatements.size()]);
	}
}
