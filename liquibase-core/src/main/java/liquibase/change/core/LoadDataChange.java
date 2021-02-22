package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ChangeWithColumns;
import liquibase.change.CheckSum;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.Warnings;
import liquibase.io.EmptyLineAndCommentSkippingInputStream;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.UtfBomAwareReader;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.InsertExecutablePreparedStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.structure.core.Column;
import liquibase.util.BooleanParser;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import liquibase.util.csv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;


@DatabaseChange(name = "loadData",
        description = "Loads data from a CSV file into an existing table. A value of NULL in a cell will be converted to a database NULL rather than the string 'NULL'\n" +
                "Lines starting with # (hash) sign are treated as comments. You can change comment pattern by specifying 'commentLineStartsWith' property in loadData tag." +
                "To disable comments set 'commentLineStartsWith' to empty value'\n" +
                "\n" +
                "Date/Time values included in the CSV file should be in ISO formathttp://en.wikipedia.org/wiki/ISO_8601 in order to be parsed correctly by Liquibase. Liquibase will initially set the date format to be 'yyyy-MM-dd'T'HH:mm:ss' and then it checks for two special cases which will override the data format string.\n" +
                "\n" +
                "If the string representing the date/time includes a '.', then the date format is changed to 'yyyy-MM-dd'T'HH:mm:ss.SSS'\n" +
                "If the string representing the date/time includes a space, then the date format is changed to 'yyyy-MM-dd HH:mm:ss'\n" +
                "Once the date format string is set, Liquibase will then call the SimpleDateFormat.parse() method attempting to parse the input string so that it can return a Date/Time. If problems occur, then a ParseException is thrown and the input string is treated as a String for the INSERT command to be generated.",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table",
        since = "1.7")
public class LoadDataChange extends AbstractChange implements ChangeWithColumns<LoadDataColumnConfig> {

    /**
     * CSV Lines starting with that sign(s) will be treated as comments by default
     */
    public static final String DEFAULT_COMMENT_PATTERN = "#";

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String file;
    private String commentLineStartsWith = DEFAULT_COMMENT_PATTERN;
    private Boolean relativeToChangelogFile;
    private String encoding = null;
    private String separator = liquibase.util.csv.CSVReader.DEFAULT_SEPARATOR + "";
    private String quotchar = liquibase.util.csv.CSVReader.DEFAULT_QUOTE_CHARACTER + "";

    private Boolean usePreparedStatements;

    private List<LoadDataColumnConfig> columns = new ArrayList<LoadDataColumnConfig>();

    @Override
    public boolean supports(Database database) {
        return true;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return true;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", description = "Name of the table to insert data into", requiredForDatabase = "all")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(description = "CSV file to load", exampleValue = "com/example/users.csv", requiredForDatabase = "all")
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Boolean getUsePreparedStatements() {
        return usePreparedStatements;
    }

    public void setUsePreparedStatements(Boolean usePreparedStatements) {
        this.usePreparedStatements = usePreparedStatements;
    }

    public String getCommentLineStartsWith() {
        return commentLineStartsWith;
    }

    public void setCommentLineStartsWith(String commentLineStartsWith) {

        //if the value is null (not provided) we want to use default value
        if (commentLineStartsWith == null) {
            this.commentLineStartsWith = DEFAULT_COMMENT_PATTERN;
        } else if (commentLineStartsWith.equals("")) {
            this.commentLineStartsWith = null;
        } else {
            this.commentLineStartsWith = commentLineStartsWith;
        }
    }

    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    @DatabaseChangeProperty(exampleValue = "UTF-8", description = "Encoding of the CSV file (defaults to UTF-8)")
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @DatabaseChangeProperty(exampleValue = ",")
    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        if (separator != null && separator.equals("\\t")) {
            separator = "\t";
        }
        this.separator = separator;
    }

    @DatabaseChangeProperty(exampleValue = "'")
    public String getQuotchar() {
        return quotchar;
    }

    public void setQuotchar(String quotchar) {
        this.quotchar = quotchar;
    }

    @Override
    public void addColumn(LoadDataColumnConfig column) {
        columns.add(column);
    }

