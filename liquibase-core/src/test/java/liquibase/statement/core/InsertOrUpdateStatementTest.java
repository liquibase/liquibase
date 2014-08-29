package liquibase.statement.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class InsertOrUpdateStatementTest extends InsertStatementTest {

    @Test
    public void setPrimaryKey(){
        String primaryKey = "PRIMARYKEY";
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("CATALOG", "SCHEMA","TABLE", primaryKey);
        assertEquals(primaryKey,statement.getPrimaryKey());
        assertEquals(Boolean.FALSE,statement.getUpdateOnly());
    }

    @Test
    public void setUpdateOnly(){
        String primaryKey = "PRIMARYKEY";
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("CATALOG", "SCHEMA","TABLE", primaryKey, true);
        assertEquals(Boolean.TRUE,statement.getUpdateOnly());
    }
}
