package liquibase.datatype;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import liquibase.database.core.H2Database;
import liquibase.datatype.core.CurrencyType;

public class CurrencyTypeTest {

  @Test
  public void h2CurrencyDatabaseTypeShouldProvidePrecisionAndScale() {
    CurrencyType currencyType = new CurrencyType();
    DatabaseDataType actualDatabaseType = currencyType.toDatabaseDataType(new H2Database());
    DatabaseDataType expectedDatabaseType = new DatabaseDataType("DECIMAL", 18, 4);
    assertEquals(expectedDatabaseType.getType(), actualDatabaseType.getType());
  }
}
