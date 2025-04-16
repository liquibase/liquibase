package org.liquibase.spring;

import jakarta.transaction.Transactional;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.*;

@DataJpaTest
@Testcontainers
public class LiquibaseSpringTest {
    static final class DatasourceInitializer {

        private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseSpringTest.class);

        @Container
        protected static final PostgreSQLContainer TEST_DB_POSTGRES = new PostgreSQLContainer<>("postgres:16").withDatabaseName("lbcat");
        public static void initialize(DynamicPropertyRegistry registry) {
            registry.add("spring.datasource.url", TEST_DB_POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", TEST_DB_POSTGRES::getUsername);
            registry.add("spring.datasource.password", TEST_DB_POSTGRES::getPassword);
        }
    }

    @Autowired
    private SpringLiquibase liquibase;

    @BeforeEach
    void runLiquibaseMigrations() throws LiquibaseException {
        liquibase.afterPropertiesSet();
    }


    @Test
    @Transactional(Transactional.TxType.NEVER)
    @DisplayName("Should create expected tables and constraints, and verify column details")
    void shouldVerifyLiquibaseSchemaChanges() throws LiquibaseException, SQLException {

        try(Connection conn = liquibase.getDataSource().getConnection(); Statement stmt = conn.createStatement()) {

        } catch (Exception x) {

        }

        ResultSet resultSet = liquibase.getDataSource().getConnection().createStatement().executeQuery("SELECT * FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';");
        ResultSetMetaData md = resultSet.getMetaData();

        int numCols = md.getColumnCount();

        String[] tableNames = {"databasechangelog","databasechangeloglock","city","person","department"};
        for (int i = 0; resultSet.next(); i++){
            Assert.assertEquals(tableNames[i],resultSet.getObject("tablename"));
            switch (i) {
                case 0:
                    ResultSet getRowCount = liquibase.getDataSource().getConnection().createStatement().executeQuery("SELECT COUNT(*) FROM databasechangelog;");
                    getRowCount.next();
                    Assert.assertEquals(11,getRowCount.getInt(1));
                    getRowCount.close();
                    break;
                case 1:
                    // there's nothing to check for in the DBCLL table
                    break;
                case 2:
                    // need to check primary key (existence + name) and not null constraint
                    ResultSet getPK = liquibase.getDataSource().getConnection().createStatement().executeQuery("select conname, count(conname) from pg_catalog.pg_constraint where conrelid::regclass::text = 'city' group by conname");
                    getPK.next();
                    Assert.assertEquals(1,getPK.getInt(2));
                    Assert.assertEquals("pk_city_zipCode",getPK.getString(1));
                    getPK.close();
                    break;
                case 3:
                    // need to check foreign key (existence + name), added column + default value, auto increment and another default value

                    ResultSet getFK = liquibase.getDataSource().getConnection().createStatement().executeQuery("SELECT conname, count(*) FROM pg_catalog.pg_constraint WHERE conrelid::regclass::text = 'person' AND contype = 'f' group by conname");
                    getFK.next();
                    Assert.assertEquals(1,getFK.getInt(2));
                    Assert.assertEquals("fk-person-city",getFK.getString(1));
                    getFK.close();

                    ResultSet checkColumnDefs = liquibase.getDataSource().getConnection().createStatement().executeQuery("SELECT column_name, data_type, column_default FROM information_schema.columns WHERE table_name = 'person' ORDER BY column_name;");
                    // the order will be {city,dob,id,isLeftHanded,lastName,name}

                    int numColumns;
                    for (numColumns = 0; checkColumnDefs.next(); numColumns++){
                        if (numColumns == 1) {
                            Assert.assertEquals("dob",checkColumnDefs.getString("column_name"));

                            // this is how postgres returns date values
                            Assert.assertEquals("'2000-01-01'::date",checkColumnDefs.getString("column_default"));
                        } else if (numColumns == 2) {
                            Assert.assertEquals("nextval('person_id_seq'::regclass)",checkColumnDefs.getString("column_default"));
                        }
                        else if (numColumns == 3) {
                            Assert.assertEquals(false,checkColumnDefs.getBoolean("column_default"));
                        }
                        else if (numColumns == 4) {
                            Assert.assertEquals("lastName",checkColumnDefs.getString("column_name"));
                            Assert.assertEquals("character varying",checkColumnDefs.getString("data_type"));
                        } else continue;

                    }
                    checkColumnDefs.close();
                    // expecting 6 columns
                    Assert.assertEquals(6,numColumns);
                    break;
                case 4:
                    // need to check for indexes
                    // there will be three: the two created directly and one related to the unique constraint
                    String[] indexNames = {"department-unique","index_one","index_two"};
                    String[] involvedColumns = {"col_1,col_2","col_3,col_1,col_2","col_2,col_3"};

                    ResultSet getIndexes = liquibase.getDataSource().getConnection().createStatement().executeQuery("SELECT i.relname AS index_name, array_to_string(array_agg(a.attname), ',') AS column_names FROM pg_class t JOIN pg_index ix ON t.oid = ix.indrelid JOIN pg_class i ON i.oid = ix.indexrelid JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(ix.indkey) WHERE t.relkind = 'r' AND t.relname = 'department' GROUP BY i.relname; ");

                    for (int j = 0; getIndexes.next(); j++){
                        Assert.assertEquals(indexNames[j],getIndexes.getString("index_name"));
                        Assert.assertEquals(involvedColumns[j],getIndexes.getString("column_names"));
                    }
                    getIndexes.close();
                    break;
            }
        }
    }


