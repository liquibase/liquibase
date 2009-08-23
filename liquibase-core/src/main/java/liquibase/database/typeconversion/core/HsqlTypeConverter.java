package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.BlobType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.DateTimeType;
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
        return new ClobType() {
            @Override
            public String getDataTypeName() {
                return "LONGVARCHAR";
            }
        };
    }

    @Override
    public BlobType getBlobType() {
        return new BlobType() {
            @Override
            public String getDataTypeName() {
                return "LONGVARBINARY";
            }
        };
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType() {
            @Override
            public String getDataTypeName() {
                return "DATETIME";
            }
        };
    }

    /**
     * @see http://hsqldb.org/doc/guide/ch02.html#N1045F
     */
    @Override
    public String getTrueBooleanValue() {
        return "TRUE";
    }

    /**
     * @see http://hsqldb.org/doc/guide/ch02.html#N1045F
     */
    @Override
    public String getFalseBooleanValue() {
        return "FALSE";
    }
}