    @Override
    @DatabaseChangeProperty(description = "Defines how the data should be loaded.", requiredForDatabase = "all")
    public List<LoadDataColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<LoadDataColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        CSVReader reader = null;
        try {
            reader = getCSVReader();

            if (reader == null) {
                throw new UnexpectedLiquibaseException("Unable to read file " + this.getFile());
            }

            String[] headers = reader.readNext();
            if (headers == null) {
                throw new UnexpectedLiquibaseException("Data file " + getFile() + " was empty");
            }

            List<SqlStatement> statements = new ArrayList<SqlStatement>();

            boolean anyPreparedStatements = false;

            String[] line;
            int lineNumber = 1; // Start at '1' to take into account the header (already processed)

            boolean isCommentingEnabled = StringUtils.isNotEmpty(commentLineStartsWith);
            while ((line = reader.readNext()) != null) {
                lineNumber++;
                if (line.length == 0 || (line.length == 1 && StringUtils.trimToNull(line[0]) == null)
                        || (isCommentingEnabled && isLineCommented(line))) {
                    continue; //nothing on this line
                }

                // Ensure eaech line has the same number of columns defined as does the header.
                // (Failure could indicate unquoted strings with commas, for example).
                if (line.length != headers.length) {
                    throw new UnexpectedLiquibaseException("CSV file " + getFile() + " Line " + lineNumber + " has " + line.length + " values defined, Header has " + headers.length + ". Numbers MUST be equal (check for unquoted string with embedded commas)");
                }

                boolean needsPreparedStatement = false;
                if (usePreparedStatements != null && usePreparedStatements) {
                    needsPreparedStatement = true;
                }

                List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
                for (int i = 0; i < headers.length; i++) {
                    Object value = line[i];
                    String columnName = headers[i].trim();

                    ColumnConfig valueConfig = new ColumnConfig();

                    ColumnConfig columnConfig = getColumnConfig(i, headers[i].trim());
                    if (columnConfig != null) {
                        if ("skip".equalsIgnoreCase(columnConfig.getType())) {
                            continue;
                        }

                        // don't overwrite header name unless there is actually a value to override it with
                        if (columnConfig.getName() != null) {
                            columnName = columnConfig.getName();
                        }

                        valueConfig.setName(columnName);

                        if (columnConfig.getType() != null) {
                            if (columnConfig.getType().equalsIgnoreCase("BOOLEAN")) {
                                if (value.toString().equalsIgnoreCase("NULL")) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValueBoolean(BooleanParser.parseBoolean(value.toString().toLowerCase()));
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase("NUMERIC")) {
                                if (value.toString().equalsIgnoreCase("NULL")) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValueNumeric(value.toString());
                                }
                            } else if (columnConfig.getType().toLowerCase().contains("date") || columnConfig.getType().toLowerCase().contains("time")) {
                                if (value.toString().equalsIgnoreCase("NULL")) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValueDate(value.toString());
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase("STRING")) {
                                if (value.toString().equalsIgnoreCase("NULL")) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValue(value.toString());
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase("COMPUTED")) {
                                if (value.toString().equalsIgnoreCase("NULL")) {
                                    valueConfig.setValue(null);
                                } else {
                                    liquibase.statement.DatabaseFunction function = new liquibase.statement.DatabaseFunction(value.toString());
                                    valueConfig.setValueComputed(function);
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase("SEQUENCE")) {
                                String sequenceName;
                                if (value.toString().equalsIgnoreCase("NULL")) {
                                    sequenceName = columnConfig.getDefaultValue();
                                    if (sequenceName == null) {
                                        throw new UnexpectedLiquibaseException("Must set a sequence name in the loadData column defaultValue attribute");
                                    }
                                } else {
                                    sequenceName = value.toString();
                                }
                                liquibase.statement.SequenceNextValueFunction function = new liquibase.statement.SequenceNextValueFunction(sequenceName);
                                valueConfig.setValueComputed(function);

                            } else if (columnConfig.getType().equalsIgnoreCase("BLOB")) {
                                if (value.toString().equalsIgnoreCase("NULL")) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValueBlobFile(value.toString());
                                    needsPreparedStatement = true;
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase("CLOB")) {
                                if (value.toString().equalsIgnoreCase("NULL")) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValueClobFile(value.toString());
                                    needsPreparedStatement = true;
                                }
                            } else {
                                throw new UnexpectedLiquibaseException("loadData type of " + columnConfig.getType() + " is not supported.  Please use BOOLEAN, NUMERIC, DATE, STRING, COMPUTED, SEQUENCE or SKIP");
                            }
                        }
                    } else {
                        if (columnName.contains("(") || columnName.contains(")") && database instanceof AbstractJdbcDatabase) {
                            columnName = ((AbstractJdbcDatabase) database).quoteObject(columnName, Column.class);
                        }

                        valueConfig.setName(columnName);

                        if (value == null || value.toString().equalsIgnoreCase("NULL")) {
                            // value is always going to be a string unless overridden by ColumnConfig
                            valueConfig.setValue((String) value);
                        } else {
                            valueConfig.setValue(value.toString());
                        }
                    }
                    columns.add(valueConfig);
                }

                if (needsPreparedStatement) {
                    anyPreparedStatements = true;

                    statements.add(new InsertExecutablePreparedStatement(database, getCatalogName(), getSchemaName(), getTableName(), columns,
                            getChangeSet(), getResourceAccessor()));
                } else {
                    InsertStatement insertStatement = this.createStatement(getCatalogName(), getSchemaName(), getTableName());

                    for (ColumnConfig column : columns) {
                        String columnName = column.getName();
                        Object value = column.getValueObject();

                        if (value == null) {
                            value = "NULL";
                        }

                        insertStatement.addColumnValue(columnName, value);
                    }

                    statements.add(insertStatement);
                }
            }

