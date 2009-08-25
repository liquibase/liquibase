package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.*;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.SybaseASADatabase;

public class SybaseASATypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof SybaseASADatabase;
    }


    @Override
    public BooleanType getBooleanType() {

        return new BooleanType.NumericBooleanType("BIT");
    }

    @Override
    public ClobType getClobType() {
        return new ClobType("LONG VARCHAR");
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#getCurrencyType()
     */
    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType("MONEY");

    }

    @Override
    public UUIDType getUUIDType() {
        return new UUIDType("UNIQUEIDENTIFIER");
    }
    
    @Override
    public BlobType getBlobType() {
        return new BlobType("LONG BINARY");
    }


    
}
