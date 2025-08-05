package liquibase.datatype;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.datatype.core.*;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateTableStatement;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for new Snowflake data types against live database.
 * Tests VARIANT, ARRAY, OBJECT, GEOGRAPHY, GEOMETRY data types.
 */
public class NewDataTypesIntegrationTest {

    private Database database;
    private Connection connection;
    private List<String> createdTables = new ArrayList<>();
    private String testDatabase = "LB_DBEXT_INT_DB";
    private String testSchema = "BASE_SCHEMA";

    private String getUniqueTableName(String methodName) {
        return "TEST_DATATYPE_" + methodName;
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Use YAML configuration instead of environment variables
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        
        // Create test database and schema
        try {
            PreparedStatement createDbStmt = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS " + testDatabase);
            createDbStmt.execute();
            createDbStmt.close();
            
            PreparedStatement useDbStmt = connection.prepareStatement("USE DATABASE " + testDatabase);
            useDbStmt.execute();
            useDbStmt.close();
            
            PreparedStatement createSchemaStmt = connection.prepareStatement("CREATE SCHEMA IF NOT EXISTS " + testSchema);
            createSchemaStmt.execute();
            createSchemaStmt.close();
            
            PreparedStatement useSchemaStmt = connection.prepareStatement("USE SCHEMA " + testSchema);
            useSchemaStmt.execute();
            useSchemaStmt.close();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set up test environment: " + e.getMessage(), e);
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Drop all created tables
        for (String tableName : createdTables) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP TABLE IF EXISTS " + tableName);
                dropStmt.execute();
                dropStmt.close();
                System.out.println("Cleaned up table: " + tableName);
            } catch (SQLException e) {
                System.err.println("Failed to drop table " + tableName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testVariantDataType() throws Exception {
        String tableName = getUniqueTableName("testVariantDataType");
        createdTables.add(tableName);

        System.out.println("Testing VARIANT Data Type");

        CreateTableStatement statement = new CreateTableStatement(null, null, tableName);
        statement.addColumn("id", new IntType(), null, new ColumnConstraint[]{new NotNullConstraint()});
        statement.addColumn("variant_data", new VariantTypeSnowflake(), null);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        System.out.println("GENERATED SQL: " + sql);
        assertTrue(sql.contains("VARIANT"), "SQL should contain VARIANT data type");

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: VARIANT table created successfully");
    }

    @Test
    public void testArrayDataType() throws Exception {
        String tableName = getUniqueTableName("testArrayDataType");
        createdTables.add(tableName);

        System.out.println("Testing ARRAY Data Type");

        CreateTableStatement statement = new CreateTableStatement(null, null, tableName);
        statement.addColumn("id", new IntType(), null, new ColumnConstraint[]{new NotNullConstraint()});
        statement.addColumn("array_data", new ArrayTypeSnowflake(), null);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        String sql = sqls[0].toSql();
        System.out.println("GENERATED SQL: " + sql);
        assertTrue(sql.contains("ARRAY"), "SQL should contain ARRAY data type");

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: ARRAY table created successfully");
    }

    @Test
    public void testObjectDataType() throws Exception {
        String tableName = getUniqueTableName("testObjectDataType");
        createdTables.add(tableName);

        System.out.println("Testing OBJECT Data Type");

        CreateTableStatement statement = new CreateTableStatement(null, null, tableName);
        statement.addColumn("id", new IntType(), null, new ColumnConstraint[]{new NotNullConstraint()});
        statement.addColumn("object_data", new ObjectTypeSnowflake(), null);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        String sql = sqls[0].toSql();
        System.out.println("GENERATED SQL: " + sql);
        assertTrue(sql.contains("OBJECT"), "SQL should contain OBJECT data type");

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: OBJECT table created successfully");
    }

    @Test
    public void testGeographyDataType() throws Exception {
        String tableName = getUniqueTableName("testGeographyDataType");
        createdTables.add(tableName);

        System.out.println("Testing GEOGRAPHY Data Type");

        CreateTableStatement statement = new CreateTableStatement(null, null, tableName);
        statement.addColumn("id", new IntType(), null, new ColumnConstraint[]{new NotNullConstraint()});
        statement.addColumn("geography_data", new GeographyTypeSnowflake(), null);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        String sql = sqls[0].toSql();
        System.out.println("GENERATED SQL: " + sql);
        assertTrue(sql.contains("GEOGRAPHY"), "SQL should contain GEOGRAPHY data type");

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: GEOGRAPHY table created successfully");
    }

    @Test
    public void testGeometryDataType() throws Exception {
        String tableName = getUniqueTableName("testGeometryDataType");
        createdTables.add(tableName);

        System.out.println("Testing GEOMETRY Data Type");

        CreateTableStatement statement = new CreateTableStatement(null, null, tableName);
        statement.addColumn("id", new IntType(), null, new ColumnConstraint[]{new NotNullConstraint()});
        statement.addColumn("geometry_data", new GeometryTypeSnowflake(), null);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        String sql = sqls[0].toSql();
        System.out.println("GENERATED SQL: " + sql);
        assertTrue(sql.contains("GEOMETRY"), "SQL should contain GEOMETRY data type");

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: GEOMETRY table created successfully");
    }

    @Test
    public void testAllNewDataTypesInOneTable() throws Exception {
        String tableName = getUniqueTableName("testAllNewDataTypesInOneTable");
        createdTables.add(tableName);

        System.out.println("Testing ALL New Data Types in One Table");

        CreateTableStatement statement = new CreateTableStatement(null, null, tableName);
        statement.addColumn("id", new IntType(), null, new ColumnConstraint[]{new NotNullConstraint()});
        statement.addColumn("variant_col", new VariantTypeSnowflake(), null);
        statement.addColumn("array_col", new ArrayTypeSnowflake(), null);
        statement.addColumn("object_col", new ObjectTypeSnowflake(), null);
        statement.addColumn("geography_col", new GeographyTypeSnowflake(), null);
        statement.addColumn("geometry_col", new GeometryTypeSnowflake(), null);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        String sql = sqls[0].toSql();
        System.out.println("GENERATED SQL: " + sql);
        
        // Verify all data types are present
        assertTrue(sql.contains("VARIANT"), "SQL should contain VARIANT");
        assertTrue(sql.contains("ARRAY"), "SQL should contain ARRAY");
        assertTrue(sql.contains("OBJECT"), "SQL should contain OBJECT");
        assertTrue(sql.contains("GEOGRAPHY"), "SQL should contain GEOGRAPHY");
        assertTrue(sql.contains("GEOMETRY"), "SQL should contain GEOMETRY");

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: Table with ALL new data types created successfully");
    }
}