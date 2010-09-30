package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.structure.type.UUIDType;
import liquibase.exception.DatabaseException;

public class Postgres83TypeConverter extends PostgresTypeConverter {

    @Override
    public int getPriority() {
        return super.getPriority()+1;
    }

    @Override
    public boolean supports(Database database) {
        if (database==null || database.getConnection() == null) {
            return false;
        }
        try {
            return super.supports(database) && (database.getDatabaseMajorVersion() * 10 + database.getDatabaseMinorVersion() >= 83);
        } catch (DatabaseException e) {
            return false;
        }
    }

    @Override
    public UUIDType getUUIDType() {
        return new UUIDType("UUID");
    }

}
