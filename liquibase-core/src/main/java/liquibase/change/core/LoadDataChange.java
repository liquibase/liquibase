package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.util.StringUtils;
import liquibase.util.csv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class LoadDataChange extends AbstractChange implements ChangeWithColumns<LoadDataColumnConfig> {

    private String schemaName;
    private String tableName;
    private String file;
    private String encoding = null;
    private String separator = liquibase.util.csv.opencsv.CSVReader.DEFAULT_SEPARATOR + "";
	private String quotchar = liquibase.util.csv.opencsv.CSVReader.DEFAULT_QUOTE_CHARACTER + "";


    private List<LoadDataColumnConfig> columns = new ArrayList<LoadDataColumnConfig>();


    public LoadDataChange() {
        super("loadData", "Load Data", ChangeMetaData.PRIORITY_DEFAULT);
    }

    protected LoadDataChange(String changeName, String changeDescription)
    {
        super(changeName,changeDescription,ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

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

    public List<LoadDataColumnConfig> getColumns() {
        return (List<LoadDataColumnConfig>) columns;
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
            String[] line = null;
            int lineNumber = 0;

            while ((line = reader.readNext()) != null) {
                lineNumber++;

                if (line.length == 0 || (line.length == 1 && StringUtils.trimToNull(line[0]) == null)) {
                    continue; //nothing on this line
                }
                InsertStatement insertStatement = this.createStatement(getSchemaName(), getTableName());
                for (int i=0; i<headers.length; i++) {
                    String columnName = null;
                    if( i >= line.length ) {
                      throw new UnexpectedLiquibaseException("CSV Line " + lineNumber + " has only " + (i-1) + " columns, the header has " + headers.length);
                    }

                    Object value = line[i];

                    ColumnConfig columnConfig = getColumnConfig(i, headers[i]);
                    if (columnConfig != null) {
                        columnName = columnConfig.getName();

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
                                throw new UnexpectedLiquibaseException("loadData type of "+columnConfig.getType()+" is not supported.  Please use BOOLEAN, NUMERIC, DATE, STRING, or COMPUTED");
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

        CSVReader reader = new CSVReader(streamReader, separator.charAt(0), quotchar );

        return reader;
    }

    protected InsertStatement createStatement(String schemaName, String tableName){
        return new InsertStatement(schemaName,tableName);
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
            return CheckSum.compute(stream);
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
