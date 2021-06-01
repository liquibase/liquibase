package liquibase.change.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.change.*;
import liquibase.changelog.ChangeSet;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.*;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.io.EmptyLineAndCommentSkippingInputStream;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
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
import liquibase.util.ObjectUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;
import liquibase.util.csv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.ResourceBundle.getBundle;
import static liquibase.change.ChangeParameterMetaData.ALL;

@DatabaseChange(name = "loadData",
        description = "Loads data from a CSV file into an existing table. A value of NULL in a cell will be " +
                "converted to a database NULL rather than the string 'NULL'.\n" +
                "Lines starting with # (hash) sign are treated as comments. You can change comment pattern by " +
                "specifying 'commentLineStartsWith' attribute." +
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
                "to be generated.\n" +
                "If UUID type is used UUID value is stored as string and NULL in cell is supported.",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table",
        since = "1.7")
public class LoadDataChange extends AbstractTableChange implements ChangeWithColumns<LoadDataColumnConfig> {
    /**
     * CSV Lines starting with that sign(s) will be treated as comments by default
     */
    public static final String DEFAULT_COMMENT_PATTERN = "#";
    public static final Pattern BASE64_PATTERN = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");
    private static final Logger LOG = Scope.getCurrentScope().getLog(LoadDataChange.class);
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    private String file;
    private String commentLineStartsWith = DEFAULT_COMMENT_PATTERN;
    private Boolean relativeToChangelogFile;
    private String encoding;
    private String separator = CSVReader.DEFAULT_SEPARATOR + "";
    private String quotchar = CSVReader.DEFAULT_QUOTE_CHARACTER + "";
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

    @DatabaseChangeProperty( description = "Name of the table to insert data into",
            requiredForDatabase = ALL,  mustEqualExisting = "table" )
    public String getTableName() {
        return super.getTableName();
    }

    @DatabaseChangeProperty(
        description = "CSV file to load", exampleValue = "com/example/users.csv",
        requiredForDatabase = ALL)
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @DatabaseChangeProperty(
       description = "Use prepared statements instead of insert statement strings if the DB supports it")
    public Boolean getUsePreparedStatements() {
        return usePreparedStatements;
    }

    public void setUsePreparedStatements(Boolean usePreparedStatements) {
        this.usePreparedStatements = usePreparedStatements;
    }

    @DatabaseChangeProperty( supportsDatabase = ALL,
        description = "Lines staring with this are treated as comment and ignored. Default: " + DEFAULT_COMMENT_PATTERN)
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

