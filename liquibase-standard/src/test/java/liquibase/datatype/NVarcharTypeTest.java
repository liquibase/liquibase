package liquibase.datatype;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.core.ClobType;
import liquibase.datatype.core.NVarcharType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NVarcharTypeTest {

    @Test
    public void mssqlNTextToNVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), false, () -> {
            NVarcharType nt = new NVarcharType();
            nt.finishInitialization("NText");
            DatabaseDataType dbType = nt.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("NText", dbType.getType());
        });
    }

    @Test
    public void mssqlEscapedNTextToNVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), false, () -> {
            NVarcharType nt = new NVarcharType();
            nt.finishInitialization("[NText]");
            DatabaseDataType dbType = nt.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("[NText]", dbType.getType());
        });
    }
}
