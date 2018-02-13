package liquibase.change.core;

import liquibase.CatalogAndSchema;
import liquibase.change.*;
import liquibase.changelog.ChangeSet;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.io.EmptyLineAndCommentSkippingInputStream;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.UtfBomAwareReader;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.BatchDmlExecutablePreparedStatement;
import liquibase.statement.ExecutablePreparedStatementBase;
import liquibase.statement.InsertExecutablePreparedStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Table;
import liquibase.util.BooleanParser;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import liquibase.util.csv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

import static java.util.ResourceBundle.getBundle;

@DatabaseChange(name = "loadData",
        description = "Loads data from a CSV file into an existing table. A value of NULL in a cell will be " +
                "converted to a database NULL rather than the string 'NULL'.\n" +
                "Lines starting with # (hash) sign are treated as comments. You can change comment pattern by " +
                "specifying 'commentLineStartsWith' property in loadData tag." +
                "To disable comments set 'commentLineStartsWith' to empty value'\n" +
                "\n" +
                "If the data type for a load column is set to NUMERIC, numbers are parsed in US locale (e.g. 123.45)." +
                "\n" +
                "Date/Time values included in the CSV file should be in ISO format " +
            "http://en.wikipedia.org/wiki/ISO_8601 in order to be parsed correctly by Liquibase. Liquibase will " +
                "initially set the date format to be 'yyyy-MM-dd'T'HH:mm:ss' and then it checks for two special " +
                "cases which will override the data format string.\n" +
                "\n" +
                "If the string representing the date/time includes a '.', then the date format is changed to " +
                "'yyyy-MM-dd'T'HH:mm:ss.SSS'\n" +
                "If the string representing the date/time includes a space, then the date format is changed " +
                "to 'yyyy-MM-dd HH:mm:ss'\n" +
            "Once the date format string is set, Liquibase will then call the SimpleDateFormat.parse() method " +
                "attempting to parse the input string so that it can return a Date/Time. If problems occur, " +
                "then a ParseException is thrown and the input string is treated as a String for the INSERT command " +
                "to be generated.",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table",
        since = "1.7")
public class LoadDataChange extends AbstractChange implements ChangeWithColumns<LoadDataColumnConfig> {
    /**
     * CSV Lines starting with that sign(s) will be treated as comments by default
     */
    public static final String DEFAULT_COMMENT_PATTERN = "#";
    private static final Logger LOG = LogService.getLog(LoadDataChange.class);
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String file;
    private String commentLineStartsWith = DEFAULT_COMMENT_PATTERN;
    private Boolean relativeToChangelogFile;
    private String encoding;
    private String separator = liquibase.util.csv.CSVReader.DEFAULT_SEPARATOR + "";
    private String quotchar = liquibase.util.csv.CSVReader.DEFAULT_QUOTE_CHARACTER + "";
    private List<LoadDataColumnConfig> columns = new ArrayList<>();

    private Boolean usePreparedStatements;

    /**
     * Transform a value read from a CSV file into a string to be written into the database if the column type
     * is not known.
     *
     * @param value the value to transform
     * @return if the value is empty or the string "NULL" (case-insensitive), return the empty string.
     * If not, the value "toString()" representation (trimmed of spaces left and right) is returned.
     */
    protected static String getValueToWrite(Object value) {
        if ((value == null) || "NULL".equalsIgnoreCase(value.toString())) {
            return "";
        } else {
            return value.toString().trim();
        }
    }

    // TODO: We can currently do this for INSERT operations, but not yet for UPDATE operations, so loadUpdateDataChange
    // will overwrite this flag for now.
    protected boolean hasPreparedStatementsImplemented() {
        return true;
    }

    @Override
    public boolean supports(Database database) {
        return true;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return true;
    }

    @DatabaseChangeProperty(
        since = "3.0",
        mustEqualExisting = "table.catalog"
    )
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

