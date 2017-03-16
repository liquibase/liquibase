package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="varbinary", aliases="java.sql.Types.VARBINARY", maxParameters = 0, minParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class VarBinaryType extends LiquibaseDataType{
	public DatabaseDataType toDatabaseDataType(Database database) {
		/*if ( database instanceof SybaseDatabase )
		{
			return new DatabaseDataType("VARBINARY");
		}*/
		return super.toDatabaseDataType(database);
	}
}