package liquibase.change;

import liquibase.database.BigQueryDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.DropColumnStatement;
import liquibase.statement.core.RawSqlStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BigQueryMergeColumnChangeTest {

    private BigQueryDatabase database;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
    }

    @Test
    void generateStatements() {
        BigQueryMergeColumnChange change = new BigQueryMergeColumnChange();
        change.setTableName("tableName");
        change.setColumn1Name("column1Name");
        change.setColumn2Name("column2Name");
        change.setSchemaName("schemaName");
        change.setCatalogName("catalogName");
        change.setFinalColumnName("finalColumnName");
        change.setFinalColumnType("finalColumnName");
        change.setJoinString("joinString");

        SqlStatement[] sqlStatements = change.generateStatements(database);
        assertEquals(4, sqlStatements.length);
        assertInstanceOf(AddColumnStatement.class, sqlStatements[0]);
        assertInstanceOf(RawSqlStatement.class, sqlStatements[1]);
        assertInstanceOf(DropColumnStatement.class, sqlStatements[2]);
        assertInstanceOf(DropColumnStatement.class, sqlStatements[3]);

        AddColumnStatement addColumnStatement = (AddColumnStatement) sqlStatements[0];
        assertEquals("finalColumnName", addColumnStatement.getColumnName());
        assertEquals("finalColumnName", addColumnStatement.getColumnType());

        RawSqlStatement rawSqlStatement = (RawSqlStatement) sqlStatements[1];
        assertEquals("UPDATE schemaName.tableName SET finalColumnName = column1Name || 'joinString' || column2Name WHERE 1 = 1 ", rawSqlStatement.getSql());

        DropColumnStatement drop1ColumnStatement = (DropColumnStatement) sqlStatements[2];
        assertEquals("column1Name", drop1ColumnStatement.getColumnName());

        DropColumnStatement drop2ColumnStatement = (DropColumnStatement) sqlStatements[3];
        assertEquals("column2Name", drop2ColumnStatement.getColumnName());


    }
}