    @DatabaseChangeProperty(
        description = "Name of the table to insert data into",
        requiredForDatabase = "all",
        mustEqualExisting = "table"
    )
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(
        description = "CSV file to load",
        exampleValue = "com/example/users.csv",
        requiredForDatabase = "all"
    )
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
        } else if ("".equals(commentLineStartsWith)) {
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

    @DatabaseChangeProperty(
        description = "Encoding of the CSV file (defaults to UTF-8)",
        exampleValue = "UTF-8"
    )
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
        if ((separator != null) && "\\t".equals(separator)) {
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
        boolean databaseSupportsBatchUpdates = false;
        try {
            databaseSupportsBatchUpdates = database.supportsBatchUpdates();
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

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

            // If we do not have a column list yet, take the column list we interpolated from the CSV headers
            // earlier.
            if (columns.isEmpty()) {
                columns.addAll(getColumnsFromHeaders(headers));
            }

            // If we have an real JDBC connection to the database, ask the database for any missing column types.
            try {
                retrieveMissingColumnLoadTypes(columns, database);
            } catch (DatabaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }

            List<ExecutablePreparedStatementBase> batchedStatements = new ArrayList<>();
            boolean anyPreparedStatements = false;
            String[] line;
            // Start at '1' to take into account the header (already processed):
            int lineNumber = 1;

            boolean isCommentingEnabled = StringUtils.isNotEmpty(commentLineStartsWith);

            List<SqlStatement> statements = new ArrayList<>();
            while ((line = reader.readNext()) != null) {
                lineNumber++;
                if
                ((line.length == 0) || ((line.length == 1) && (StringUtils.trimToNull(line[0]) == null)) ||
                    (isCommentingEnabled && isLineCommented(line))
                ) {
                    //nothing interesting on this line
                    continue;
                }

                // Ensure each line has the same number of columns defined as does the header.
                // (Failure could indicate unquoted strings with commas, for example).
                if (line.length != headers.length) {
                    throw new UnexpectedLiquibaseException(
                        "CSV file " + getFile() + " Line " + lineNumber + " has " + line.length +
                            " values defined, Header has " + headers.length +
                            ". Numbers MUST be equal (check for unquoted string with embedded commas)"
                    );
                }

                boolean needsPreparedStatement = false;
                if (usePreparedStatements != null && usePreparedStatements) {
                    needsPreparedStatement = true;
                }

                List<ColumnConfig> columnsFromCsv = new ArrayList<>();
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
                            if (columnConfig.getType().equalsIgnoreCase(LOAD_DATA_TYPE.BOOLEAN.toString())) {
                                if ("NULL".equalsIgnoreCase(value.toString())) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValueBoolean(
                                        BooleanParser.parseBoolean(value.toString().toLowerCase())
                                    );
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase(LOAD_DATA_TYPE.NUMERIC.toString())) {
                                if ("NULL".equalsIgnoreCase(value.toString())) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValueNumeric(value.toString());
                                }
                            } else if
                            (
                                columnConfig.getType().toLowerCase().contains("date")
                                    || columnConfig.getType().toLowerCase().contains("time")
                            ) {
                                if ("NULL".equalsIgnoreCase(value.toString())) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValueDate(value.toString());
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase(LOAD_DATA_TYPE.STRING.toString())) {
                                if ("NULL".equalsIgnoreCase(value.toString())) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValue(value.toString());
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase(LOAD_DATA_TYPE.COMPUTED.toString())) {
                                if ("NULL".equalsIgnoreCase(value.toString())) {
                                    valueConfig.setValue(null);
                                } else {
                                    liquibase.statement.DatabaseFunction function =
                                        new liquibase.statement.DatabaseFunction(value.toString());
                                    valueConfig.setValueComputed(function);
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase(LOAD_DATA_TYPE.SEQUENCE.toString())) {
                                String sequenceName;
                                if ("NULL".equalsIgnoreCase(value.toString())) {
                                    sequenceName = columnConfig.getDefaultValue();
                                    if (sequenceName == null) {
                                        throw new UnexpectedLiquibaseException(
                                            "Must set a sequence name in the loadData column defaultValue attribute"
                                        );
                                    }
                                } else {
                                    sequenceName = value.toString();
                                }
                                liquibase.statement.SequenceNextValueFunction function =
                                    new liquibase.statement.SequenceNextValueFunction(sequenceName);
                                valueConfig.setValueComputed(function);

                            } else if (columnConfig.getType().equalsIgnoreCase(LOAD_DATA_TYPE.BLOB.toString())) {
                                if ("NULL".equalsIgnoreCase(value.toString())) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValueBlobFile(value.toString());
                                    needsPreparedStatement = true;
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase(LOAD_DATA_TYPE.CLOB.toString())) {
                                if ("NULL".equalsIgnoreCase(value.toString())) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValueClobFile(value.toString());
                                    needsPreparedStatement = true;
                                }
                            } else {
                                throw new UnexpectedLiquibaseException(
                                    String.format(coreBundle.getString("loaddata.type.is.not.supported"),
                                        columnConfig.getType()
                                    )
                                );
                            }
                        } else {
                            // columnConfig did not specify a type
                            valueConfig.setValue(getValueToWrite(value));
                        }
                    } else {
                        // No columnConfig found. Assume header column name to be the table column name.
                        if (columnName.contains("(") || (columnName.contains(")") && (database instanceof
                            AbstractJdbcDatabase))) {
                            columnName = ((AbstractJdbcDatabase) database).quoteObject(columnName, Column.class);
                        }

                        valueConfig.setName(columnName);

                        valueConfig.setValue(getValueToWrite(value));
                    }
                    columnsFromCsv.add(valueConfig);
                }
                // end of: iterate through all the columns of a CSV line

                // Try to use prepared statements if any of the two following conditions apply:
                // 1. There is no other option than using a prepared statement (e.g. in cases of LOBs)
                // 2. The database supports batched statements (for improved performance) AND we are not in an
                //    "SQL" mode (i.e. we generate an SQL file instead of actually modifying the database).
                if
                (
                    (needsPreparedStatement ||
                        (databaseSupportsBatchUpdates &&
                                !(ExecutorService.getInstance().getExecutor(database) instanceof LoggingExecutor)
                        )
                    )
                    && hasPreparedStatementsImplemented()
                ) {
                    anyPreparedStatements = true;
                    ExecutablePreparedStatementBase stmt =
                        this.createPreparedStatement(
                            database, getCatalogName(), getSchemaName(), getTableName(), columnsFromCsv,
                            getChangeSet(), getResourceAccessor()
                        );
                    batchedStatements.add(stmt);
                } else {
                    InsertStatement insertStatement =
                        this.createStatement(getCatalogName(), getSchemaName(), getTableName());

                    for (ColumnConfig column : columnsFromCsv) {
                        String columnName = column.getName();
                        Object value = column.getValueObject();

                        if (value == null) {
                            value = "NULL";
                        }

                        insertStatement.addColumnValue(columnName, value);
                    }

                    statements.add(insertStatement);
                }
                // end of: will we use a PreparedStatement?
            }
            // end of: loop for every input line from the CSV file

            if (anyPreparedStatements) {

                // If we have only prepared statements and the database supports batching, let's roll
                if (databaseSupportsBatchUpdates && statements.isEmpty() && (!batchedStatements.isEmpty())) {
                    return new SqlStatement[] {
                            new BatchDmlExecutablePreparedStatement(
                                    database, getCatalogName(), getSchemaName(),
                                    getTableName(), columns,
                                    getChangeSet(), getResourceAccessor(),
                                    batchedStatements)
                    };
                } else {
                    return statements.toArray(new SqlStatement[statements.size()]);
                }
            } else {
            	if (statements.isEmpty()) {
            		// avoid returning unnecessary dummy statement
            		return new SqlStatement[0];
            	}

                InsertSetStatement statementSet = this.createStatementSet(
                        getCatalogName(), getSchemaName(), getTableName()
                );
                for (SqlStatement stmt : statements) {
                    statementSet.addInsertStatement((InsertStatement) stmt);
                }

                if ((database instanceof MSSQLDatabase) || (database instanceof MySQLDatabase) || (database
                    instanceof PostgresDatabase)) {
                    List<InsertStatement> innerStatements = statementSet.getStatements();
                    if ((innerStatements != null) && (!innerStatements.isEmpty()) && (innerStatements.get(0)
                        instanceof InsertOrUpdateStatement)) {
                        //cannot do insert or update in a single statement
                        return statementSet.getStatementsArray();
                    }
                    // we only return a single "statement" - it's capable of emitting multiple sub-statements,
                    // should the need arise, on generation.
                    return new SqlStatement[]{statementSet};
                } else {
                    return statementSet.getStatementsArray();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UnexpectedLiquibaseException ule) {
            if ((getChangeSet() != null) && (getChangeSet().getFailOnError() != null) && !getChangeSet()
                .getFailOnError()) {
                LOG.info(LogType.LOG, "Change set " + getChangeSet().toString(false) +
                         " failed, but failOnError was false.  Error: " + ule.getMessage());
                return new SqlStatement[0];
            } else {
                throw ule;
            }
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }
    }

    /**
     * Iterate through the List of LoadDataColumnConfig and ask the database for any column types that we have
     * no data type of.
     * @param columns a list of LoadDataColumnConfigs to process
     */
    @SuppressWarnings("CommentedOutCodeLine")
    private void retrieveMissingColumnLoadTypes(List<LoadDataColumnConfig> columns, Database database) throws
            DatabaseException {
        boolean matched = false;

        // If no column is missing type information, we are already done.
        for (LoadDataColumnConfig c : columns) {
            if (c.getType() == null) {
                matched = true;
            }
        }
        if (!matched) {
            return;
        }
        /* The above is the JDK7 version of:
           if (columns.stream().noneMatch(c -> c.getType() == null)) {
            return;
        }
        */

        // Snapshot the database table
        CatalogAndSchema catalogAndSchema = new CatalogAndSchema(getCatalogName(), getSchemaName());
        catalogAndSchema = catalogAndSchema.standardize(database);
        Table targetTable = new Table(catalogAndSchema.getCatalogName(), catalogAndSchema.getSchemaName(),
                database.correctObjectName(getTableName(), Table.class));
        Table snapshotOfTable;
        try {
            snapshotOfTable = SnapshotGeneratorFactory.getInstance().createSnapshot(
                        targetTable,
                        database);
        } catch (InvalidExampleException e) {
            throw new DatabaseException(e);
        }
        if (snapshotOfTable == null) {
            LOG.warning(LogType.LOG, String.format(
                    coreBundle.getString("could.not.snapshot.table.to.get.the.missing.column.type.information"),
                    database.escapeTableName(
                            targetTable.getSchema().getCatalogName(),
                            targetTable.getSchema().getName(),
                            targetTable.getName())
            ));
            return;
        }

        // Save the columns of the database table in a lookup table
        Map<String, Column> tableColumns = new HashMap<>();
        for (Column c : snapshotOfTable.getColumns()) {
            tableColumns.put(c.getName(), c);
        }
        /* The above is the JDK7 version of:
            snapshotOfTable.getColumns().forEach(c -> tableColumns.put(c.getName(), c));
        */

        // Normalise the LoadDataColumnConfig column names to the database
        Map<String, LoadDataColumnConfig> columnConfigs = new HashMap<>();
        for (LoadDataColumnConfig c : columns) {
            columnConfigs.put(
                database.correctObjectName(c.getName(), Column.class),
                c
            );
        }
        /* The above is the JDK7 version of:
        columns.forEach(c -> columnConfigs.put(
                database.correctObjectName(c.getName(), Column.class),
                c
        ));
        */

        for (Map.Entry<String, LoadDataColumnConfig> entry : columnConfigs.entrySet()) {
            if (!(entry.getValue().getType() == null)) {
                continue;
            }
            LoadDataColumnConfig columnConfig = entry.getValue();
            DataType dataType = tableColumns.get(entry.getKey()).getType();
            if (dataType == null) {
                LOG.warning(LogType.LOG, String.format(coreBundle.getString("unable.to.find.load.data.type"),
                    columnConfig.toString(), snapshotOfTable.toString()));
                columnConfig.setType(LOAD_DATA_TYPE.STRING.toString());
            } else {
                LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance()
                    .fromDescription(dataType.toString(), database);
                if (liquibaseDataType != null) {
                    columnConfig.setType(liquibaseDataType.getLoadTypeName().toString());
                } else {
                    LOG.warning(LogType.LOG, String.format(coreBundle.getString("unable.to.convert.load.data.type"),
                        columnConfig.toString(), snapshotOfTable.toString(), liquibaseDataType.toString()));
                }
            }
        }

        /* The above is the JDK7 version of:
        columnConfigs.entrySet().stream()
                .filter(entry -> entry.getValue().getType() == null)
                .forEach(entry -> {
                    LoadDataColumnConfig columnConfig = entry.getValue();
                    DataType dataType = tableColumns.get(entry.getKey()).getType();
                    if (dataType == null) {
                        LOG.warning(String.format(coreBundle.getString("unable.to.find.load.data.type"),
                                columnConfig.toString(), snapshotOfTable.toString() ));
                        columnConfig.setType(LOAD_DATA_TYPE.STRING.toString());
                    } else {
                        LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance()
                                .fromDescription(dataType.toString(), database);
                        if (liquibaseDataType != null) {
                            columnConfig.setType(liquibaseDataType.getLoadTypeName().toString());
                        } else {
                            LOG.warning(String.format(coreBundle.getString("unable.to.convert.load.data.type"),
                                    columnConfig.toString(), snapshotOfTable.toString(), liquibaseDataType.toString()));
                        }
                    }
                }
        );
        */
    }

    /**
     * If no columns (and their data types) are specified in the loadData change, we interpolate their names from
     * the header columns of the CSV file.
     * @param headers the headers of the CSV file
     * @return a List of LoadDataColumnConfigs
     */
    private List<LoadDataColumnConfig> getColumnsFromHeaders(String[] headers) {
        ArrayList<LoadDataColumnConfig> result = new ArrayList<>();
        int i=0;
        for (String columnNameFromHeader : headers) {
            LoadDataColumnConfig loadDataColumnConfig = new LoadDataColumnConfig();
            loadDataColumnConfig.setIndex(i);
            loadDataColumnConfig.setHeader(columnNameFromHeader);
            loadDataColumnConfig.setName(columnNameFromHeader);
            result.add(loadDataColumnConfig);
            i++;
        }
        return result;
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
        if (StringUtils.trimToEmpty(this.quotchar).isEmpty()) {
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

    protected ExecutablePreparedStatementBase createPreparedStatement(
            Database database, String catalogName, String schemaName, String tableName,
            List<ColumnConfig> columns, ChangeSet changeSet, ResourceAccessor resourceAccessor) {
        return new InsertExecutablePreparedStatement(database, catalogName, schemaName, tableName, columns,
                changeSet, resourceAccessor);
    }

    protected InsertStatement createStatement(String catalogName, String schemaName, String tableName) {
        return new InsertStatement(catalogName, schemaName, tableName);
    }

    protected InsertSetStatement createStatementSet(String catalogName, String schemaName, String tableName) {
        return new InsertSetStatement(catalogName, schemaName, tableName);
    }

    protected ColumnConfig getColumnConfig(int index, String header) {
        for (LoadDataColumnConfig config : columns) {
            if ((config.getIndex() != null) && config.getIndex().equals(index)) {
                return config;
            }
            if ((config.getHeader() != null) && config.getHeader().equalsIgnoreCase(header)) {
                return config;
            }

            if ((config.getName() != null) && config.getName().equalsIgnoreCase(header)) {
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
            if (stream == null) {
                throw new UnexpectedLiquibaseException(getFile() + " could not be found");
            }
            stream = new EmptyLineAndCommentSkippingInputStream(stream, commentLineStartsWith);
            return CheckSum.compute(getTableName() + ":" + CheckSum.compute(stream, /*standardizeLineEndings*/ true));
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                    // Do nothing
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

    @SuppressWarnings("HardCodedStringLiteral")
    public enum LOAD_DATA_TYPE {
        BOOLEAN, NUMERIC, DATE, STRING, COMPUTED, SEQUENCE, BLOB, CLOB, SKIP
    }
}
