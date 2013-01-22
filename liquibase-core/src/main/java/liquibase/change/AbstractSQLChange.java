package liquibase.change;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A common parent for all SQL related changes regardless of where the sql was sourced from.
 * 
 * Implements the necessary logic to choose how it should be parsed to generate the statements.
 *
 */
public abstract class AbstractSQLChange extends AbstractChange {

    private boolean stripComments;
    private boolean splitStatements;
    private String endDelimiter;
    private String sql;

    protected AbstractSQLChange() {
        stripComments= false;
        splitStatements =true;
    }

    @Override
    public boolean supports(Database database) {
        return true;
    }

    /**
     * @param stripComments true if comments should be stripped out, otherwise false
     */
    public void setStripComments(Boolean stripComments) {
        this.stripComments = stripComments;
    }

    /**
     * 
     * @return true if stripping comments, otherwise false
     */
    public boolean isStrippingComments() {
        return stripComments;
    }

    /**
     * If set to true then the sql will be split around any ; and \ngo\n entries in the sql and
     * each line provided as a separate statement.
     * 
     * @param splitStatements set true if the SQL should be split, otherwise false
     */
    public void setSplitStatements(Boolean splitStatements) {
        this.splitStatements = splitStatements;
    }
    
    /**
     * 
     * @return true if a multi-line file will be split, otherwise false
     */
    public boolean isSplittingStatements() {
        return splitStatements;
    }

    public String getSql() {
        return sql;
    }

    /**
     * The raw SQL to use for this change.
     */
    public void setSql(String sql) {
       this.sql = StringUtils.trimToNull(sql);
    }

    public String getEndDelimiter() {
        return endDelimiter;
    }

    public void setEndDelimiter(String endDelimiter) {
        this.endDelimiter = endDelimiter;
    }

    /**
     * Calculates an MD5 from the contents of the file.
     *
     * @see liquibase.change.AbstractChange#generateCheckSum()
     */
    @Override
    public CheckSum generateCheckSum() {
        String sql = getSql();
        if (sql == null) {
            sql = "";
        }
        return CheckSum.compute(this.endDelimiter+":"+
                this.isSplittingStatements()+":"+
                this.isStrippingComments()+":"+
                sql.replaceAll("\r\n", "\n").replaceAll("\r", "\n")); //normalize line endings
    }


    /**
     * Generates one or more statements depending on how the SQL should be parsed.
     * If split statements is set to true then the SQL is split on the ; and go\n entries
     * found in the sql text and each is made a separate statement.
     *
     * If stripping comments is true then any comments after -- on a line and any comments
     * between /* and \*\/ will be stripped before the splitting is executed.
     *
     * The end result is one or more SQL statements split in the way the user requested
     */
    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> returnStatements = new ArrayList<SqlStatement>();

        if (StringUtils.trimToNull(getSql()) == null) {
            return new SqlStatement[0];
        }

        String processedSQL = getSql().replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        for (String statement : StringUtils.processMutliLineSQL(processedSQL, isStrippingComments(), isSplittingStatements(), getEndDelimiter())) {
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