            if (anyPreparedStatements) {
                return statements.toArray(new SqlStatement[statements.size()]);
            } else {
                InsertSetStatement statementSet = this.createStatementSet(getCatalogName(), getSchemaName(), getTableName());
                for (SqlStatement stmt : statements) {
                    statementSet.addInsertStatement((InsertStatement) stmt);
                }

                if (database instanceof MSSQLDatabase || database instanceof MySQLDatabase || database instanceof PostgresDatabase) {
                    List<InsertStatement> innerStatements = statementSet.getStatements();
                    if (innerStatements != null && innerStatements.size() > 0 && innerStatements.get(0) instanceof InsertOrUpdateStatement) {
                        //cannot do insert or update in a single statement
                        return statementSet.getStatementsArray();
                    }
                    // we only return a single "statement" - it's capable of emitting multiple sub-statements, should the need arise, on generation.
                    return new SqlStatement[]{statementSet};
                } else {
                    return statementSet.getStatementsArray();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UnexpectedLiquibaseException ule) {
            if (getChangeSet() != null && getChangeSet().getFailOnError() != null && !getChangeSet().getFailOnError()) {
                Logger log = LogFactory.getLogger();
                log.info("Change set " + getChangeSet().toString(false) + " failed, but failOnError was false.  Error: " + ule.getMessage());
                return new SqlStatement[0];
            } else {
                throw ule;
            }
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }

    private boolean isLineCommented(String[] line) {
        return StringUtils.startsWith(line[0], commentLineStartsWith);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    public CSVReader getCSVReader() throws IOException {
        ResourceAccessor resourceAccessor = getResourceAccessor();
        if (resourceAccessor == null) {
            throw new UnexpectedLiquibaseException("No file resourceAccessor specified for " + getFile());
        }
        InputStream stream = StreamUtil.openStream(file, isRelativeToChangelogFile(), getChangeSet(), resourceAccessor);
        if (stream == null) {
            return null;
        }
        Reader streamReader;
        if (getEncoding() == null) {
            streamReader = new UtfBomAwareReader(stream);
        } else {
            streamReader = new UtfBomAwareReader(stream, getEncoding());
        }

        char quotchar;
        if (StringUtils.trimToEmpty(this.quotchar).length() == 0) {
            // hope this is impossible to have a field surrounded with non ascii char 0x01
            quotchar = '\1';
        } else {
            quotchar = this.quotchar.charAt(0);
        }

        if (separator == null) {
            separator = liquibase.util.csv.CSVReader.DEFAULT_SEPARATOR + "";
        }

        return new CSVReader(streamReader, separator.charAt(0), quotchar);
    }

    protected InsertStatement createStatement(String catalogName, String schemaName, String tableName) {
        return new InsertStatement(catalogName, schemaName, tableName);
    }

    protected InsertSetStatement createStatementSet(String catalogName, String schemaName, String tableName) {
        return new InsertSetStatement(catalogName, schemaName, tableName);
    }

    protected ColumnConfig getColumnConfig(int index, String header) {
        for (LoadDataColumnConfig config : columns) {
            if (config.getIndex() != null && config.getIndex().equals(index)) {
                return config;
            }
            if (config.getHeader() != null && config.getHeader().equalsIgnoreCase(header)) {
                return config;
            }

            if (config.getName() != null && config.getName().equalsIgnoreCase(header)) {
                return config;
            }
        }
        return null;
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Cannot check loadData status");
    }

    @Override
    public String getConfirmationMessage() {
        return "Data loaded from " + getFile() + " into " + getTableName();
    }

    @Override
    public CheckSum generateCheckSum() {
        InputStream stream = null;
        try {
            stream = StreamUtil.openStream(file, isRelativeToChangelogFile(), getChangeSet(), getResourceAccessor());
            stream = new EmptyLineAndCommentSkippingInputStream(stream, commentLineStartsWith);
            return CheckSum.compute(getTableName() + ":" + CheckSum.compute(stream, /*standardizeLineEndings*/ true));
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    @Override
    public Warnings warn(Database database) {
        return null;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
