package liquibase.datatype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import liquibase.datatype.core.IntType;
import liquibase.datatype.core.VarcharType;

public class DataTypeFactoryTest {
    @Test
    public void parse() throws Exception {
        assertParseCorrect("int", IntType.class);
        assertParseCorrect("varchar(255)", VarcharType.class);
        assertParseCorrect("int{autoIncrement:true}", "int", IntType.class);
        assertParseCorrect("int{}", "int", IntType.class);
        assertParseCorrect("varchar COLLATE Latin1_General_BIN", VarcharType.class);
        assertParseCorrect("varchar(255) COLLATE Latin1_General_BIN", VarcharType.class);

        assertTrue(((IntType) DataTypeFactory.getInstance().fromDescription("int{autoIncrement:true}")).isAutoIncrement());
        assertFalse(((IntType) DataTypeFactory.getInstance().fromDescription("int{autoIncrement:false}")).isAutoIncrement());
        assertFalse(((IntType) DataTypeFactory.getInstance().fromDescription("int")).isAutoIncrement());
        assertFalse(((IntType) DataTypeFactory.getInstance().fromDescription("int{}")).isAutoIncrement());
    }

    private void assertParseCorrect(String liquibaseString, String databaseString, Class<? extends LiquibaseDataType> expectedType) {
        LiquibaseDataType parsed = DataTypeFactory.getInstance().fromDescription(liquibaseString);
        assertEquals(expectedType.getName(), parsed.getClass().getName());
        assertEquals(databaseString, parsed.toString());
    }
    
    private void assertParseCorrect(String type, Class<? extends LiquibaseDataType> expectedType) {
        assertParseCorrect(type, type, expectedType);
    }
}
