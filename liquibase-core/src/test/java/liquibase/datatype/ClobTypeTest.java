package liquibase.datatype;

import liquibase.Scope;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.GlobalConfiguration;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.core.ClobType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClobTypeTest {
    @Before
    public void prepare() {
        LiquibaseConfiguration.getInstance().reset();
    }

    @After
    public void reset() {
        LiquibaseConfiguration.getInstance().reset();
    }

    @Test
    public void mssqlTextToVarcharTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getProperty(), true, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("Text");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("varchar (max)", dbType.getType());
        });
    }

    @Test
    public void mssqlEscapedTextToVarcharTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getProperty(), true, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("[Text]");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("varchar (max)", dbType.getType());
        });
    }

    @Test
    public void mssqlTextToVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getProperty(), false, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("Text");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("varchar (max)", dbType.getType());
        });
    }

    @Test
    public void mssqlNTextToNVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getProperty(), false, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("NText");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("nvarchar (max)", dbType.getType());
        });
    }

    @Test
    public void mssqlEscapedTextToVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getProperty(), false, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("[Text]");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("varchar (max)", dbType.getType());
        });
    }

    @Test
    public void mssqlEscapedNTextToNVarcharNoConvertTest() throws Exception {
        Scope.child(GlobalConfiguration.CONVERT_DATA_TYPES.getProperty(), false, () -> {
            ClobType ct = new ClobType();
            ct.finishInitialization("[NText]");
            DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
            assertEquals("nvarchar (max)", dbType.getType());
        });
    }
}
