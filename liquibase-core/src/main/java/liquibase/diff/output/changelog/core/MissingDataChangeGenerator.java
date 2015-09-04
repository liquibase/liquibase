package liquibase.diff.output.changelog.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.LoadDataChange;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Data;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.ISODateFormat;
import liquibase.util.JdbcUtils;
import liquibase.util.csv.CSVWriter;

public class MissingDataChangeGenerator implements MissingObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Data.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[]{
                Table.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[]{
                PrimaryKey.class, ForeignKey.class, Index.class
        };
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl outputControl, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Data data = (Data) missingObject;

            Table table = data.getTable();
            if (referenceDatabase.isLiquibaseObject(table)) {
                return null;
            }

            String sql = "SELECT * FROM " + referenceDatabase.escapeTableName(table.getSchema().getCatalogName(), table.getSchema().getName(), table.getName());

            stmt = ((JdbcConnection) referenceDatabase.getConnection()).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(1000);
            rs = stmt.executeQuery(sql);

            List<String> columnNames = new ArrayList<String>();
            for (int i=0; i< rs.getMetaData().getColumnCount(); i++) {
                columnNames.add(rs.getMetaData().getColumnName(i+1));
            }

            // if dataDir is not null, print out a csv file and use loadData
            // tag
            String dataDir = outputControl.getDataDir();
            if (dataDir != null) {
                String fileName = table.getName().toLowerCase() + ".csv";
                if (dataDir != null) {
                    fileName = dataDir + "/" + fileName;
                }

                File parentDir = new File(dataDir);
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                if (!parentDir.isDirectory()) {
                    throw new RuntimeException(parentDir
                            + " is not a directory");
                }

                CSVWriter outputFile = new CSVWriter(new BufferedWriter(new FileWriter(fileName)));
                String[] dataTypes = new String[columnNames.size()];
                String[] line = new String[columnNames.size()];
                for (int i = 0; i < columnNames.size(); i++) {
                    line[i] = columnNames.get(i);
                }
                outputFile.writeNext(line);

                while (rs.next()) {
                    line = new String[columnNames.size()];

                    for (int i = 0; i < columnNames.size(); i++) {
                        Object value = JdbcUtils.getResultSetValue(rs, i + 1);
                        if (dataTypes[i] == null && value != null) {
                            if (value instanceof Number) {
                                dataTypes[i] = "NUMERIC";
                            } else if (value instanceof Boolean) {
                                dataTypes[i] = "BOOLEAN";
                            } else if (value instanceof Date) {
                                dataTypes[i] = "DATE";
                            } else {
                                dataTypes[i] = "STRING";
                            }
                        }
                        if (value == null) {
                            line[i] = "NULL";
                        } else {
                            if (value instanceof Date) {
                                line[i] = new ISODateFormat().format(((Date) value));
                            } else {
                                line[i] = value.toString();
                            }
                        }
                    }
                    outputFile.writeNext(line);
                }
                outputFile.flush();
                outputFile.close();

                LoadDataChange change = new LoadDataChange();
                change.setFile(fileName);
                change.setEncoding("UTF-8");
                if (outputControl.isIncludeCatalog()) {
                    change.setCatalogName(table.getSchema().getCatalogName());
                }
                if (outputControl.isIncludeSchema()) {
                    change.setSchemaName(table.getSchema().getName());
                }
                change.setTableName(table.getName());

                for (int i = 0; i < columnNames.size(); i++) {
                    String colName = columnNames.get(i);
                    LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
                    columnConfig.setHeader(colName);
                    columnConfig.setName(colName);
                    columnConfig.setType(dataTypes[i]);

                    change.addColumn(columnConfig);
                }

                return new Change[]{
                        change
                };
            } else { // if dataDir is null, build and use insert tags
                List<Change> changes = new ArrayList<Change>();
                while (rs.next()) {
                    InsertDataChange change = new InsertDataChange();
                    if (outputControl.isIncludeCatalog()) {
                        change.setCatalogName(table.getSchema().getCatalogName());
                    }
                    if (outputControl.isIncludeSchema()) {
                        change.setSchemaName(table.getSchema().getName());
                    }
                    change.setTableName(table.getName());

                    // loop over all columns for this row
                    for (int i = 0; i < columnNames.size(); i++) {
                        ColumnConfig column = new ColumnConfig();
                        column.setName(columnNames.get(i));

                        Object value = JdbcUtils.getResultSetValue(rs, i + 1);
                        if (value == null) {
                            column.setValue(null);
                        } else if (value instanceof Number) {
                            column.setValueNumeric((Number) value);
                        } else if (value instanceof Boolean) {
                            column.setValueBoolean((Boolean) value);
                        } else if (value instanceof Date) {
                            column.setValueDate((Date) value);
                        } else { // string
                            column.setValue(value.toString().replace("\\", "\\\\"));
                        }

                        change.addColumn(column);

                    }

                    // for each row, add a new change
                    // (there will be one group per table)
                    changes.add(change);
                }

                return changes.toArray(new Change[changes.size()]);

            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) { }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) { }
            }
        }
    }
}
