package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.RawSqlStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A common parent for all SQL related changes regardless of where the sql was sourced from.
 * 
 * Implements the necessary logic to choose how it should be parsed to generate the statements.
 * 
 * @author <a href="mailto:csuml@yahoo.co.uk">Paul Keeble</a>
 *
 */
public abstract class AbstractSQLChange extends AbstractChange {

    private boolean stripComments;
    private boolean splitStatements;
    private String endDelimiter;
    private String sql;

    protected AbstractSQLChange(String tagName, String changeName) {
        super(tagName, changeName);
        stripComments= false;
        splitStatements =true;
        endDelimiter =  ";";
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
       this.sql = sql;
    }

    public String getEndDelimiter() {
        return endDelimiter;
    }

    public void setEndDelimiter(String endDelimiter) {
        this.endDelimiter = endDelimiter;
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(getSql()) == null) {
            throw new InvalidChangeDefinitionException("sql text is required", this);
        }

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
    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {

        List<SqlStatement> returnStatements = new ArrayList<SqlStatement>();

        String processedSQL = isStrippingComments() ? StringUtils.stripComments(getSql()) : getSql();
        processedSQL = processedSQL.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        
        if(isSplittingStatements()) {
            String[] statements = StringUtils.splitSQL(processedSQL);
            for (String statement : statements) {
                returnStatements.add(new RawSqlStatement(statement, getEndDelimiter()));
            }
        } else {
            returnStatements.add(new RawSqlStatement(processedSQL, getEndDelimiter()));
        }
        
        return returnStatements.toArray(new SqlStatement[returnStatements.size()]);
    }
}
