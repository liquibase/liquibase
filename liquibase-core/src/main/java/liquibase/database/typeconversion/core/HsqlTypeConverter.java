package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.BlobType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.NVarcharType;
import liquibase.database.structure.type.NumberType;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;

public class HsqlTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof HsqlDatabase;
    }


    @Override
    public ClobType getClobType() {
        return new ClobType("LONGVARCHAR");
    }

    @Override
    public BlobType getBlobType() {
        return new BlobType("LONGVARBINARY");
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType("DATETIME");
    }
    @Override
    public NVarcharType getNVarcharType() {
        return new NVarcharType("VARCHAR");
    }
    @Override
    public NumberType getNumberType() {
        return new NumberType("NUMERIC");
    }
}
