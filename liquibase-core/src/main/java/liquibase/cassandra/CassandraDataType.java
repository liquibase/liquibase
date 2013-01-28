package liquibase.cassandra;

import liquibase.common.datatype.DataTypeWrapper;
import liquibase.datatype.LiquibaseDataType;

public class CassandraDataType extends DataTypeWrapper {

	public CassandraDataType(LiquibaseDataType originalType) {
		super(extractOriginalType(originalType));
	}

	private static LiquibaseDataType extractOriginalType(LiquibaseDataType originalType) {
		return originalType;
	}
}
