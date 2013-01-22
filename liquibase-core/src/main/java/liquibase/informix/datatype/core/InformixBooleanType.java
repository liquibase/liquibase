package liquibase.informix.datatype.core;

import liquibase.database.Database;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.core.BooleanType;

/**
 * 
 * An informix-compatible boolean data type.
 * 
 * @author Ivaylo Slavov
 */
public class InformixBooleanType extends BooleanType {


	@Override
	public liquibase.datatype.DatabaseDataType toDatabaseDataType(Database database) {
		return new DatabaseDataType("BOOLEAN");
	}

    @Override
    public String getFalseBooleanValue(Database database) {
        return "'F'";
    }

    @Override
    public String getTrueBooleanValue(Database database) {
        return "'T'";
    }
}
