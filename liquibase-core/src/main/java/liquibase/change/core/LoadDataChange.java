package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;
import liquibase.util.csv.CSVReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


@DatabaseChange(name="loadData",
        description = "Loads data from a CSV file into an existing table. A value of NULL in a cell will be converted to a database NULL rather than the string “NULL”\n" +
                "\n" +
                "Date/Time values included in the CSV file should be in ISO formathttp://en.wikipedia.org/wiki/ISO_8601 in order to be parsed correctly by Liquibase. Liquibase will initially set the date format to be “yyyy-MM-dd'T'HH:mm:ss” and then it checks for two special cases which will override the data format string.\n" +
                "\n" +
                "If the string representing the date/time includes a ”.”, then the date format is changed to “yyyy-MM-dd'T'HH:mm:ss.SSS”\n" +
                "If the string representing the date/time includes a space, then the date format is changed to “yyyy-MM-dd HH:mm:ss”\n" +
                "Once the date format string is set, Liquibase will then call the SimpleDateFormat.parse() method attempting to parse the input string so that it can return a Date/Time. If problems occur, then a ParseException is thrown and the input string is treated as a String for the INSERT command to be generated.",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table",
        since="1.7")
public class LoadDataChange extends AbstractChange implements ChangeWithColumns<LoadDataColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String file;
    private String encoding = null;
    private String separator = liquibase.util.csv.opencsv.CSVReader.DEFAULT_SEPARATOR + "";
	private String quotchar = liquibase.util.csv.opencsv.CSVReader.DEFAULT_QUOTE_CHARACTER + "";


    private List<LoadDataColumnConfig> columns = new ArrayList<LoadDataColumnConfig>();

    @Override
    public boolean supports(Database database) {
        return true;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting = "table", description = "Name of the table to insert data into")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", description = "CSV file to load", exampleValue = "com/example/users.csv")
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @DatabaseChangeProperty(exampleValue = "UTF-8", description = "Encoding of the CSV file (defaults to UTF-8)")
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getQuotchar() {
		return quotchar;
	}

	public void setQuotchar(String quotchar) {
		this.quotchar = quotchar;
	}

	public void addColumn(LoadDataColumnConfig column) {
      	columns.add(column);
    }

    @DatabaseChangeProperty(description = "Defines how the data should be loaded.")
    public List<LoadDataColumnConfig> getColumns() {
        return columns;
    }

    public SqlStatement[] generateStatements(Database database) {
        CSVReader reader = null;
        try {
            reader = getCSVReader();

            String[] headers = reader.readNext();
            if (headers == null) {
                throw new UnexpectedLiquibaseException("Data file "+getFile()+" was empty");
            }

            List<SqlStatement> statements = new ArrayList<SqlStatement>();
            String[] line;
            int lineNumber = 0;

            while ((line = reader.readNext()) != null) {
                lineNumber++;

                if (line.length == 0 || (line.length == 1 && StringUtils.trimToNull(line[0]) == null)) {
                    continue; //nothing on this line
                }
                InsertStatement insertStatement = this.createStatement(getCatalogName(), getSchemaName(), getTableName());
                for (int i=0; i<headers.length; i++) {
                    String columnName = null;
                    if( i >= line.length ) {
                      throw new UnexpectedLiquibaseException("CSV Line " + lineNumber + " has only " + (i-1) + " columns, the header has " + headers.length);
                    }

                    Object value = line[i];

                    ColumnConfig columnConfig = getColumnConfig(i, headers[i]);
                    if (columnConfig != null) {
                        columnName = columnConfig.getName();

                        if ("skip".equalsIgnoreCase(columnConfig.getType())) {
                            continue;
                        }

                        if (value.toString().equalsIgnoreCase("NULL")) {
                            value = "NULL";
                        } else if (columnConfig.getType() != null) {
                            ColumnConfig valueConfig = new ColumnConfig();
                            if (columnConfig.getType().equalsIgnoreCase("BOOLEAN")) {
                                valueConfig.setValueBoolean(Boolean.parseBoolean(value.toString().toLowerCase()));
                            } else if (columnConfig.getType().equalsIgnoreCase("NUMERIC")) {
                                valueConfig.setValueNumeric(value.toString());
                            } else if (columnConfig.getType().toLowerCase().contains("date") ||columnConfig.getType().toLowerCase().contains("time")) {
                                valueConfig.setValueDate(value.toString());
                            } else if (columnConfig.getType().equalsIgnoreCase("STRING")) {
                                valueConfig.setValue(value.toString());
                            } else if (columnConfig.getType().equalsIgnoreCase("COMPUTED")) {
                                valueConfig.setValue(value.toString());
                            } else {
                                throw new UnexpectedLiquibaseException("loadData type of "+columnConfig.getType()+" is not supported.  Please use BOOLEAN, NUMERIC, DATE, STRING, COMPUTED or SKIP");
                            }
                            value = valueConfig.getValueObject();
                        }
                    }

                    if (columnName == null) {
                        columnName = headers[i];
                    }


                    insertStatement.addColumnValue(columnName, value);
                }
                statements.add(insertStatement);
            }

            return statements.toArray(new SqlStatement[statements.size()]);
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

    protected CSVReader getCSVReader() throws IOException {
        ResourceAccessor opener = getResourceAccessor();
        if (opener == null) {
            throw new UnexpectedLiquibaseException("No file opener specified for "+getFile());
        }
        InputStream stream = opener.getResourceAsStream(getFile());
        if (stream == null) {
            throw new UnexpectedLiquibaseException("Data file "+getFile()+" was not found");
        }

        InputStreamReader streamReader;
        if (getEncoding() == null) {
            streamReader = new InputStreamReader(stream);
        } else {
            streamReader = new InputStreamReader(stream, getEncoding());
        }

        char quotchar;
        if (0 == this.quotchar.length() ) {
        	// hope this is impossible to have a field surrounded with non ascii char 0x01
        	quotchar = '\1';
        } else {
        	quotchar = this.quotchar.charAt(0);
        }

        return new CSVReader(streamReader, separator.charAt(0), quotchar );
    }

    protected InsertStatement createStatement(String catalogName, String schemaName, String tableName){
        return new InsertStatement(catalogName, schemaName,tableName);
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

    public String getConfirmationMessage() {
        return "Data loaded from "+getFile()+" into "+getTableName();
    }

    @Override
    public CheckSum generateCheckSum() {
        InputStream stream = null;
        try {
            stream = getResourceAccessor().getResourceAsStream(getFile());
            if (stream == null) {
                throw new RuntimeException(getFile() + " could not be found");
            }
            stream = new BufferedInputStream(stream);
            return CheckSum.compute(stream, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }
}
