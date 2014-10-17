package liquibase.statement.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class InsertOrUpdateStatementTest extends InsertStatementTest {

    @Test
    public void setPrimaryKey(){
        String primaryKey = "PRIMARYKEY";
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("CATALOG", "SCHEMA","TABLE", primaryKey);
        assertEquals(primaryKey,statement.getPrimaryKey());
        assertEquals(Boolean.FALSE,statement.getOnlyUpdate());
    }

    @Test
    public void setOnlyUpdate(){
        String primaryKey = "PRIMARYKEY";
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("CATALOG", "SCHEMA","TABLE", primaryKey, true);
        assertEquals(Boolean.TRUE,statement.getOnlyUpdate());
    }
    
    @Test
    public void setOnlyUpdateToNull(){
        String primaryKey = "PRIMARYKEY";
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("CATALOG", "SCHEMA","TABLE", primaryKey);
        statement.setOnlyUpdate(null);
        assertEquals(Boolean.FALSE,statement.getOnlyUpdate());
    }
    
}
