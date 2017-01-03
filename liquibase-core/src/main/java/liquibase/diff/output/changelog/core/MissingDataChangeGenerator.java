package liquibase.diff.output.changelog.core;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;

import org.apache.commons.io.IOUtils;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.datatype.core.BlobType;
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
import liquibase.util.JdbcUtils;

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

            List<Change> changes = new ArrayList<Change>();
            while (rs.next()) {
                InsertDataChange change = new InsertDataChange();
                if (outputControl.getIncludeCatalog()) {
                    change.setCatalogName(table.getSchema().getCatalogName());
                }
                if (outputControl.getIncludeSchema()) {
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
                    } else if (value instanceof Blob) {
                    	byte[] blob = IOUtils.toByteArray(((Blob) value).getBinaryStream());
                    	SerialBlob serialBlob = new SerialBlob(blob);
                    	column.setValueBlob(serialBlob);
                    } else if (value instanceof Clob) {
                    	char[] clob = IOUtils.toCharArray(((Clob)value).getCharacterStream());
                    	SerialClob serialClob = new SerialClob(clob);
                    	column.setValueClob(serialClob);
                    } else { // string
                        if (referenceDatabase instanceof InformixDatabase) {
                            if (value instanceof byte[]) {
                                byte[] bytes = (byte[]) value;
                                value = new String(bytes);
                            }
                        }

                        column.setValue(value.toString().replace("\\", "\\\\"));
                    }

                    change.addColumn(column);

                }

                // for each row, add a new change
                // (there will be one group per table)
                changes.add(change);
            }

            return changes.toArray(new Change[changes.size()]);
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
