package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.*;

public class SybaseASATypeConverter extends DefaultTypeConverter {

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
    public BooleanType getBooleanType() {

        return new BooleanType() {
            @Override
            public String getDataTypeName() {
                return "BIT";
            }
        };
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#getClobType()
     */
    @Override
    public ClobType getClobType() {
        return new ClobType() {
            @Override
            public String getDataTypeName() {
                return "LONG VARCHAR";
            }
        };
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#getCurrencyType()
     */
    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType() {
            @Override
            public String getDataTypeName() {
                return "MONEY";
            }
        };

    }

    @Override
    public UUIDType getUUIDType() {
        return new UUIDType() {
            @Override
            public String getDataTypeName() {
                return "UNIQUEIDENTIFIER";
            }
        };
    }
    
    @Override
    public BlobType getBlobType() {
        return new BlobType() {
            @Override
            public String getDataTypeName() {
                return "LONG BINARY";
            }
        };
    }


    
}
