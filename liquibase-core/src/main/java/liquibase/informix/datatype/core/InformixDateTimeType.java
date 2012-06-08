package liquibase.informix.datatype.core;

import liquibase.database.Database;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.core.DateTimeType;

/**
 * 
 * An informix-compatible datetime data type.
 * 
 * @author Ivaylo Slavov
 */
public class InformixDateTimeType extends DateTimeType {

	@Override
	public DatabaseDataType toDatabaseDataType(Database database) {

        return new DatabaseDataType("DATETIME YEAR TO FRACTION", 5);
	}
	
	@Override
	public Object stringToObject(String value, Database database) {
		return value;
	}
}