    /*
    *
    *
    *
    *       UTILITY METHODS
    *
    *
    *
    * */

    private void assertRowCount(Statement stmt, String table, int expectedCount) {
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            Assert.assertTrue(rs.next());
            Assert.assertEquals(expectedCount,rs.getInt(1));
        } catch (SQLException e) {
            Assert.fail("Failed to assert row count for table " + table + ": " + e.getMessage());
        }
    }

    private void assertConstraintExists(Statement stmt, String table, String constraintName) {
        try (ResultSet rs = stmt.executeQuery("SELECT conname FROM pg_catalog.pg_constraint WHERE conrelid::regclass::text = '" + table + "';")) {
            Set<String> constraints = new HashSet<>();
            while (rs.next()) {
                constraints.add(rs.getString("conname"));
            }
            Assert.assertTrue(constraints.contains(constraintName));
        } catch (SQLException e) {
            Assert.fail("Failed to assert constraint for table " + table + ": " + e.getMessage());
        }
    }

    private void assertForeignKeyExists(Statement stmt, String table, String fkName) {
        try (ResultSet rs = stmt.executeQuery("SELECT conname FROM pg_catalog.pg_constraint WHERE conrelid::regclass::text = '" + table + "' AND contype = 'f';")) {
            List<String> fks = new ArrayList<>();
            while (rs.next()) {
                fks.add(rs.getString("conname"));
            }
            Assert.assertTrue(fks.contains(fkName));
        } catch (SQLException e) {
            Assert.fail("Failed to assert foreign key for table " + table + ": " + e.getMessage());
        }
    }

    private void assertColumnDefaults(Statement stmt, String tableName, List<String> colNames, List<String> expectedDefaults) throws SQLException {
        if (colNames.size() != expectedDefaults.size()) Assert.fail("Number of columns does not match number of expected default values.");

        try (ResultSet rs = stmt.executeQuery("SELECT column_name, column_default FROM information_schema.columns WHERE table_name = '" + tableName + "' ORDER BY column_name;")) {
            Map<String,String> expDefaultsMap = new HashMap<>();
            for (int i = 0; i < colNames.size(); i++){
                expDefaultsMap.put(colNames.get(i),expectedDefaults.get(i));
            }

            Map<String, String> actualDefaults = new HashMap<>();
            while (rs.next()) {
                String name = rs.getString("column_name");
                String def = rs.getString("column_default");
                if (expDefaultsMap.containsKey(name)) {
                    actualDefaults.put(name, def);
                }
            }
            Assert.assertTrue(actualDefaults.entrySet().containsAll(expDefaultsMap.entrySet()));
        } catch (Exception e) {
            Assert.fail("Failed to assert default values for table " + tableName + ": " + e.getMessage());
        }
    }

    /*@Test
    @Ignore
    public void testObjectCreationAndDeletion_basic(){
        DataSource dataSource = liquibase.getDataSource();
        try (DatabaseConnection connection = (DatabaseConnection) dataSource.getConnection()){
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);

            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME);
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG,"db/changelog/basic/createObjects.xml");
            commandScope.execute();


            if (database.getConnection() != null) {
                String sql = "SELECT * FROM " + database.getDatabaseChangeLogTableName();
                for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                    listener.writeSqlWillRun(sql);
                }
                ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement().executeUpdate(
                        sql
                );
                database.commit();
            }
        } catch (Exception ex) {

        }



    }*/


}
