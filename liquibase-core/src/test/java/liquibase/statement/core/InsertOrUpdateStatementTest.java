package liquibase.statement.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: bassettt
 * Date: Dec 1, 2009
 * Time: 11:37:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class InsertOrUpdateStatementTest extends InsertStatementTest {

    @Test
    public void setPrimaryKey(){
        String primaryKey = "PRIMARYKEY";
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("SCHEMA","TABLE", primaryKey);
        assertEquals(primaryKey,statement.getPrimaryKey());
    }

}
