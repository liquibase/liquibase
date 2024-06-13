package liquibase.change.core;

import com.opencsv.exceptions.CsvMalformedLineException;
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
import liquibase.resource.Resource;
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
import liquibase.util.BooleanUtil;
import liquibase.util.ObjectUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;
import liquibase.util.csv.CSVReader;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static java.util.ResourceBundle.getBundle;
import static liquibase.change.ChangeParameterMetaData.ALL;

@DatabaseChange(name = "loadData",
        description = "Loads data from a CSV file into an existing table",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table",
        since = "1.7")
@SuppressWarnings("java:S2583")
public class LoadDataChange extends AbstractTableChange implements ChangeWithColumns<LoadDataColumnConfig> {
    /**
     * CSV Lines starting with that sign(s) will be treated as comments by default
     */
    public static final String DEFAULT_COMMENT_PATTERN = "#";
    public static final Pattern BASE64_PATTERN = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");
    private static final Logger LOG = Scope.getCurrentScope().getLog(LoadDataChange.class);
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    @Setter
    private String file;
    private String commentLineStartsWith = DEFAULT_COMMENT_PATTERN;
    @Setter
    private Boolean relativeToChangelogFile;
    @Setter
    private String encoding;
    private String separator = CSVReader.DEFAULT_SEPARATOR + "";
    @Setter
    private String quotchar = CSVReader.DEFAULT_QUOTE_CHARACTER + "";
    private List<LoadDataColumnConfig> columns = new ArrayList<>();

    @Setter
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

    @Override
    @DatabaseChangeProperty(description = "Name of the table to insert data into",
        requiredForDatabase = ALL, mustEqualExisting = "table")
    public String getTableName() {
        return super.getTableName();
    }

    @DatabaseChangeProperty(
        description = "CSV file to load", exampleValue = "com/example/users.csv",
        requiredForDatabase = ALL)
    public String getFile() {
        return file;
    }

    @DatabaseChangeProperty(
        description = "Whether to use prepared statements instead of INSERT statement strings (if the database supports it)")
    public Boolean getUsePreparedStatements() {
        return usePreparedStatements;
    }

    @DatabaseChangeProperty(supportsDatabase = ALL,
        description = "Lines starting with this are treated as comments and ignored. "+
            "To disable comments, set 'commentLineStartsWith' to an empty value. Default: " + DEFAULT_COMMENT_PATTERN)
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

    @DatabaseChangeProperty(supportsDatabase = ALL, description = "Specifies whether the file path is relative to the changelog file " +
        "rather than looked up in the search path. Default: false.")
    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    @DatabaseChangeProperty(exampleValue = "UTF-8", supportsDatabase = ALL,
        description = "Encoding of the CSV file (defaults to UTF-8)")
    public String getEncoding() {
        return encoding;
    }

    @DatabaseChangeProperty(exampleValue = ",", supportsDatabase = ALL,
        description = "Character separating the fields. Default: " + CSVReader.DEFAULT_SEPARATOR)
    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        if ("\\t".equals(separator)) {
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

    @Override
    public void addColumn(LoadDataColumnConfig column) {
        columns.add(column);
    }

    @Override
    @DatabaseChangeProperty(supportsDatabase = ALL, serializationType = SerializationType.NESTED_OBJECT,
        description = "Column mapping and defaults can be defined.\n\n" +
            "'header' or 'index' attributes need to be defined. If the header name in the CSV " +
            "is different than the column, 'name' needs to be inserted.\n" +
            "If column 'type' is not defined, the type is taken from the database. " +
            "Otherwise, for non-string columns, the type definition might be required.\n" +
            "The 'defaultValue*' attributes can define values for empty fields.")
    public List<LoadDataColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<LoadDataColumnConfig> columns) {
        this.columns = columns;
    }

    /**
     * Returns a unique {@code string} for the column for better identification.
     * The returned {@code string} includes the index of the column and, if available, its name.
     *
     * @param index        the index of the column
     * @param columnConfig the column configuration
     * @return a string in the format " / column[index] (name:'columnName')" or " / column[index]" if the column name is not available.
     */
    protected String columnIdString(int index, LoadDataColumnConfig columnConfig) {
        return " / column[" + index + "]" +
                (StringUtil.trimToNull(columnConfig.getName()) != null ?
                        " (name:'" + columnConfig.getName() + "')" : "");
    }

