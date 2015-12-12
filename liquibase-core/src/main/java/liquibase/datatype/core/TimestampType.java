package liquibase.datatype.core;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtils;

@DataTypeInfo(name = "timestamp", aliases = { "java.sql.Types.TIMESTAMP", "java.sql.Timestamp", "timestamptz" }, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class TimestampType extends DateTimeType {

	@Override
	public DatabaseDataType toDatabaseDataType(Database database) {
		String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
		if (database instanceof MySQLDatabase) {
			if (getRawDefinition().contains(" ") || getRawDefinition().contains("(")) {
				return new DatabaseDataType(getRawDefinition());
			}
			return super.toDatabaseDataType(database);
		}
		if (database instanceof MSSQLDatabase) {
			if (!LiquibaseConfiguration.getInstance()
					.getProperty(GlobalConfiguration.class, GlobalConfiguration.CONVERT_DATA_TYPES)
					.getValue(Boolean.class)
					&& originalDefinition.toLowerCase().startsWith("timestamp")) {
				return new DatabaseDataType(database.escapeDataTypeName("timestamp"));
			}

			return new DatabaseDataType(database.escapeDataTypeName("datetime"));
		}
		if (database instanceof SybaseDatabase) {
			return new DatabaseDataType(database.escapeDataTypeName("datetime"));
		}
		return super.toDatabaseDataType(database);
	}
}
