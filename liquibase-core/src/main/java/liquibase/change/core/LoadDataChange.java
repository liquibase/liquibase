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


public class LoadDataChange extends AbstractChange implements ChangeWithColumns {

    private String schemaName;
    private String tableName;
    private String file;
    private String encoding = null;
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

    public void addColumn(ColumnConfig column) {
      	columns.add((LoadDataColumnConfig) column);
    }

    public SqlStatement[] generateStatements(Database database) {
        try {
            CSVReader reader = getCSVReader();

            String[] headers = reader.readNext();
            if (headers == null) {
                throw new UnexpectedLiquibaseException("Data file "+getFile()+" was empty");
            }

            List<SqlStatement> statements = new ArrayList<SqlStatement>();
            String[] line = null;
            while ((line = reader.readNext()) != null) {
                InsertStatement insertStatement = this.createStatement(getSchemaName(), getTableName());
                for (int i=0; i<headers.length; i++) {
                    String columnName = null;
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
        }
    }

    protected CSVReader getCSVReader() throws IOException {
        ResourceAccessor opener = getFileOpener();
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

        CSVReader reader = new CSVReader(streamReader);
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
            stream = getFileOpener().getResourceAsStream(getFile());
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
