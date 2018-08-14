package liquibase.change;

import liquibase.Scope;
import liquibase.change.core.*;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.database.core.MockDatabase;
import liquibase.serializer.LiquibaseSerializable;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static liquibase.test.Assert.assertSetsEqual;
import static org.junit.Assert.*;

public class ChangeParameterMetaDataTest {

    @Test
    public void constructor() {
        Map<String, Object> examples = new HashMap<String, Object>();
        examples.put("all", "examp");

        ChangeParameterMetaData metaData = new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", "desc", examples, "2.1", Integer.class, new String[]{"mysql", "mssql"}, new String[] {"h2", "mysql","mssql"}, "column", LiquibaseSerializable.SerializationType.NESTED_OBJECT);
        assertEquals("x", metaData.getParameterName()) ;
        assertEquals("y", metaData.getDisplayName());
        assertEquals("integer", metaData.getDataType());
        assertEquals(2, metaData.getRequiredForDatabase().size());
        assertTrue(metaData.getRequiredForDatabase().contains("mysql"));
        assertTrue(metaData.getRequiredForDatabase().contains("mssql"));
        assertEquals("column", metaData.getMustEqualExisting());
        assertEquals(LiquibaseSerializable.SerializationType.NESTED_OBJECT, metaData.getSerializationType());
        assertEquals("desc", metaData.getDescription());
        assertEquals("examp", metaData.getExampleValue(new MockDatabase()));
        assertEquals("2.1", metaData.getSince());

        assertEquals(3, metaData.getSupportedDatabases().size());
        assertTrue(metaData.getSupportedDatabases().contains("mysql"));
        assertTrue(metaData.getSupportedDatabases().contains("mssql"));
        assertTrue(metaData.getSupportedDatabases().contains("h2"));

    }

