package liquibase.datatype;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.core.ClobType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClobTypeTest {
    @Before
    public void prepare() {
        LiquibaseConfiguration.getInstance().reset();
    }

    @Test
    public void mssqlTextToVarcharTest() {
        LiquibaseConfiguration.getInstance()
                .getConfiguration(GlobalConfiguration.class)
                .getProperty(GlobalConfiguration.CONVERT_DATA_TYPES)
                .setValue(Boolean.TRUE);

        ClobType ct = new ClobType();
        ct.finishInitialization("Text");
        DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
        assertEquals("varchar (max)", dbType.getType());
    }

    @Test
    public void mssqlEscapedTextToVarcharTest() {
        LiquibaseConfiguration.getInstance()
                .getConfiguration(GlobalConfiguration.class)
                .getProperty(GlobalConfiguration.CONVERT_DATA_TYPES)
                .setValue(Boolean.TRUE);

        ClobType ct = new ClobType();
        ct.finishInitialization("[Text]");
        DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
        assertEquals("varchar (max)", dbType.getType());
    }

    @Test
    public void mssqlTextToVarcharNoConvertTest() {
        LiquibaseConfiguration.getInstance()
                .getConfiguration(GlobalConfiguration.class)
                .getProperty(GlobalConfiguration.CONVERT_DATA_TYPES)
                .setValue(Boolean.FALSE);

        ClobType ct = new ClobType();
        ct.finishInitialization("Text");
        DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
        assertEquals("varchar (max)", dbType.getType());
    }

    @Test
    public void mssqlNTextToNVarcharNoConvertTest() {
        LiquibaseConfiguration.getInstance()
                .getConfiguration(GlobalConfiguration.class)
                .getProperty(GlobalConfiguration.CONVERT_DATA_TYPES)
                .setValue(Boolean.FALSE);

        ClobType ct = new ClobType();
        ct.finishInitialization("NText");
        DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
        assertEquals("nvarchar (max)", dbType.getType());
    }

    @Test
    public void mssqlEscapedTextToVarcharNoConvertTest() {
        LiquibaseConfiguration.getInstance()
                .getConfiguration(GlobalConfiguration.class)
                .getProperty(GlobalConfiguration.CONVERT_DATA_TYPES)
                .setValue(Boolean.FALSE);

        ClobType ct = new ClobType();
        ct.finishInitialization("[Text]");
        DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
        assertEquals("varchar (max)", dbType.getType());
    }

    @Test
    public void mssqlEscapedNTextToNVarcharNoConvertTest() {
        LiquibaseConfiguration.getInstance()
                .getConfiguration(GlobalConfiguration.class)
                .getProperty(GlobalConfiguration.CONVERT_DATA_TYPES)
                .setValue(Boolean.FALSE);

        ClobType ct = new ClobType();
        ct.finishInitialization("[NText]");
        DatabaseDataType dbType = ct.toDatabaseDataType(new MSSQLDatabase());
        assertEquals("nvarchar (max)", dbType.getType());
    }
}