    /**
     * Validate all columns and collect errors in 'validationErrors'
     *
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
        supportsBatchUpdates(database);

        try (CSVReader reader = getCSVReader()) {

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

            String[] line;
            // Start at '1' to take into account the header (already processed):
            int lineNumber = 1;

            boolean isCommentingEnabled = StringUtil.isNotEmpty(commentLineStartsWith);

            List<LoadDataRowConfig> rows = new ArrayList<>();
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

                boolean needsPreparedStatement = false;

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
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.BOOLEAN) {
                            if (value == null) { // TODO getDefaultValueBoolean should use BooleanUtil.parseBoolean also for consistent behaviour
                                valueConfig.setValueBoolean(columnConfig.getDefaultValueBoolean());
                            } else {
                                valueConfig.setValueBoolean(BooleanUtil.parseBoolean(value));
                            }
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.NUMERIC) {
                            if (value != null) {
                                valueConfig.setValueNumeric(value);
                            } else {
                                valueConfig.setValueNumeric(columnConfig.getDefaultValueNumeric());
                            }
                        } else if (columnConfig.getType().equalsIgnoreCase("date")
                                || columnConfig.getType().equalsIgnoreCase("datetime")
                                || columnConfig.getType().equalsIgnoreCase("time")) {
                            if ("NULL".equalsIgnoreCase(value) || "".equals(value)) {
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
                                    new liquibase.statement.SequenceNextValueFunction(getSchemaName(), value);
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
                        } else if (columnConfig.getTypeEnum() == LOAD_DATA_TYPE.UNKNOWN) {
                            // columnConfig did not match a specific type
                            valueConfig.setValue(value);
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
                // BUT: if the user specifically requests usePreparedStatement=false, then respect that
                boolean actuallyUsePreparedStatements = false;
                if (hasPreparedStatementsImplemented()) {
                    if (usePreparedStatements != null) {
                        if (!usePreparedStatements && needsPreparedStatement) {
                            throw new UnexpectedLiquibaseException("loadData is requesting usePreparedStatements=false but prepared statements are required");
                        }
                        actuallyUsePreparedStatements = usePreparedStatements;
                    } else {
                        actuallyUsePreparedStatements = needsPreparedStatement || (!isLoggingExecutor(database) && preferPreparedStatements(database));
                    }
                }
                rows.add(new LoadDataRowConfig(actuallyUsePreparedStatements, columnsFromCsv));
            }
            return generateStatementsFromRows(database, rows);
        } catch (CsvMalformedLineException e) {
            throw new RuntimeException("Error parsing " + getRelativeTo() + " on line " + e.getLineNumber() + ": " + e.getMessage());
        } catch (IOException | LiquibaseException e) {
            throw new RuntimeException(e);
        } catch (UnexpectedLiquibaseException ule) {
            if ((getChangeSet() != null) && (getChangeSet().getFailOnError() != null) && !getChangeSet()
                    .getFailOnError()) {
                LOG.info("Changeset " + getChangeSet().toString(false) +
                        " failed, but failOnError was false.  Error: " + ule.getMessage());
                return SqlStatement.EMPTY_SQL_STATEMENT;
            } else {
                throw ule;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Do nothing
    }

    /**
     * If the loaded data does not require a prepared statement, and the user did not specify whether to use them or not,
     * should we use prepared statements as a default?
     */
    private boolean preferPreparedStatements(Database database) {
        if (database instanceof PostgresDatabase) {
            return false; //postgresql seems surprisingly slow with prepared statements
        }
        return supportsBatchUpdates(database);
    }

