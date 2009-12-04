package liquibase.sqlgenerator.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.database.core.OracleDatabase;
import liquibase.sql.Sql;
import static junit.framework.Assert.assertTrue;


/**
 * Created by IntelliJ IDEA.
 * User: bassettt
 * Date: Dec 2, 2009
 * Time: 12:28:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class InsertOrUpdateGeneratorOracleTest {

    @Test
    public void ContainsInsertStatement(){
        OracleDatabase database = new OracleDatabase();
        InsertOrUpdateGeneratorOracle generator = new InsertOrUpdateGeneratorOracle();
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("myschema","mytable","pk_col1");
        statement.addColumnValue("pk_col1","value1");
        statement.addColumnValue("col2","value2");
        Sql[] sql = generator.generateSql( statement, database,  null);
        String theSql = sql[0].toSql();
        assertTrue(theSql.contains("INSERT INTO myschema.mytable (col2, pk_col1) VALUES ('value2', 'value1');"));
        assertTrue(theSql.contains("UPDATE myschema.mytable"));
        String[] sqlLines = theSql.split("\n");
        int lineToCheck = 0;
        assertEquals("DECLARE",sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("v_reccount NUMBER := 0;",sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("BEGIN",sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("SELECT COUNT(*) INTO v_reccount FROM myschema.mytable WHERE pk_col1 = 'value1';",sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("IF v_reccount = 0 THEN",sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("INSERT INTO myschema.mytable (col2, pk_col1) VALUES ('value2', 'value1');",sqlLines[lineToCheck]);
        lineToCheck++;
        assertEquals("ELSIF v_reccount = 1 THEN",sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("UPDATE myschema.mytable SET col2 = 'value2', pk_col1 = 'value1' WHERE pk_col1 = 'value1';",sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("END IF;",sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("END;",sqlLines[lineToCheck].trim());

/*
DECLARE
  v_prodcount NUMBER := 0;
BEGIN
  -- Check if product with this name already exists
  SELECT COUNT (*)
  INTO   v_prodcount
   FROM books WHERE isbn = 12345678;
  -- Product does not exist
  IF v_prodcount = 0 THEN
   -- Insert row into PRODUCT based on arguments passed
   INSERT INTO books
   VALUES
         ( 12345678,
           98765432,
           'Working with Liquibase');
  -- Product with this name already exists
  ELSIF v_prodcount = 1 THEN
    -- Update the existing product with values
    -- passed as arguments
    UPDATE books
    SET    author_id = 98765432,
           title = 'Working with liquibase'
    WHERE  isbn = 12345678;
  END IF;
END;*/

    }

}
