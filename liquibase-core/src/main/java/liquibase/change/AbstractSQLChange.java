package liquibase.change;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A common parent for all raw SQL related changes regardless of where the sql was sourced from.
 * 
 * Implements the necessary logic to choose how the SQL string should be parsed to generate the statements.
 *
 */
public abstract class AbstractSQLChange extends AbstractChange implements DbmsTargetedChange {

    private boolean stripComments;
    private boolean splitStatements;
    private String endDelimiter;
    private String sql;
    private String dbms;

    protected InputStream sqlStream;

    protected String encoding = null;


    protected AbstractSQLChange() {
        setStripComments(null);
        setSplitStatements(null);
    }

    @Override
    @DatabaseChangeProperty(since = "3.0", exampleValue = "h2, oracle")
    public String getDbms() {
        return dbms;
    }

    @Override
    public void setDbms(final String dbms) {
        this.dbms = dbms;
    }

    /**
     * {@inheritDoc}
     * @param database
     * @return
     */
    @Override
    public boolean supports(Database database) {
        return true;
    }

    /**
     * Return if comments should be stripped from the SQL before passing it to the database.
     * <p></p>
     * This will always return a non-null value and should be a boolean rather than a Boolean, but that breaks the Bean Standard.
     */
    @DatabaseChangeProperty(description = "Set to true to remove any comments in the SQL before executing, otherwise false. Defaults to false if not set")
    public Boolean isStripComments() {
        return stripComments;
    }


    /**
     * Return true if comments should be stripped from the SQL before passing it to the database.
     * Passing null sets stripComments to the default value (false).
     */
    public void setStripComments(Boolean stripComments) {
        if (stripComments == null) {
            this.stripComments = false;
        } else {
            this.stripComments = stripComments;
        }
    }

    /**
     * Return if the SQL should be split into multiple statements before passing it to the database.
     * By default, statements are split around ";" and "go" delimiters.
     * <p></p>
     * This will always return a non-null value and should be a boolean rather than a Boolean, but that breaks the Bean Standard.
     */
    @DatabaseChangeProperty(description = "Set to false to not have liquibase split statements on ;'s and GO's. Defaults to true if not set")
    public Boolean isSplitStatements() {
        return splitStatements;
    }

    /**
     * Set whether SQL should be split into multiple statements.
     * Passing null sets stripComments to the default value (true).
     */
    public void setSplitStatements(Boolean splitStatements) {
        if (splitStatements == null) {
            this.splitStatements = true;
        } else {
            this.splitStatements = splitStatements;
        }
    }

    /**
     * Return the raw SQL managed by this Change
     */
    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE)
    public String getSql() {
        return sql;
    }

    /**
     * Set the raw SQL managed by this Change. The passed sql is trimmed and set to null if an empty string is passed.
     */
    public void setSql(String sql) {
       this.sql = StringUtils.trimToNull(sql);
    }

    /**
     * Set the end delimiter used to split statements. Will return null if the default delimiter should be used.
     *
     * @see #splitStatements
     */
    @DatabaseChangeProperty(description = "Delimiter to apply to the end of the statement. Defaults to ';', may be set to ''.", exampleValue = "\\nGO")
    public String getEndDelimiter() {
        return endDelimiter;
    }

    /**
     * Set the end delimiter for splitting SQL statements. Set to null to use the default delimiter.
     * @param endDelimiter
     */
    public void setEndDelimiter(String endDelimiter) {
        this.endDelimiter = endDelimiter;
    }

    /**
     * Calculates the checksum based on the contained SQL.
     *
     * @see liquibase.change.AbstractChange#generateCheckSum()
     */
    @Override
    public CheckSum generateCheckSum() {
        InputStream stream = this.sqlStream;
        if (stream == null && sql != null) {
            stream = new ByteArrayInputStream(sql.getBytes());
        }

        return CheckSum.compute(this.getEndDelimiter()+":"+
                this.isSplitStatements()+":"+
                this.isStripComments()+":"+
                prepareSqlForChecksum(stream)); //normalize line endings
    }


    /**
     * Generates one or more SqlStatements depending on how the SQL should be parsed.
     * If split statements is set to true then the SQL is split and each command is made into a separate SqlStatement.
     * <p></p>
     * If stripping comments is true then any comments are removed before the splitting is executed.
     * The set SQL is passed through the {@link java.sql.Connection#nativeSQL} method if a connection is available.
     */
    @Override
    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> returnStatements = new ArrayList<SqlStatement>();

        if (StringUtils.trimToNull(getSql()) == null) {
            return new SqlStatement[0];
        }

        String processedSQL = normalizeLineEndings(sql);
        for (String statement : StringUtils.processMutliLineSQL(processedSQL, isStripComments(), isSplitStatements(), getEndDelimiter())) {
            if (database instanceof MSSQLDatabase) {
                 statement = statement.replaceAll("\n", "\r\n");
             }

            String escapedStatement = statement;
            try {
                if (database.getConnection() != null) {
                    escapedStatement = database.getConnection().nativeSQL(statement);
                }
            } catch (DatabaseException e) {
				escapedStatement = statement;
			}

            returnStatements.add(new RawSqlStatement(escapedStatement, getEndDelimiter()));
        }

        return returnStatements.toArray(new SqlStatement[returnStatements.size()]);
    }

    protected String normalizeLineEndings(String string) {
        return string.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
    }

    protected String prepareSqlForChecksumOld(String string) {
        string = string.trim(); //remove begininng and trailig space
        string = string.replace("\r\n", "\n").replace("\r", "\n"); //ensure line endings are consistent (for next replacements) across OS types
        string = string.replaceAll("\\s*\\n\\s*", " "); //remove line endings, preserving them as a space. Collapse any whitespace around them into the same space
        string = string.replaceAll("\\s+", " "); //collapse duplicate spaces

        return string;
    }

    protected String prepareSqlForChecksum(String sql) {
        if (sql == null) {
            return "";
        }
        return prepareSqlForChecksum(new ByteArrayInputStream(sql.getBytes()));
    }

    protected String prepareSqlForChecksum(InputStream stream) {
        if (stream == null) {
            return "";
        }
        StringBuilder returnString = new StringBuilder();
        String encoding = this. encoding;
        if (encoding == null) {
            encoding = "UTF-8";
        }

        Pattern multipleSpaces = Pattern.compile("\\s+");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));

            String line;
            while ((line = reader.readLine()) != null) {
                String fixedLine = multipleSpaces.matcher(line.trim()).replaceAll(" ");
                if (!fixedLine.equals("")) { //something on this line
                    returnString.append(fixedLine).append(" ");
                }
            }
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        if (returnString.length() > 0) {
            returnString.deleteCharAt(returnString.length()-1); //remove trailing space added
        }

        return returnString.toString();
    }

//    @Override
//    public Set<String> getSerializableFields() {
//        Set<String> fieldsToSerialize = new HashSet<String>(super.getSerializableFields());
//        fieldsToSerialize.add("splitStatements");
//        fieldsToSerialize.add("stripComments");
//        return Collections.unmodifiableSet(fieldsToSerialize);
//    }
//
//    @Override
//    public Object getSerializableFieldValue(String field) {
//        if (field.equals("splitStatements")) {
//            return isSplitStatements();
//        }
//    }
}
