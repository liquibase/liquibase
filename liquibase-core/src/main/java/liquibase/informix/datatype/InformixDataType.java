package liquibase.informix.datatype;

import liquibase.common.datatype.DataTypeWrapper;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BooleanType;
import liquibase.datatype.core.DateTimeType;
import liquibase.datatype.core.DoubleType;
import liquibase.informix.datatype.core.InformixBooleanType;
import liquibase.informix.datatype.core.InformixDateTimeType;
import liquibase.informix.datatype.core.InformixDoubleType;

/**
 * 
 * A wrapper class that is used for Informix database.
 * Wraps a LiquibaseDataType instance and replaces some of the
 * original data types with Informix-compatible ones.
 * 
 * @author Ivaylo Slavov
 */
public class InformixDataType extends DataTypeWrapper {

	public InformixDataType(LiquibaseDataType originalType) {
		super(extractOriginalType(originalType));		
	}
	

	private static LiquibaseDataType extractOriginalType(LiquibaseDataType originalType) {
		
		if (originalType instanceof BooleanType && !(originalType instanceof InformixBooleanType)) {
			
			originalType = new InformixBooleanType();

		} else if (originalType instanceof DoubleType && !(originalType instanceof InformixDoubleType)) {
			
			originalType = new InformixDoubleType();
			
		} else if (originalType instanceof DateTimeType && !(originalType instanceof InformixDateTimeType)) {
			
			originalType = new InformixDateTimeType();
			
		}
		
		// TODO: add checks for other incompatible informix data types when necessary.
			
		return originalType;
	}
}
