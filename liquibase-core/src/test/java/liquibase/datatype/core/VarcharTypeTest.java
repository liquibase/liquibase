package liquibase.datatype.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DatabaseDataType;

import org.junit.Test;

public class VarcharTypeTest {

    @Test
    public void varchar2ForHsqldbInOracleSyntaxMode() {
        VarcharType type = new VarcharType();
        HsqlDatabase hsqlDatabase = mock(HsqlDatabase.class);
        when(hsqlDatabase.isUsingOracleSyntax()).thenReturn(true);
        DatabaseDataType databaseDataType = type.toDatabaseDataType(hsqlDatabase);
        assertEquals("VARCHAR2", databaseDataType.getType().toUpperCase());
    }

    @Test
    public void varcharForHsqldbNotInOracleSyntaxMode() {
        VarcharType type = new VarcharType();
        HsqlDatabase hsqlDatabase = mock(HsqlDatabase.class);
        when(hsqlDatabase.isUsingOracleSyntax()).thenReturn(false);
        DatabaseDataType databaseDataType = type.toDatabaseDataType(hsqlDatabase);
        assertEquals("VARCHAR", databaseDataType.getType().toUpperCase());
    }

}
