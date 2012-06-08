package liquibase.informix.datatype.core;

import liquibase.database.Database;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.core.DoubleType;

/**
 * 
 * An informix-compatible double data type.
 * 
 * @author Ivaylo Slavov
 */
public class InformixDoubleType  extends DoubleType {
    
	@Override
	public DatabaseDataType toDatabaseDataType(Database database) {
		return new DatabaseDataType("DOUBLE PRECISION");
	}
}
