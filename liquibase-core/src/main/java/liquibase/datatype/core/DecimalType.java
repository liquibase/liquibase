package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name = "decimal", aliases = { "java.sql.Types.DECIMAL", "java.math.BigDecimal" }, minParameters = 0, maxParameters = 2, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DecimalType  extends LiquibaseDataType {

    private boolean autoIncrement;

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

  @Override
  public DatabaseDataType toDatabaseDataType(Database database) {
    if (database instanceof InformixDatabase) {

      if(getParameters() != null && getParameters().length == 2) {

        // Don't use 255 as a scale because it is invalid, 
        // use only 1 argument in this special case
        if("255".equals(String.valueOf(getParameters()[1]))) {
          return new DatabaseDataType(getName(), getParameters()[0]);
        }
      }
    }

    return super.toDatabaseDataType(database);
  }

}