    protected boolean supportsBatchUpdates(Database database) {
        boolean databaseSupportsBatchUpdates = false;
        try {
            databaseSupportsBatchUpdates = database.supportsBatchUpdates();
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return databaseSupportsBatchUpdates;
    }

    private boolean isLoggingExecutor(Database database) {
        final ExecutorService executorService = Scope.getCurrentScope().getSingleton(ExecutorService.class);

        return executorService.executorExists("logging", database) &&
                (executorService.getExecutor("logging", database) instanceof LoggingExecutor);
    }

    /**
     * Iterate through the List of LoadDataColumnConfig and ask the database for any column types that we have
     * no data type of.
     *
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
        if (columnConfig.getHeader() != null && columnConfig.getIndex() != null) {
            validationErrors.addWarning("Since attribute 'header' is also defined, 'index' ignored for "
                    + validationErrors.getChangeName() + columnIDString);
        }
    }

    /**
     * Get the column using the name, index or the header
     *
     * @param name name or header of the column searched
     * @param idx  the index of the column searched
     * @return The column having the name or header equal to "name" or the index equal to idx
     * or a new LoadDataColumnConfig if not found
     */
    protected LoadDataColumnConfig columnConfigFromName(String name, Integer idx) {
        for (LoadDataColumnConfig c : this.columns) {
            if (name.equals(c.getName()) || name.equals(c.getHeader()) || idx.equals(c.getIndex())) {
                return c;
            }
        }
        if (null == StringUtil.trimToNull(name)) {
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
     */
    private void addColumnsFromHeaders(String[] headers) {
        int i = 0;
        for (String columnNameFromHeader : headers) {
            LoadDataColumnConfig loadDataColumnConfig = columnConfigFromName(columnNameFromHeader, i);
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

        Resource resource;
        if (getRelativeTo() == null) {
            resource = resourceAccessor.get(file);
        } else {
            resource = resourceAccessor.get(getRelativeTo()).resolveSibling(file);
        }
        if (!resource.exists()) {
            return null;
        }

        @SuppressWarnings("java:S2095") // SONAR want us to close the stream here, but it is only read by CSVReader outside this method.
        InputStream stream = resource.openInputStream();
        if (resource.getPath().toLowerCase().endsWith(".gz") && !(stream instanceof GZIPInputStream)) {
            stream = new GZIPInputStream(stream);
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
            relativeTo = getChangeSet().getChangeLog().getPhysicalFilePath();
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
            ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
            Resource resource;
            if (getRelativeTo() == null) {
                resource = resourceAccessor.getExisting(file);
            } else {
                resource = resourceAccessor.get(getRelativeTo()).resolveSibling(file);
            }

            stream = new EmptyLineAndCommentSkippingInputStream(resource.openInputStream(), commentLineStartsWith);
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


    protected SqlStatement[] generateStatementsFromRows(Database database, List<LoadDataRowConfig> rows) {
        List<SqlStatement> statements = new ArrayList<>();
        List<ExecutablePreparedStatementBase> preparedStatements = new ArrayList<>();

        for (LoadDataRowConfig row : rows) {
            List<LoadDataColumnConfig> columnsFromCsv = row.getColumns();
            if (row.needsPreparedStatement()) {
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
        }
        if (rows.stream().anyMatch(LoadDataRowConfig::needsPreparedStatement)) {
            // If we have only prepared statements and the database supports batching, let's roll
            if (supportsBatchUpdates(database) && !preparedStatements.isEmpty()) {
                if (database instanceof PostgresDatabase || database instanceof MySQLDatabase) {
                    // we don't do batch updates for Postgres but we still send as a prepared statement, see LB-744
                    // mysql supports batch updates, but the performance vs. the big insert is worse
                    return preparedStatements.toArray(SqlStatement.EMPTY_SQL_STATEMENT);
                } else {
                    return new SqlStatement[]{
                            new BatchDmlExecutablePreparedStatement(
                                    database, getCatalogName(), getSchemaName(),
                                    getTableName(), columns,
                                    getChangeSet(), Scope.getCurrentScope().getResourceAccessor(),
                                    preparedStatements)
                    };
                }
            } else {
                return statements.toArray(SqlStatement.EMPTY_SQL_STATEMENT);
            }
        } else {
            if (statements.isEmpty()) {
                // avoid returning unnecessary dummy statement
                return SqlStatement.EMPTY_SQL_STATEMENT;
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
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public enum LOAD_DATA_TYPE {
        BOOLEAN, NUMERIC, DATE, STRING, COMPUTED, SEQUENCE, BLOB, CLOB, SKIP, UUID, OTHER, UNKNOWN
    }

    protected static class LoadDataRowConfig {

        private final boolean needsPreparedStatement;
        private final List<LoadDataColumnConfig> columns;

        public LoadDataRowConfig(boolean needsPreparedStatement, List<LoadDataColumnConfig> columns) {
            this.needsPreparedStatement = needsPreparedStatement;
            this.columns = columns;
        }

        public boolean needsPreparedStatement() {
            return needsPreparedStatement;
        }

        public List<LoadDataColumnConfig> getColumns() {
            return columns;
        }
    }
}
