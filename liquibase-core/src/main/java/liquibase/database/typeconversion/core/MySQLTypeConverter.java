package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.MySQLDatabase;

public class MySQLTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof MySQLDatabase;
    }


    @Override
    public BooleanType getBooleanType() {
        return new BooleanType.NumericBooleanType("TINYINT(1)");
    }

    @Override
    public ClobType getClobType() {
        return new ClobType("TEXT");
    }
}
