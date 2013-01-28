package liquibase.cassandra;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;

public class CassandraDataTypeFactory extends DataTypeFactory {

	private static CassandraDataTypeFactory instance;

	private CassandraDataTypeFactory() {
		super();
	}

	@Deprecated
	public static synchronized CassandraDataTypeFactory getInstance() {
		if (instance == null) {
			instance = new CassandraDataTypeFactory();
		}
		return instance;
	}

	public static void reset() {
		instance = new CassandraDataTypeFactory();
	}

	@Override
	public LiquibaseDataType fromObject(Object object, Database database) {
		System.out.println("from object: "+object + " => "+super.fromObject(object, database));
		return super.fromObject(object, database);
	}

	public LiquibaseDataType fromDescription(String dataTypeDefinition) {
		System.out.println("from description: "+dataTypeDefinition);
		LiquibaseDataType dataType = super.fromDescription(dataTypeDefinition);

		return new CassandraDataType(dataType);
	}
}