    @Test
    public void constructor_badValues() {
        try {
            new ChangeParameterMetaData(new ExampleAbstractChange(), null, "y", null, null, null,String.class, null, null, null, LiquibaseSerializable.SerializationType.NAMED_FIELD);
            fail("Did not throw exception");
        } catch (UnexpectedLiquibaseException e) {
            assertEquals("Unexpected null parameterName", e.getMessage());
        }

        try {
            new ChangeParameterMetaData(new ExampleAbstractChange(), "x tag", "y", null, null,null, String.class, null, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD);
            fail("Did not throw exception");
        } catch (UnexpectedLiquibaseException e) {
            assertEquals("Unexpected space in parameterName", e.getMessage());
        }

        try {
            new ChangeParameterMetaData(new ExampleAbstractChange(), "x", null, null, null,null, String.class, null, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD);
            fail("Did not throw exception");
        } catch (UnexpectedLiquibaseException e) {
            assertEquals("Unexpected null displayName", e.getMessage());
        }

        try {
            new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null, null, null, null,null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD);
            fail("Did not throw exception");
        } catch (UnexpectedLiquibaseException e) {
            assertEquals("Unexpected null dataType", e.getMessage());
        }
    }

    @Test
    public void getRequiredForDatabase_nullPassedInReturnsEmptySet() {
        assertEquals(0, new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, null, null,null, LiquibaseSerializable.SerializationType.NAMED_FIELD).getRequiredForDatabase().size());
    }

    @Test
    public void getRequiredForDatabase_nonePassedReturnsEmptySet() {
        assertEquals(0, new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[] {"none"}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).getRequiredForDatabase().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRequiredForDatabase_immutable() {
        new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[] {"mysql"}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).getRequiredForDatabase().add("mssql");
    }

    @Test
    public void isRequiredFor() {
        assertTrue(new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[]{"mysql"}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).isRequiredFor(new MySQLDatabase()));
        assertTrue(new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[]{"mysql"}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).isRequiredFor(new MySQLDatabase() {})); //mysql database subclass
        assertFalse(new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[]{"mysql"}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).isRequiredFor(new MSSQLDatabase()));

        assertTrue(new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[]{"mysql", "mssql"}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).isRequiredFor(new MySQLDatabase()));
        assertTrue(new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[]{"mysql", "mssql"}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).isRequiredFor(new MSSQLDatabase()));
        assertFalse(new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[]{"mysql", "mssql"}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).isRequiredFor(new OracleDatabase()));

        assertTrue(new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[]{"all"}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).isRequiredFor(new OracleDatabase()));
        assertTrue(new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[]{"all"}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).isRequiredFor(new MySQLDatabase()));

        assertFalse(new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[]{}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).isRequiredFor(new OracleDatabase()));
        assertFalse(new ChangeParameterMetaData(new ExampleAbstractChange(), "x", "y", null, null,null, Integer.class, new String[]{}, null, null,LiquibaseSerializable.SerializationType.NAMED_FIELD).isRequiredFor(new MySQLDatabase()));
    }

    @Test
    public void getCurrentValue() {
        CreateTableChange change = new CreateTableChange();
        change.setTableName("newTable");
        change.setCatalogName("newCatalog");

        ChangeParameterMetaData tableNameMetaData = new ChangeParameterMetaData(new ExampleAbstractChange(), "tableName", "New Table", null, null,null, String.class, null,null, null, LiquibaseSerializable.SerializationType.NAMED_FIELD);
        ChangeParameterMetaData catalogNameMetaData = new ChangeParameterMetaData(new ExampleAbstractChange(), "catalogName", "New Catalog", null, null,null, String.class, null, null,null, LiquibaseSerializable.SerializationType.NAMED_FIELD);
        ChangeParameterMetaData remarksMetaData = new ChangeParameterMetaData(new ExampleAbstractChange(), "remarks", "Remarks", null, null,null, String.class, null, null,null, LiquibaseSerializable.SerializationType.NAMED_FIELD);

        assertEquals("newTable", tableNameMetaData.getCurrentValue(change));
        assertEquals("newCatalog", catalogNameMetaData.getCurrentValue(change));
        assertNull(remarksMetaData.getCurrentValue(change));

        change.setTableName("changedTableName");
        assertEquals("changedTableName", tableNameMetaData.getCurrentValue(change));
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void getCurrentValue_badParam() {
        CreateTableChange change = new CreateTableChange();
        ChangeParameterMetaData badParamMetaData = new ChangeParameterMetaData(new ExampleAbstractChange(), "badParameter", "Doesn't really exist", null, null,null, Integer.class, null,null, null, LiquibaseSerializable.SerializationType.NAMED_FIELD);
        badParamMetaData.getCurrentValue(change);

    }

    @Test
    public void computedDatabasesCorrect(){
        ChangeParameterMetaData catalogName = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(new AddNotNullConstraintChange()).getParameters().get("catalogName");
        assertSetsEqual(new String[]{}, catalogName.analyzeRequiredDatabases(new String[] {ChangeParameterMetaData.COMPUTE}));
        assertSetsEqual(new String[]{"all"}, catalogName.analyzeSupportedDatabases(new String[] {ChangeParameterMetaData.COMPUTE}));


        ChangeParameterMetaData tableName = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(new AddNotNullConstraintChange()).getParameters().get("tableName");
        assertSetsEqual(new String[]{"all"}, tableName.analyzeRequiredDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));
        assertSetsEqual(new String[]{"all"}, tableName.analyzeSupportedDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));

        ChangeParameterMetaData columnDataType = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(new AddNotNullConstraintChange()).getParameters().get("columnDataType");
        assertSetsEqual(new String[]{"informix","mssql","h2","mysql","mariadb"}, columnDataType.analyzeRequiredDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));
        assertSetsEqual(new String[]{"all"}, columnDataType.analyzeSupportedDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));

        ChangeParameterMetaData column = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(new AddColumnChange()).getParameters().get("columns");
        assertSetsEqual(new String[]{"all"}, column.analyzeRequiredDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));
        assertSetsEqual(new String[]{"all"}, column.analyzeSupportedDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));

        tableName = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(new AddColumnChange()).getParameters().get("tableName");
        assertSetsEqual(new String[]{"all"}, tableName.analyzeRequiredDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));
        assertSetsEqual(new String[]{"all"}, tableName.analyzeSupportedDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));

        catalogName = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(new DropPrimaryKeyChange()).getParameters().get("catalogName");
        assertSetsEqual(new String[]{}, catalogName.analyzeRequiredDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));
        assertSetsEqual(new String[]{"all"}, catalogName.analyzeSupportedDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));

        ChangeParameterMetaData columns  = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(new AddColumnChange()).getParameters().get("columns");
        assertSetsEqual(new String[]{"all"}, columns.analyzeRequiredDatabases(new String[]{"all"}));
        assertSetsEqual(new String[]{"all"}, columns.analyzeSupportedDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));

        ChangeParameterMetaData baseTableCatalogName  = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(new DropAllForeignKeyConstraintsChange()).getParameters().get("baseTableCatalogName");
        assertSetsEqual(new String[]{}, baseTableCatalogName.analyzeRequiredDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));
        assertSetsEqual(new String[]{"all"}, baseTableCatalogName.analyzeSupportedDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));

        ChangeParameterMetaData replaceIfExists  = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(new CreateViewChange()).getParameters().get("replaceIfExists");
        assertSetsEqual(new String[]{}, replaceIfExists.analyzeRequiredDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));
        assertSetsEqual(new String[]{"sybase","mssql","postgresql","firebird","oracle","sqlite","mysql","mariadb","h2","hsqldb","db2"}, replaceIfExists.analyzeSupportedDatabases(new String[]{ChangeParameterMetaData.COMPUTE}));
    }


}
