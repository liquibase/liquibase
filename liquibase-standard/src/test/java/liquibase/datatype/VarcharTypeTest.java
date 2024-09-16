package liquibase.datatype;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.core.ClobType;
import liquibase.datatype.core.VarcharType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VarcharTypeTest {
    @Test
    public void mssqlTextToVarcharTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            VarcharType vt = new VarcharType();
            vt.finishInitialization("Text");
            DatabaseDataType dbType = vt.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("varchar (max)", dbType.getType());
        });
    }

    @Test
    public void mssqlEscapedTextToVarcharTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            VarcharType vt = new VarcharType();
            vt.finishInitialization("[Text]");
            DatabaseDataType dbType = vt.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("varchar (max)", dbType.getType());
        });
    }

    @Test
    public void mssqlTextToVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), false, () -> {
            VarcharType vt = new VarcharType();
            vt.finishInitialization("Text");
            DatabaseDataType dbType = vt.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("Text", dbType.getType());
        });
    }

    @Test
    public void mssqlEscapedTextToVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), false, () -> {
            VarcharType vt = new VarcharType();
            vt.finishInitialization("[Text]");
            DatabaseDataType dbType = vt.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("[Text]", dbType.getType());
        });
    }
}
