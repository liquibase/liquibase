package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.OracleDatabase;
import liquibase.sql.Sql;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.UpdateStatement;
import org.junit.Test;

import static org.junit.Assert.*;


public class InsertOrUpdateGeneratorOracleTest {

    @Test
    public void ContainsInsertStatement() {
        OracleDatabase database = new OracleDatabase();
        InsertOrUpdateGeneratorOracle generator = new InsertOrUpdateGeneratorOracle();
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("mycatalog", "myschema","mytable","pk_col1");
        statement.addColumnValue("pk_col1","value1");
        statement.addColumnValue("col2","value2");
        statement.addColumnValue("pk_col1","value1");
        statement.addColumnValue("col2","value2");
        Sql[] sql = generator.generateSql( statement, database,  null);
        String theSql = sql[0].toSql();
        assertTrue(theSql.contains("INSERT INTO mycatalog.mytable (pk_col1, col2) VALUES ('value1', 'value2');"));
        assertTrue(theSql.contains("UPDATE mycatalog.mytable"));
        String[] sqlLines = theSql.split("\n");
        int lineToCheck = 0;
        assertEquals("DECLARE", sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("v_reccount NUMBER := 0;", sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("BEGIN", sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("SELECT COUNT(*) INTO v_reccount FROM mycatalog.mytable WHERE pk_col1 = 'value1';", sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("IF v_reccount = 0 THEN", sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("INSERT INTO mycatalog.mytable (pk_col1, col2) VALUES ('value1', 'value2');", sqlLines[lineToCheck]);
        lineToCheck++;
        assertEquals("ELSIF v_reccount = 1 THEN", sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("UPDATE mycatalog.mytable SET col2 = 'value2' WHERE pk_col1 = 'value1';", sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("END IF;", sqlLines[lineToCheck].trim());
        lineToCheck++;
        assertEquals("END;", sqlLines[lineToCheck].trim());

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

    @Test
    public void testOnlyUpdateFlag() {
        OracleDatabase database = new OracleDatabase();
        InsertOrUpdateGeneratorOracle generator = new InsertOrUpdateGeneratorOracle();
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("mycatalog", "myschema", "mytable", "pk_col1", true);
        statement.addColumnValue("pk_col1", "value1");
        statement.addColumnValue("col2", "value2");
        Sql[] sql = generator.generateSql(statement, database, null);
        String theSql = sql[0].toSql();
        assertFalse("should not have had insert statement", theSql.contains("INSERT INTO mycatalog.mytable (pk_col1, col2) VALUES ('value1', 'value2');"));
        assertTrue("missing update statement", theSql.contains("UPDATE mycatalog.mytable"));
        String[] sqlLines = theSql.split("\n");
        int lineToCheck = 0;
        assertEquals("UPDATE mycatalog.mytable SET col2 = 'value2' WHERE pk_col1 = 'value1'", sqlLines[lineToCheck].trim());
        assertEquals("Wrong number of lines", 1, sqlLines.length);
    }

    private String prepareInsertStatement(DatabaseFunction databaseSchemaBasedFunction) {
        OracleDatabase database = new OracleDatabase();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);

        InsertGenerator generator = new InsertGenerator();
        InsertStatement statement = new InsertStatement("mycatalog", "myschema", "mytable");
        ColumnConfig columnConfig = new ColumnConfig();

        if (databaseSchemaBasedFunction instanceof SequenceNextValueFunction) {
            columnConfig.setValueSequenceNext((SequenceNextValueFunction) databaseSchemaBasedFunction);
        } else if (databaseSchemaBasedFunction instanceof SequenceCurrentValueFunction) {
            columnConfig.setValueSequenceCurrent((SequenceCurrentValueFunction) databaseSchemaBasedFunction);
        }
        columnConfig.setName("col3");
        statement.addColumn(columnConfig);

        Sql[] sql = generator.generateSql(statement, database, null);

        return sql[0].toSql();
    }

    private String prepareUpdateStatement(SequenceNextValueFunction sequenceNextValueFunction) {
        OracleDatabase database = new OracleDatabase();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);

        UpdateGenerator generator = new UpdateGenerator();
        UpdateStatement statement = new UpdateStatement("mycatalog", "myschema", "mytable");

        statement.addNewColumnValue("col3", sequenceNextValueFunction);

        Sql[] sql = generator.generateSql(statement, database, null);

        return sql[0].toSql();
    }

    @Test
    public void testInsertSequenceValWithSchema() {
        SequenceNextValueFunction sequenceNext = new SequenceNextValueFunction("myschema", "my_seq");
        assertEquals(
                "INSERT INTO mycatalog.mytable (col3) VALUES (myschema.my_seq.nextval)",
                prepareInsertStatement(sequenceNext));
    }

    @Test
    public void testInsertSequenceValWithSchemaInWholeStatement() {
        SequenceNextValueFunction sequenceNext = new SequenceNextValueFunction("myschema", "my_seq");
        assertEquals(
                "INSERT INTO mycatalog.mytable (col3) VALUES (myschema.my_seq.nextval)",
                prepareInsertStatement(sequenceNext));
    }


    @Test
    public void testUpdateSequenceValWithSchema() {
        SequenceNextValueFunction sequenceNext = new SequenceNextValueFunction("myschema", "my_seq");
        assertEquals(
                "UPDATE mycatalog.mytable SET col3 = myschema.my_seq.nextval",
                prepareUpdateStatement(sequenceNext));
    }

    @Test
    public void testUpdateSequenceValWithSchemaInWholeStatement() {
        SequenceNextValueFunction sequenceNext = new SequenceNextValueFunction("myschema", "my_seq");
        assertEquals(
                "UPDATE mycatalog.mytable SET col3 = myschema.my_seq.nextval",
                prepareUpdateStatement(sequenceNext));
    }
}
