package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.DataType;

public class SybaseASATypeConverter extends DefaultTypeConverter {

    private static final DataType BOOLEAN_TYPE = new DataType("BIT", false);
    private static final DataType CLOB_TYPE = new DataType("LONG VARCHAR", false);
    private static final DataType CURRENCY_TYPE = new DataType("MONEY", false);
    private static final DataType DATETIME_TYPE = new DataType("DATETIME", false);
    private static final DataType UUID_TYPE = new DataType("UNIQUEIDENTIFIER", false);
    private static final DataType BLOB_TYPE = new DataType("LONG BINARY", false);


    @Override
    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
    public String getFalseBooleanValue() {
        return "0";
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#getBooleanType()
     */
    @Override
    public DataType getBooleanType() {

        return BOOLEAN_TYPE;
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#getClobType()
     */
    @Override
    public DataType getClobType() {
        return CLOB_TYPE;
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#getCurrencyType()
     */
    @Override
    public DataType getCurrencyType() {
        return CURRENCY_TYPE;
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#getDateTimeType()
     */
    @Override
    public DataType getDateTimeType() {
        return DATETIME_TYPE;
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#getUUIDType()
     */
    @Override
    public DataType getUUIDType() {
        return UUID_TYPE;
    }
    
    @Override
    public DataType getBlobType() {

        return BLOB_TYPE;
    }


    
}
