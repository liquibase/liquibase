package liquibase.change;

import liquibase.change.core.RawSQLChange;
import liquibase.Scope;
import liquibase.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawCompoundStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StringUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static liquibase.statement.SqlStatement.EMPTY_SQL_STATEMENT;

/**
 * A common parent for all raw SQL related changes regardless of where the sql was sourced from.
 * <p>
 * Implements the necessary logic to choose how the SQL string should be parsed to generate the statements.
 *
 */
@SuppressWarnings("java:S5998")
public abstract class AbstractSQLChange extends AbstractChange implements DbmsTargetedChange {

    private boolean stripComments;
    private boolean splitStatements;
    /**
     *
     * @deprecated  To be removed when splitStatements is changed to be type Boolean
     *
     */
    private boolean splitStatementsSet;

    private String endDelimiter;
    private String sql;
    private String dbms;

    protected String encoding;


    protected AbstractSQLChange() {
        setStripComments(null);
        setSplitStatements(null);
    }

    public InputStream openSqlStream() throws IOException {
        return null;
    }

    @Override
    @DatabaseChangeProperty(since = "3.0", exampleValue = "h2, oracle",
        description = "Specifies which database type(s) a changeset is to be used for. " +
            "See valid database type names on Supported Databases docs page. Separate multiple databases with commas. " +
            "Specify that a changeset is not applicable to a particular database type by prefixing with !. " +
            "The keywords 'all' and 'none' are also available.")
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
     * @return always true (in AbstractSQLChange)
     */
    @Override
    public boolean supports(Database database) {
        return true;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (StringUtil.trimToNull(sql) == null) {
            validationErrors.addError("'sql' is required");
        }
        return validationErrors;

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
            splitStatementsSet = true;
        }
    }

    /**
     * @deprecated  To be removed when splitStatements is changed to be Boolean type
     * @return
     */
    @Deprecated
    public boolean isSplitStatementsSet() {
        return splitStatementsSet;
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
       this.sql = StringUtil.trimToNull(sql);
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
        InputStream stream = null;
        try {
            stream = openSqlStream();

            String sql = this.sql;
            if ((stream == null) && (sql == null)) {
                sql = "";
            }

            if (sql != null) {
                stream = new ByteArrayInputStream(sql.getBytes(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue())
                );
            }

            return CheckSum.compute(new NormalizingStream(this.getEndDelimiter(), this.isSplitStatements(), this.isStripComments(), stream), false);
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Scope.getCurrentScope().getLog(getClass()).fine("Error closing stream", e);
                }
            }
        }
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

        List<SqlStatement> returnStatements = new ArrayList<>();

        String sql = StringUtil.trimToNull(getSql());
        if (sql == null) {
            return EMPTY_SQL_STATEMENT;
        }

        String processedSQL = normalizeLineEndings(sql);
        if (this instanceof RawSQLChange && ((RawSQLChange) this).isRerunnable()) {
            returnStatements.add(new RawSqlStatement(processedSQL, getEndDelimiter()));
            return returnStatements.toArray(EMPTY_SQL_STATEMENT);
        }
        for (String statement : StringUtil.processMultiLineSQL(processedSQL, isStripComments(), isSplitStatements(), getEndDelimiter())) {
            if (database instanceof MSSQLDatabase) {
                statement = statement.replaceAll("\\n", "\r\n");
            }

            String escapedStatement = statement;
            try {
                if (database.getConnection() != null) {
                    escapedStatement = database.getConnection().nativeSQL(statement);
                }
            } catch (DatabaseException e) {
                escapedStatement = statement;
            }

            if (database instanceof Db2zDatabase && escapedStatement.toUpperCase().startsWith("CALL")) {
                returnStatements.add(new RawCompoundStatement(escapedStatement, getEndDelimiter()));
            } else {
                returnStatements.add(new RawSqlStatement(escapedStatement, getEndDelimiter()));
            }
        }

        return returnStatements.toArray(EMPTY_SQL_STATEMENT);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Cannot check raw sql status");
    }

    protected String normalizeLineEndings(String string) {
        return string.replace("\r", "");
    }

    public static class NormalizingStream extends InputStream {
        private ByteArrayInputStream headerStream;
        private PushbackInputStream stream;

        private byte[] quickBuffer = new byte[100];
        private List<Byte> resizingBuffer = new ArrayList<>();


        private int lastChar = 'X';
        private boolean seenNonSpace;

        public NormalizingStream(String endDelimiter, Boolean splitStatements, Boolean stripComments, InputStream stream) {
            this.stream = new PushbackInputStream(stream, 2048);
            try {
                this.headerStream = new ByteArrayInputStream((endDelimiter+":"+splitStatements+":"+stripComments+":").getBytes(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
            } catch (UnsupportedEncodingException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

        @Override
        public int read() throws IOException {
            if (headerStream != null) {
                int returnChar = headerStream.read();
                if (returnChar != -1) {
                    return returnChar;
                }
                headerStream = null;
            }

            int returnChar = stream.read();
            if (isWhiteSpace(returnChar)) {
                returnChar = ' ';
            }

            while ((returnChar == ' ') && (!seenNonSpace || (lastChar == ' '))) {
                returnChar = stream.read();

                if (isWhiteSpace(returnChar)) {
                    returnChar = ' ';
                }
            }

            seenNonSpace = true;

            lastChar = returnChar;

            if ((lastChar == ' ') && isOnlyWhitespaceRemaining()) {
                return -1;
            }

            return returnChar;
        }

        @Override
        public int available() throws IOException {
            return stream.available();
        }

        @Override
        public boolean markSupported() {
            return stream.markSupported();
        }

        @Override
        public synchronized void mark(int readLimit) {
            stream.mark(readLimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            stream.reset();
        }

        private boolean isOnlyWhitespaceRemaining() throws IOException {
            try {
                int quickBufferUsed = 0;
                while (true) {
                    byte read = (byte) stream.read();
                    if (quickBufferUsed >= quickBuffer.length) {
                        resizingBuffer.add(read);
                    } else {
                        quickBuffer[quickBufferUsed++] = read;
                    }

                    if (read == -1) {
                        return true;
                    }
                    if (!isWhiteSpace(read)) {
                        if (!resizingBuffer.isEmpty()) {

                            byte[] buf = new byte[resizingBuffer.size()];
                            for (int i=0; i< resizingBuffer.size(); i++) {
                                buf[i] = resizingBuffer.get(i);
                            }

                            stream.unread(buf);
                        }

                        stream.unread(quickBuffer, 0, quickBufferUsed);
                        return false;
                    }
                }
            } finally {
                resizingBuffer.clear();
            }
        }

        private boolean isWhiteSpace(int read) {
            return (read == ' ') || (read == '\n') || (read == '\r') || (read == '\t');
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }
}
