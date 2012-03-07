package liquibase.datatype.core;

import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="number", aliases = {"numeric", "java.sql.Types.NUMERIC"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NumericType extends LiquibaseDataType {
}
