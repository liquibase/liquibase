package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.LoadDataChange;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.LiquibaseService;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Data;
import liquibase.structure.core.Table;
import liquibase.util.ISODateFormat;
import liquibase.util.JdbcUtils;
import liquibase.util.csv.CSVWriter;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@LiquibaseService(skip = true)
public class MissingDataExternalFileChangeGenerator extends MissingDataChangeGenerator {

    private String dataDir;

    public MissingDataExternalFileChangeGenerator(String dataDir) {
        this.dataDir = dataDir;
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Data.class.isAssignableFrom(objectType)) {
            return PRIORITY_ADDITIONAL;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl outputControl, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
    
        ResultSet rs = null;
        try (
            Statement stmt = ((JdbcConnection) referenceDatabase.getConnection()).createStatement(
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        )
        {
            Data data = (Data) missingObject;

            Table table = data.getTable();
            if (referenceDatabase.isLiquibaseObject(table)) {
                return null;
            }

            String sql = "SELECT * FROM " + referenceDatabase.escapeTableName(table.getSchema().getCatalogName(), table.getSchema().getName(), table.getName());

            
            stmt.setFetchSize(100);
            rs = stmt.executeQuery(sql);

            List<String> columnNames = new ArrayList<>();
            for (int i=0; i< rs.getMetaData().getColumnCount(); i++) {
                columnNames.add(rs.getMetaData().getColumnName(i+1));
            }

            String fileName = table.getName().toLowerCase() + ".csv";
            if (dataDir != null) {
                fileName = dataDir + "/" + fileName;

                File parentDir = new File(dataDir);
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                if (!parentDir.isDirectory()) {
                    throw new IOException(parentDir.getAbsolutePath() +  " is not a valid directory");
                }
            }
    
            String[] dataTypes = new String[0];
            try (
                        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                             fileOutputStream,
                             LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class)
                             .getOutputEncoding()
                        );
                        CSVWriter outputFile = new CSVWriter(new BufferedWriter(outputStreamWriter));
            ) {
                
                dataTypes = new String[columnNames.size()];
                String[] line = new String[columnNames.size()];
                for (int i = 0; i < columnNames.size(); i++) {
                    line[i] = columnNames.get(i);
                }
                outputFile.writeNext(line);
        
                int rowNum = 0;
                while (rs.next()) {
                    line = new String[columnNames.size()];
    
                    for (int i = 0; i < columnNames.size(); i++) {
                        Object value = JdbcUtils.getResultSetValue(rs, i + 1);
                        if ((dataTypes[i] == null) && (value != null)) {
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
                    rowNum++;
                    if ((rowNum % 5000) == 0) {
                        outputFile.flush();
                    }
                }
            }
    
            LoadDataChange change = new LoadDataChange();
            change.setFile(fileName);
            change.setEncoding(LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding());
            if (outputControl.getIncludeCatalog()) {
                change.setCatalogName(table.getSchema().getCatalogName());
            }
            if (outputControl.getIncludeSchema()) {
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
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                    // nothing can be done
                } // try...
            } // rs == null?
        } // try... finally
    } // method fixMissing
} // class MissingDataExternalFileChangeGenerator
