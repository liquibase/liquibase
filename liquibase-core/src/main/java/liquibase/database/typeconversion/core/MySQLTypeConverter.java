package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.BlobType;
import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.NumberType;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.DateTimeType;

public class MySQLTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof MySQLDatabase;
    }

    @Override
    public BooleanType getBooleanType() {
        return new BooleanType.NumericBooleanType("TINYINT(1)");
    }

    @Override
    public ClobType getClobType() {
        return new ClobType("LONGTEXT");
    }

    @Override
    public BlobType getLongBlobType() {
    	return new BlobType("LONGBLOB");
    }

    @Override
    public NumberType getNumberType() {
        return new NumberType("NUMERIC");
    }
    
    @Override
    protected DataType getDataType(String columnTypeString, Boolean autoIncrement, String dataTypeName, String precision, String additionalInformation) {
        if (columnTypeString.equalsIgnoreCase("timestamp")) {
            return new DateTimeType("TIMESTAMP");
        }
        return super.getDataType(columnTypeString, autoIncrement, dataTypeName, precision, additionalInformation);
    }
}
