package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.ext.HanaDBDatabase;
import liquibase.database.structure.type.*;

public class HanaDBTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof HanaDBDatabase;
    }

    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType("DECIMAL(15, 2)");
    }

    @Override
    public ClobType getClobType() {
        return new ClobType("NCLOB");
    }

    @Override
    public BlobType getBlobType() {
        return new BlobType("BLOB");
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType("TIMESTAMP");
    }

    @Override
    public BooleanType getBooleanType() {
        return new BooleanType.NumericBooleanType("SMALLINT");
    }

    @Override
    public NumberType getNumberType() {
        return new NumberType("DECIMAL");
    }

}
