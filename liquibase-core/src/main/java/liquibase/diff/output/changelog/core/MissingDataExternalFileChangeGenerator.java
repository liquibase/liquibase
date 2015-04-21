package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.LoadDataChange;
import liquibase.change.core.LoadDataColumnConfig;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import liquibase.database.core.MySQLDatabase;

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
        
        int file_limit = 10000;
        
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Data data = (Data) missingObject;
            
            Table table = data.getTable();
            if (referenceDatabase.isLiquibaseObject(table)) {
                return null;
            }
            
            String sql = "SELECT COUNT(*) as counter FROM " + referenceDatabase.escapeTableName(table.getSchema().getCatalogName(), table.getSchema().getName(), table.getName());
            
            stmt = ((JdbcConnection) referenceDatabase.getConnection()).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            rs = stmt.executeQuery(sql);
            rs.next();
             int rowcount = 0;
             //String limit = "";
             int counter = rs.getInt("counter");
             int changecount = 1;
             
            if( counter > file_limit ){
                
                changecount = (counter/file_limit)+1;    
            
            }
            
             try {
                        rs.close();
             } catch (SQLException ignore) { }

             Change[] changes = new Change[changecount];
             
            for( int x=0; x<changecount; x++ ){ 
                
                sql = "SELECT * FROM " + referenceDatabase.escapeTableName(table.getSchema().getCatalogName(), table.getSchema().getName(), table.getName());

                if( referenceDatabase.getConnection().getURL().contains(":mysql:") ){
                    sql = "SELECT * FROM " + referenceDatabase.escapeTableName(table.getSchema().getCatalogName(), table.getSchema().getName(), table.getName())+" LIMIT "+Integer.toString(x*file_limit)+", "+Integer.toString( file_limit );
                }

                if( referenceDatabase.getConnection().getURL().contains(":oracle:") ){
                    System.out.println("OOPS, select query not written yet for oracle.");
                    System.exit(0);
                    //sql = "SELECT ROWNUM \"nums\","+referenceDatabase.escapeTableName(table.getSchema().getCatalogName(), table.getSchema().getName(), table.getName())+".* FROM " + referenceDatabase.escapeTableName(table.getSchema().getCatalogName(), table.getSchema().getName(), table.getName());
                    //sql = "SELECT * FROM ( "+sql+" ) WHERE '.self::limit( $count['from'], $count['count'] );
                }
                
                stmt = ((JdbcConnection) referenceDatabase.getConnection()).createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(Integer.MIN_VALUE);
                rs = stmt.executeQuery(sql);
                rowcount = 0;
                while (rs.next()) {
                    rowcount++;
                }

                if( rowcount > 0 ){

                        try {
                            rs.close();
                        } catch (SQLException ignore) { }

                        rs = stmt.executeQuery(sql);

                        List<String> columnNames = new ArrayList<String>();
                        for (int i=0; i< rs.getMetaData().getColumnCount(); i++) {
                            columnNames.add(rs.getMetaData().getColumnName(i+1));
                        }

                        String fileName = table.getName().toLowerCase()+"_"+Integer.toString(x) + ".csv";
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

                        int rowNum = 0;
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
                            rowNum++;
                            if (rowNum % 5000 == 0) {
                                outputFile.flush();
                            }
                        }
                        outputFile.flush();
                        outputFile.close();

                        LoadDataChange change = new LoadDataChange();
                        change.setFile(fileName);
                        change.setEncoding("UTF-8");
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

                        changes[x] = change;

                    }
            }
            
            if( counter > 0 ){
                return changes;
            }else{
                return null;
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
