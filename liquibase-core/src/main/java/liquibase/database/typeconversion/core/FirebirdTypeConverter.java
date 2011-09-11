package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.*;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;

public class FirebirdTypeConverter  extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof FirebirdDatabase;
    }

    @Override
    public DataType getDataType(String columnTypeString, Boolean autoIncrement) {
        DataType type = super.getDataType(columnTypeString, autoIncrement);
        if (type.getDataTypeName().startsWith("BLOB SUB_TYPE <0")) {
            return getBlobType();
        } else {
            return type;
        }
    }

    @Override
    public BooleanType getBooleanType() {
        return new BooleanType.NumericBooleanType("SMALLINT");
    }

    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType("DECIMAL(18, 4)");
    }

    @Override
    public ClobType getClobType() {
        return new ClobType("BLOB SUB_TYPE TEXT");
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType("TIMESTAMP");
    }

    @Override
    public NumberType getNumberType() {
        return new NumericType();
    }
}
