package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.CurrencyType;
import liquibase.database.structure.type.DateTimeType;
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
    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type = super.getColumnType(columnType, autoIncrement);
        if (type.startsWith("BLOB SUB_TYPE <0")) {
            return getBlobType().getDataTypeName();
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
}
