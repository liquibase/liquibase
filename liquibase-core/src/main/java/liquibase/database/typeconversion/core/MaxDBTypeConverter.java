package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.structure.type.BlobType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.CurrencyType;
import liquibase.database.structure.type.DateTimeType;

public class MaxDBTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof MaxDBDatabase;
    }

    @Override
    public String getTrueBooleanValue() {
        return "TRUE";
    }

    @Override
    public String getFalseBooleanValue() {
        return "FALSE";
    }

    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType() {
            @Override
            public String getDataTypeName() {
                return "NUMERIC(15, 2)";
            }
        };
    }

    @Override
    public ClobType getClobType() {
        return new ClobType() {
            @Override
            public String getDataTypeName() {
                return "LONG VARCHAR";
            }
        };
    }

    @Override
    public BlobType getBlobType() {
        return new BlobType() {
            @Override
            public String getDataTypeName() {
                return "LONG BYTE";
            }
        };
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType() {
            @Override
            public String getDataTypeName() {
                return "TIMESTAMP";
            }
        };
    }
}
