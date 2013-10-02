package liquibase.datatype.core;

import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="decimal", aliases = "java.sql.Types.DECIMAL" , minParameters = 0, maxParameters = 2, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DecimalType  extends LiquibaseDataType {
}
