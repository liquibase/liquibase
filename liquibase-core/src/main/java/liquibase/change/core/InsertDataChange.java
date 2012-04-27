package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.PreparedStatementFactory;
import liquibase.exception.DatabaseException;
import liquibase.statement.ExecutablePreparedStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Inserts data into an existing table.
 */
public class InsertDataChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;

    public InsertDataChange() {
        super("insert", "Insert Row", ChangeMetaData.PRIORITY_DEFAULT);
        columns = new ArrayList<ColumnConfig>();
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

    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        columns.remove(column);
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{ new ExecutableStatement(database) };
    }

    /**
     * @see liquibase.change.Change#getConfirmationMessage()
     */
    public String getConfirmationMessage() {
        return "New row inserted into " + getTableName();
    }


    /**
     * Handles INSERT Execution
     */
    public class ExecutableStatement implements ExecutablePreparedStatement {

        private Database database;

        public ExecutableStatement(Database database) {
            this.database = database;
        }

        public void execute(PreparedStatementFactory factory) throws DatabaseException {
            // build the sql statement
            String schema = getSchemaName() == null ? database.getDefaultSchemaName() : getSchemaName();
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            StringBuilder params = new StringBuilder("VALUES(");
            sql.append(database.escapeTableName(schema, getTableName()));
            sql.append("(");
            // list of columns which will have have values set as parameters
            List<ColumnConfig> cols = new ArrayList<ColumnConfig>(getColumns().size());
            for(ColumnConfig column : getColumns()) {
                if(database.supportsAutoIncrement()
                    && Boolean.TRUE.equals(column.isAutoIncrement())) {
                    // not adding column name or parameter
                    continue;
                }
                // add column name
                if( sql.charAt(sql.length()-1) != '(' ) {
                  sql.append(", ");
                }
                sql.append(database.escapeColumnName(schema, getTableName(), column.getName()));
                if( params.charAt(params.length()-1) != '(' ) {
                  params.append(", ");
                }
                if( column.getValueComputed() != null ) {
                  // valueComputed will be a SQL fragment - add as a non-parametrized value
                  params.append(column.getValueComputed());
                } else {
                  // all other column types will be added as a parametrized value
                  params.append("?");
                  cols.add(column);
                }
            }
            params.append(")");
            sql.append(") ");
            sql.append(params);

            // create prepared statement
            PreparedStatement stmt = factory.create(sql.toString());

            try {
                // attach params
                int i = 1;  // index starts from 1
                for(ColumnConfig col : cols) {
                    if(col.getValue() != null) {
                        stmt.setString(i, col.getValue());
                    } else if(col.getValueBoolean() != null) {
                        stmt.setBoolean(i, col.getValueBoolean());
                    } else if(col.getValueNumeric() != null) {
                        Number number = col.getValueNumeric();
                        if(number instanceof Long) {
                            stmt.setLong(i, number.longValue());
                        } else if(number instanceof Integer) {
                            stmt.setInt(i, number.intValue());
                        } else if(number instanceof Double) {
                            stmt.setDouble(i, number.doubleValue());
                        } else if(number instanceof Float) {
                            stmt.setFloat(i, number.floatValue());
                        } else if(number instanceof BigDecimal) {
                            stmt.setBigDecimal(i, (BigDecimal)number);
                        } else if(number instanceof BigInteger) {
                            stmt.setInt(i, number.intValue());
                        }
                    } else if(col.getValueDate() != null) {
                        stmt.setDate(i, new java.sql.Date(col.getValueDate().getTime()));
                    } else if(col.getValueBlob() != null) {
                        try {
                            File file = new File(col.getValueBlob());
                            stmt.setBinaryStream(i, new BufferedInputStream(new FileInputStream(file)), (int) file.length());
                        } catch (FileNotFoundException e) {
                            throw new DatabaseException(e.getMessage(), e); // wrap
                        }
                    } else if(col.getValueClob() != null) {
                        try {
                            File file = new File(col.getValueClob());
                            stmt.setCharacterStream(i, new BufferedReader(new FileReader(file)), (int) file.length());
                        } catch(FileNotFoundException e) {
                            throw new DatabaseException(e.getMessage(), e); // wrap
                        }
                    }
                    i++;
                }

                // trigger execution
                stmt.execute();
            } catch(SQLException e) {
                throw new DatabaseException(e);
            }
        }

        public boolean skipOnUnsupported() {
            return false;
        }
    }
}
