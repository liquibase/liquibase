package liquibase.datatype;

import liquibase.Scope;
import liquibase.GlobalConfiguration;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.core.ClobType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClobTypeTest {

    @Test
    public void mssqlTextToVarcharTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("Text");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("varchar (max)", dbType.getType());
        });
    }

    @Test
    public void mssqlEscapedTextToVarcharTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), true, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("[Text]");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("varchar (max)", dbType.getType());
        });
    }

    @Test
    public void mssqlTextToVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), false, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("Text");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("Text", dbType.getType());
        });
    }

    @Test
    public void mssqlNTextToNVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), false, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("NText");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("NText", dbType.getType());
        });
    }

    @Test
    public void mssqlEscapedTextToVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), false, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("[Text]");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("[Text]", dbType.getType());
        });
    }

    @Test
    public void mssqlEscapedNTextToNVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getKey(), false, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("[NText]");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("[NText]", dbType.getType());
        });
    }
}
