package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;

/**
 * Oracle Database-specific parts of ColumnSnapshotGenerator
 */
public class ColumnSnapshotGeneratorOracle extends ColumnSnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof OracleDatabase)
            return PRIORITY_DATABASE;
        else
            return PRIORITY_NONE; // Other DB? Let the generic handler do it.
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[]{ColumnSnapshotGenerator.class};
    }

    @Override
    protected DataType readDataType(CachedRow columnMetadataResultSet, Column column, Database database) {

        String dataType = columnMetadataResultSet.getString("DATA_TYPE_NAME");
        dataType = dataType.replace("VARCHAR2", "VARCHAR");
        dataType = dataType.replace("NVARCHAR2", "NVARCHAR");

        DataType type = new DataType(dataType);
        type.setDataTypeId(columnMetadataResultSet.getInt("DATA_TYPE"));
        if ("NUMBER".equalsIgnoreCase(dataType)) {
            type.setColumnSize(columnMetadataResultSet.getInt("DATA_PRECISION"));
            type.setDecimalDigits(columnMetadataResultSet.getInt("DATA_SCALE"));

        } else {
            type.setColumnSize(columnMetadataResultSet.getInt("DATA_LENGTH"));

            if ("NCLOB".equalsIgnoreCase(dataType) || "BLOB".equalsIgnoreCase(dataType) || "CLOB".equalsIgnoreCase
                (dataType)) {
                type.setColumnSize(null);
            } else if ("NVARCHAR".equalsIgnoreCase(dataType) || "NCHAR".equalsIgnoreCase(dataType)) {
                type.setColumnSize(columnMetadataResultSet.getInt("CHAR_LENGTH"));
                type.setColumnSizeUnit(DataType.ColumnSizeUnit.CHAR);
            } else {
                String charUsed = columnMetadataResultSet.getString("CHAR_USED");
                DataType.ColumnSizeUnit unit = null;
                if ("C".equals(charUsed)) {
                    unit = DataType.ColumnSizeUnit.CHAR;
                    type.setColumnSize(columnMetadataResultSet.getInt("CHAR_LENGTH"));
                } else if ("B".equals(charUsed)) {
                    unit = DataType.ColumnSizeUnit.BYTE;
                }
                type.setColumnSizeUnit(unit);
            }
        }


        return type;
    }


}