    @DatabaseChangeProperty( supportsDatabase = ALL,
        description = "Option whether the 'file' is relative to the changelog file")
    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    @DatabaseChangeProperty(exampleValue = "UTF-8", supportsDatabase = ALL,
        description = "Encoding of the CSV file (defaults to UTF-8)")
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @DatabaseChangeProperty(exampleValue = ",", supportsDatabase = ALL,
        description = "Character separating the fields. Default: " + CSVReader.DEFAULT_SEPARATOR)
    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        if ((separator != null) && "\\t".equals(separator)) {
            separator = "\t";
        }
        this.separator = separator;
    }

    @DatabaseChangeProperty(exampleValue = "'", supportsDatabase = ALL,
        description = "The quote character for string fields containing the separator character. " +
            "Default: " + CSVReader.DEFAULT_QUOTE_CHARACTER)
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
    @DatabaseChangeProperty( supportsDatabase = ALL, serializationType = SerializationType.NESTED_OBJECT,
        description = "Column mapping and defaults can be defined.\n\n" +
                "'header' or 'index' attributes needs to be defined if the header name in the CSV " +
                "is different than the column name needs to be inserted\n" +
                "Not defined column type it is taken from the DB.\n" +
                "The 'defaultValue[XXX]' attributes can define value for empty fields.")
    public List<LoadDataColumnConfig> getColumns() { return columns; }

    @Override
    public void setColumns(List<LoadDataColumnConfig> columns) {
        this.columns = columns;
    }

    /**
     * Unique string for the column for better identification
     * @param index index of the column
     * @param columnConfig the column
     * @return
     */
    protected String columnIdString(int index, LoadDataColumnConfig columnConfig) {
        return " / column[" + index + "]" +
                (StringUtil.trimToNull(columnConfig.getName()) != null ?
                        " (name:'" + columnConfig.getName() + "')" : "") ;
    }

    /**
     * Validate all columns and collect errors in 'validationErrors'
     * @param validationErrors ValidationErrors to collect errors
     * @return validationErrors
     */
    protected ValidationErrors validateColumns(ValidationErrors validationErrors) {
        if (getColumns() != null) {
            int i = 1;
            for (LoadDataColumnConfig columnConfig : getColumns()) {
                validateColumn(columnConfig, validationErrors, columnIdString(i, columnConfig));
                i++;
            }
        }
        return validationErrors;
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

            // Make sure all take the column list we interpolated from the CSV headers
            addColumnsFromHeaders(headers);

            // If we have an real JDBC connection to the database, ask the database for any missing column types.
            try {
                retrieveMissingColumnLoadTypes(columns, database);
            } catch (DatabaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }

            List<ExecutablePreparedStatementBase> preparedStatements = new ArrayList<>();
            boolean anyPreparedStatements = false;
            String[] line;
            // Start at '1' to take into account the header (already processed):
            int lineNumber = 1;

            boolean isCommentingEnabled = StringUtil.isNotEmpty(commentLineStartsWith);

            List<SqlStatement> statements = new ArrayList<>();
            while ((line = reader.readNext()) != null) {
                lineNumber++;
                if
                ((line.length == 0) || ((line.length == 1) && (StringUtil.trimToNull(line[0]) == null)) ||
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

                boolean needsPreparedStatement = true;
                if (usePreparedStatements != null && !usePreparedStatements) {
                    needsPreparedStatement = false;
                }

                List<LoadDataColumnConfig> columnsFromCsv = new ArrayList<>();
                for (int i = 0; i < headers.length; i++) {
                    String value = line[i];
                    String columnName = headers[i].trim();

                    LoadDataColumnConfig valueConfig = new LoadDataColumnConfig();

                    LoadDataColumnConfig columnConfig = getColumnConfig(i, columnName);
                    if (columnConfig != null) {
                        if ("skip".equalsIgnoreCase(columnConfig.getType())) {
                            continue;
                        }

                        // don't overwrite header name unless there is actually a value to override it with
                        if (columnConfig.getName() != null) {
                            columnName = columnConfig.getName();
                        }

                        //
                        // Always set the type for the valueConfig if the value is NULL
                        //
                        if ("NULL".equalsIgnoreCase(value)) {
                            valueConfig.setType(columnConfig.getType());
                        }
                        valueConfig.setName(columnName);
                        valueConfig.setAllowUpdate(columnConfig.getAllowUpdate());

                        if (value.isEmpty()) {
                            value = columnConfig.getDefaultValue();
                        }
                        if (StringUtil.equalsWordNull(value)) {
                            valueConfig.setValue(null);
                        } else if (columnConfig.getType() == null) {
                            // columnConfig did not specify a type
                            valueConfig.setValue(value);
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.UNKNOWN) {
                            // columnConfig did not match a specific type
                            valueConfig.setValue(value);
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.BOOLEAN) {
                            if (value == null) { // TODO getDefaultValueBoolean should use BooleanParser.parseBoolean also for consistent behaviour
                                valueConfig.setValueBoolean(columnConfig.getDefaultValueBoolean());
                            } else {
                                valueConfig.setValueBoolean(BooleanParser.parseBoolean(value));
                            }
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.NUMERIC) {
                            if (value != null) {
                                valueConfig.setValueNumeric(value);
                            } else {
                                valueConfig.setValueNumeric(columnConfig.getDefaultValueNumeric());
                            }
                        } else if ( columnConfig.getType().toLowerCase().contains("date")
                                 || columnConfig.getType().toLowerCase().contains("time")
                        ) {
                                if ("NULL".equalsIgnoreCase(value) ||
                                    "".equals(value)) {
                                    valueConfig.setValue(null);
                                } else {
                                    try {
                                        // Need the column type for handling 'NOW' or 'TODAY' type column value
                                        valueConfig.setType(columnConfig.getType());
                                        if (value != null) {
                                            valueConfig.setValueDate(value);
                                        } else {
                                            valueConfig.setValueDate(columnConfig.getDefaultValueDate());
                                        }
                                    } catch (DateParseException e) {
                                        throw new UnexpectedLiquibaseException(e);
                                    }
                                }
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.STRING) {
                            valueConfig.setType(columnConfig.getType());
                                    valueConfig.setValue(value == null ? "" : value);
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.COMPUTED) {
                            if (null != value) {
                                liquibase.statement.DatabaseFunction function =
                                        new liquibase.statement.DatabaseFunction(value);
                                valueConfig.setValueComputed(function);
                            } else {
                                valueConfig.setValueComputed(columnConfig.getDefaultValueComputed());
                            }
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.SEQUENCE) {
                            if (value == null) {
                                throw new UnexpectedLiquibaseException(
                                        "Must set a sequence name in the loadData column defaultValue attribute"
                                );
                            }
                            liquibase.statement.SequenceNextValueFunction function =
                                    new liquibase.statement.SequenceNextValueFunction(value);
                            valueConfig.setValueComputed(function);

                            } else if (columnConfig.getType().equalsIgnoreCase(LOAD_DATA_TYPE.BLOB.toString())) {
                                if ("NULL".equalsIgnoreCase(value)) {
                                    valueConfig.setValue(null);
                                } else if (BASE64_PATTERN.matcher(value).matches()) {
                                    valueConfig.setType(columnConfig.getType());
                                    valueConfig.setValue(value);
                                    needsPreparedStatement = true;
                                } else {
                                    valueConfig.setValueBlobFile(value);
                                    needsPreparedStatement = true;
                                }
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.CLOB) {
                            valueConfig.setValueClobFile(value);
                            needsPreparedStatement = true;
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.UUID) {
                            valueConfig.setType(columnConfig.getType());
                                if ("NULL".equalsIgnoreCase(value)) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValue(value);
                                }
                            } else if (columnConfig.getType().equalsIgnoreCase(LOAD_DATA_TYPE.OTHER.toString())) {
                                valueConfig.setType(columnConfig.getType());
                                if ("NULL".equalsIgnoreCase(value)) {
                                    valueConfig.setValue(null);
                                } else {
                                    valueConfig.setValue(value);
                                }
                        } else {
                            throw new UnexpectedLiquibaseException(
                                    String.format(coreBundle.getString("loaddata.type.is.not.supported"),
                                        columnConfig.getType()
                                    )
                            );
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

                // Try to use prepared statements if any of the following conditions apply:
                // 1. There is no other option than using a prepared statement (e.g. in cases of LOBs) regardless
                //     of whether the 'usePreparedStatement' is set to false
                // 2. The database supports batched statements (for improved performance) AND we are not in an
                //    "SQL" mode (i.e. we generate an SQL file instead of actually modifying the database).
                if (
                        (needsPreparedStatement || (databaseSupportsBatchUpdates && !isLoggingExecutor(database)))
                                && hasPreparedStatementsImplemented()
                ) {
                    anyPreparedStatements = true;
                    ExecutablePreparedStatementBase stmt =
                        this.createPreparedStatement(
                            database, getCatalogName(), getSchemaName(), getTableName(), columnsFromCsv,
                            getChangeSet(), Scope.getCurrentScope().getResourceAccessor()
                        );
                    preparedStatements.add(stmt);
                } else {
                    InsertStatement insertStatement =
                        this.createStatement(getCatalogName(), getSchemaName(), getTableName());

                    for (LoadDataColumnConfig column : columnsFromCsv) {
                        String columnName = column.getName();
                        Object value = column.getValueObject();

                        if (value == null) {
                            value = "NULL";
                        }

                        insertStatement.addColumnValue(columnName, value);

                        if (insertStatement instanceof InsertOrUpdateStatement) {
                            ((InsertOrUpdateStatement) insertStatement).setAllowColumnUpdate(columnName, column.getAllowUpdate() == null || column.getAllowUpdate());
                        }
                    }

                    statements.add(insertStatement);
                }
                // end of: will we use a PreparedStatement?
            }
            // end of: loop for every input line from the CSV file

            if (anyPreparedStatements) {
                // If we have only prepared statements and the database supports batching, let's roll
                if (databaseSupportsBatchUpdates && statements.isEmpty() && (!preparedStatements.isEmpty())) {
                    if (database instanceof PostgresDatabase) {
                        // we don't do batch updates for Postgres but we still send as a prepared statement, see LB-744
                        return preparedStatements.toArray(new SqlStatement[preparedStatements.size()]);
                    } else {
                        return new SqlStatement[] {
                            new BatchDmlExecutablePreparedStatement(
                                    database, getCatalogName(), getSchemaName(),
                                    getTableName(), columns,
                                    getChangeSet(), Scope.getCurrentScope().getResourceAccessor(),
                                    preparedStatements)
                    };
                    }
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
        } catch (IOException | LiquibaseException e) {
            throw new RuntimeException(e);
        } catch (UnexpectedLiquibaseException ule) {
            if ((getChangeSet() != null) && (getChangeSet().getFailOnError() != null) && !getChangeSet()
                .getFailOnError()) {
                LOG.info("Change set " + getChangeSet().toString(false) +
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

    private boolean isLoggingExecutor(Database database) {
        final ExecutorService executorService = Scope.getCurrentScope().getSingleton(ExecutorService.class);

        return executorService.executorExists("logging", database) &&
              (executorService.getExecutor("logging", database) instanceof LoggingExecutor);
    }
    /**
     * Iterate through the List of LoadDataColumnConfig and ask the database for any column types that we have
     * no data type of.
     * @param columns a list of LoadDataColumnConfigs to process
     */
    @SuppressWarnings("CommentedOutCodeLine")
    private void retrieveMissingColumnLoadTypes(List<LoadDataColumnConfig> columns, Database database) throws
            DatabaseException {
        // If no column is missing type information, we are already done.
        if (columns.stream().noneMatch(c -> c.getType() == null)) {
            return;
        }

        // Snapshot the database table
        CatalogAndSchema catalogAndSchema = new CatalogAndSchema(getCatalogName(), getSchemaName());
        catalogAndSchema = catalogAndSchema.standardize(database);
        Table targetTable = new Table(catalogAndSchema.getCatalogName(), catalogAndSchema.getSchemaName(),
                database.correctObjectName(getTableName(), Table.class));
        Table snapshotOfTable;
        try {
            snapshotOfTable = SnapshotGeneratorFactory.getInstance().createSnapshot(
                        targetTable,
                        database, new SnapshotControl(database, Table.class, Column.class));
        } catch (InvalidExampleException e) {
            throw new DatabaseException(e);
        }
        if (snapshotOfTable == null) {
            LOG.warning(String.format(
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
            // Normalise the LoadDataColumnConfig column names to the database
            tableColumns.put(database.correctObjectName(c.getName(), Column.class), c);
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
            if (entry.getValue().getType() != null) {
                continue;
            }
            LoadDataColumnConfig columnConfig = entry.getValue();
            Column c = tableColumns.get(entry.getKey());
            if (null == c) {
                LOG.severe(String.format(coreBundle.getString("unable.to.find.column.in.table"),
                        columnConfig.getName(), snapshotOfTable));
            } else {
                DataType dataType = c.getType();
                if (dataType == null) {
                LOG.warning(String.format(coreBundle.getString("unable.to.find.load.data.type"),
                            columnConfig.toString(), snapshotOfTable));
                    columnConfig.setType(LOAD_DATA_TYPE.STRING);
                } else {
                    LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance()
                            .fromDescription(dataType.toString(), database);
                    if (liquibaseDataType != null) {
                        columnConfig.setType(liquibaseDataType.getLoadTypeName());
                    } else {
                    LOG.warning(String.format(coreBundle.getString("unable.to.convert.load.data.type"),
                                columnConfig.toString(), snapshotOfTable, dataType));
                    }
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

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors(this);
        validationErrors.addAll(super.validate(database));
        return validateColumns(validationErrors);
    }

    public void validateColumn(LoadDataColumnConfig columnConfig, ValidationErrors validationErrors, String columnIDString) {
        if(columnConfig.getHeader() != null && columnConfig.getIndex() != null) {
            validationErrors.addWarning("Since attribute 'header' is also defined, 'index' ignored for "
                    + validationErrors.getChangeName() + columnIDString);
        }
    }

    /**
     * Get the column using the name, index or the header
     * @param name name or header of the column searched
     * @param idx the index of the column searched
     * @return The column having the name or header equal to "name" or the index equal to idx
     *  or a new LoadDataColumnConfig if not found
     */
    protected LoadDataColumnConfig columnConfigFromName(String name, Integer idx) {
        for (LoadDataColumnConfig c : this.columns) {
            if (name.equals(c.getName()) || name.equals(c.getHeader()) || idx.equals(c.getIndex())) {
                return c;
            }
        }
        if(null == StringUtil.trimToNull(name)) {
            throw new UnexpectedLiquibaseException("Unreferenced unnamed column is not supported");
        }
        LoadDataColumnConfig cfg = new LoadDataColumnConfig();
        columns.add(cfg);
        cfg.setName(name);
        return cfg;
    }

    /**
     * Add columns if they were not specified in the loadData change, we interpolate their names from
     * the header columns of the CSV file.
     *
     * @param headers the headers of the CSV file
     * @return a List of LoadDataColumnConfigs
     */
    private void addColumnsFromHeaders(String[] headers) {
        int i = 0;
        for (String columnNameFromHeader : headers) {
            LoadDataColumnConfig loadDataColumnConfig = columnConfigFromName(columnNameFromHeader,i);
            loadDataColumnConfig.setIndex(i);
            loadDataColumnConfig.setHeader(columnNameFromHeader);
            i++;
        }
    }

    private boolean isLineCommented(String[] line) {
        return StringUtil.startsWith(line[0], commentLineStartsWith);
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    public CSVReader getCSVReader() throws IOException, LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        if (resourceAccessor == null) {
            throw new UnexpectedLiquibaseException("No file resourceAccessor specified for " + getFile());
        }
        String relativeTo = getRelativeTo();
        InputStream stream = resourceAccessor.openStream(relativeTo, file);
        if (stream == null) {
            return null;
        }
        Reader streamReader = StreamUtil.readStreamWithReader(stream, getEncoding());

        char quotchar;
        if (StringUtil.trimToEmpty(this.quotchar).isEmpty()) {
            // hope this is impossible to have a field surrounded with non ascii char 0x01
            quotchar = '\1';
        } else {
            quotchar = this.quotchar.charAt(0);
        }

        if (separator == null) {
            separator = CSVReader.DEFAULT_SEPARATOR + "";
        }

        return new CSVReader(streamReader, separator.charAt(0), quotchar);
    }

    protected String getRelativeTo() {
        String relativeTo = null;
        if (ObjectUtil.defaultIfNull(isRelativeToChangelogFile(), false)) {
            relativeTo = getChangeSet().getFilePath();
        }
        return relativeTo;
    }

    protected ExecutablePreparedStatementBase createPreparedStatement(
            Database database, String catalogName, String schemaName, String tableName,
            List<LoadDataColumnConfig> columns, ChangeSet changeSet, ResourceAccessor resourceAccessor) {
        return new InsertExecutablePreparedStatement(database, catalogName, schemaName, tableName, columns,
                changeSet, resourceAccessor);
    }

    protected InsertStatement createStatement(String catalogName, String schemaName, String tableName) {
        return new InsertStatement(catalogName, schemaName, tableName);
    }

    protected InsertSetStatement createStatementSet(String catalogName, String schemaName, String tableName) {
        return new InsertSetStatement(catalogName, schemaName, tableName);
    }

    protected LoadDataColumnConfig getColumnConfig(int index, String header) {
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
        return String.format(coreBundle.getString("loaddata.successful"), getFile(), getTableName());
    }

    @Override
    public CheckSum generateCheckSum() {
        InputStream stream = null;
        try {
            stream = Scope.getCurrentScope().getResourceAccessor().openStream(getRelativeTo(), file);
            if (stream == null) {
                throw new UnexpectedLiquibaseException(String.format(
                        coreBundle.getString("file.not.found"), file));
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
        BOOLEAN, NUMERIC, DATE, STRING, COMPUTED, SEQUENCE, BLOB, CLOB, SKIP,UUID, OTHER, UNKNOWN
    }
}
