package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.BlobType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.DateTimeType;

public class HsqlTypeConverter extends DefaultTypeConverter {

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
