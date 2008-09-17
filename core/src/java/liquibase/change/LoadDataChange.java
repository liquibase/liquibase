package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.InsertStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.csv.CSVReader;
import liquibase.util.MD5Util;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class LoadDataChange extends AbstractChange implements ChangeWithColumns {

    private String schemaName;
    private String tableName;
    private String file;
    private String encoding = null;
    private List<LoadDataColumnConfig> columns = new ArrayList<LoadDataColumnConfig>();


    public LoadDataChange() {
        super("loadData", "Load Data");
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

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(tableName) == null) {
            throw new InvalidChangeDefinitionException("tableName is required", this);
        }

    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        try {
            InputStream stream = getFileOpener().getResourceAsStream(getFile());
            if (stream == null) {
                throw new UnsupportedChangeException("Data file "+getFile()+" was not found");
            }

            InputStreamReader streamReader;
            if (getEncoding() == null) {
                streamReader = new InputStreamReader(stream);
            } else {
                streamReader = new InputStreamReader(stream, getEncoding());
            }
            
            CSVReader reader = new CSVReader(streamReader);
            String[] headers = reader.readNext();
            if (headers == null) {
                throw new UnsupportedChangeException("Data file "+getFile()+" was empty");
            }

            List<SqlStatement> statements = new ArrayList<SqlStatement>();
            String[] line = null;
            while ((line = reader.readNext()) != null) {
                InsertStatement insertStatement = new InsertStatement(getSchemaName(), getTableName());
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
                            } else if (columnConfig.getType().equalsIgnoreCase("DATE")) {
                                valueConfig.setValueDate(value.toString());
                            } else if (columnConfig.getType().equalsIgnoreCase("STRING")) {
                                valueConfig.setValue(value.toString());
                            } else {
                                throw new UnsupportedChangeException("loadData type of "+columnConfig.getType()+" is not supported.  Please use BOOLEAN, NUMERIC, DATE, or STRING");
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

    private ColumnConfig getColumnConfig(int index, String header) {
        for (LoadDataColumnConfig config : columns) {
            if (config.getIndex() != null && config.getIndex().equals(index)) {
                return config;
            }
            if (config.getHeader() != null && config.getHeader().equals(header)) {
                return config;
            }
        }
        return null;
    }

    public String getConfirmationMessage() {
        return "Data loaded from "+getFile()+" into "+getTableName();
    }

    public Element createNode(Document currentChangeLogDOM) {
        Element node = currentChangeLogDOM.createElement("loadData");
        if (getSchemaName() != null) {
            node.setAttribute("schemaName", getSchemaName());
        }

        node.setAttribute("file", getFile());
        node.setAttribute("tableName", getTableName());

        if (getEncoding() != null) {
            node.setAttribute("encoding", getEncoding());
        }

        for (LoadDataColumnConfig column : columns) {
            node.appendChild(column.createNode(currentChangeLogDOM));
        }

        return node;

    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }

    public String getMD5Sum() {
        InputStream stream = null;
        try {
            stream = getFileOpener().getResourceAsStream(getFile());
            if (stream == null) {
                throw new RuntimeException(getFile() + " could not be found");
            }
            return MD5Util.computeMD5(stream);
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
