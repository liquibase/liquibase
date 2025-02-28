package liquibase.diff.output.changelog.core;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.core.LoadDataChange;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.OpenOptions;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.servicelocator.LiquibaseService;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Data;
import liquibase.structure.core.Table;
import liquibase.util.ISODateFormat;
import liquibase.util.JdbcUtil;
import liquibase.util.csv.CSVWriter;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@LiquibaseService(skip = true)
public class MissingDataExternalFileChangeGenerator extends MissingDataChangeGenerator {

    private final String dataDir;

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

    private Statement createStatement(Database database) throws Exception {
        if (! (database instanceof DB2Database)) {
            Statement stmt = ((JdbcConnection) database.getConnection()).createStatement(
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            return stmt;
        }
        Statement stmt = ((JdbcConnection) database.getConnection()).createStatement();
        return stmt;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl outputControl, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
    
        ResultSet rs = null;
        try (
            Statement stmt = createStatement(referenceDatabase);
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

            if (referenceDatabase instanceof DB2Database || rs.isBeforeFirst()) {
                List<String> columnNames = new ArrayList<>();
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    columnNames.add(rs.getMetaData().getColumnName(i + 1));
                }

                final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
                String fileName = table.getName().toLowerCase() + ".csv";
                Resource externalFileResource = pathHandlerFactory.getResource(fileName);
                if (dataDir != null) {
                    Resource dataDirResource = pathHandlerFactory.getResource(dataDir);
                    externalFileResource = dataDirResource.resolve(fileName);
                }

                String[] dataTypes = new String[0];
                try (
                        OutputStream fileOutputStream = externalFileResource.openOutputStream(new OpenOptions());
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                fileOutputStream, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
                        CSVWriter outputFile = new CSVWriter(new BufferedWriter(outputStreamWriter))
                ) {

                    dataTypes = new String[columnNames.size()];
                    String[] line = new String[columnNames.size()];
                    for (int i = 0; i < columnNames.size(); i++) {
                        line[i] = columnNames.get(i);
                    }
                    outputFile.writeNext(line);

                    int rowNum = 0;
                    if (rs.next()) {
                        do {
                            line = new String[columnNames.size()];

                            for (int i = 0; i < columnNames.size(); i++) {
                                Object value = JdbcUtil.getResultSetValue(rs, i + 1);
                                if ((dataTypes[i] == null) && (value != null)) {
                                    if (value instanceof Number) {
                                        dataTypes[i] = "NUMERIC";
                                    } else if (value instanceof Boolean) {
                                        dataTypes[i] = "BOOLEAN";
                                    } else if (value instanceof Date) {
                                        dataTypes[i] = "DATE";
                                    } else if (value instanceof byte[]) {
                                        dataTypes[i] = "BLOB";
                                    } else {
                                        dataTypes[i] = "STRING";
                                    }
                                }
                                if (value == null) {
                                    line[i] = "NULL";
                                } else {
                                    if (value instanceof Date) {
                                        line[i] = new ISODateFormat().format(((Date) value));
                                    } else if (value instanceof byte[]) {
                                        // extract the value as a Base64 string, to safely store the
                                        // binary data
                                        line[i] = Base64.getEncoder().encodeToString((byte[]) value);
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
                        } while (rs.next());
                    }
                    if (rowNum == 0) {
                        return Change.EMPTY_CHANGE;
                    }
                }

                LoadDataChange change = new LoadDataChange();
                change.setFile(externalFileResource.getPath());
                change.setEncoding(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
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
                    columnConfig.setType(dataTypes[i] != null ? dataTypes[i] : "skip");

                    change.addColumn(columnConfig);
                }
                return new Change[]{
                        change
                };
            }
            return Change.EMPTY_CHANGE;
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
