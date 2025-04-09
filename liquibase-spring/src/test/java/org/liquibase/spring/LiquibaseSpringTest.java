package org.liquibase.spring;

import javax.sql.DataSource;
import jakarta.transaction.Transactional;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.DropAllCommandStep;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.listener.SqlListener;
import liquibase.statement.core.RawParameterizedSqlStatement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
/*import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@DataJpaTest
@Testcontainers
public class LiquibaseSpringTest {
    static final class DatasourceInitializer {

        private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceInitializer.class);
        //
        public static final Logger CONTAINER_LOGGER = LoggerFactory.getLogger("Containerlogger");

        public static void initialize(DynamicPropertyRegistry registry, JdbcDatabaseContainer container) {
            registry.add("spring.datasource.url", container::getJdbcUrl);
            registry.add("spring.datasource.username", container::getUsername);
            registry.add("spring.datasource.password", container::getPassword);
//            logDatasourceInfo(container);
        }

        private static void logDatasourceInfo(JdbcDatabaseContainer container) {
            LOGGER.info("################################################");
            LOGGER.info("Using datasource config for tests: ");
            LOGGER.info("URL     : {}", container.getJdbcUrl());
            LOGGER.info("User    : {}", container.getUsername());
            LOGGER.info("Password: {}", container.getPassword());
            LOGGER.info("################################################");
        }
    }

    @Autowired
    private SpringLiquibase liquibase;

/*    @Autowired
    private DataSource dataSource;*/

    @Container
    protected static final PostgreSQLContainer MY_TEST_DB = new PostgreSQLContainer<>("postgres:16")
            // .withLogConsumer(new Slf4jLogConsumer(DatasourceInitializer.CONTAINER_LOGGER))
            .withDatabaseName("lbcat");

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        DatasourceInitializer.initialize(registry, MY_TEST_DB);
    }

    @Test
    @Transactional(Transactional.TxType.NEVER)
    void migrationAddsVweNummer() throws LiquibaseException, SQLException {
        liquibase.afterPropertiesSet();

        ResultSet resultSet = liquibase.getDataSource().getConnection().createStatement().executeQuery("SELECT * FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';");
        ResultSetMetaData md = resultSet.getMetaData();

        int numCols = md.getColumnCount();

        List<String> colNames = IntStream.range(0, numCols)
                .mapToObj(i -> {
                    try {
                        return md.getColumnName(i + 1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return "?";
                    }
                })
                .collect(Collectors.toList());


        String[] tableNames = {"databasechangelog","databasechangeloglock","city","person","department"};
        for (int i = 0; resultSet.next(); i++){
            Assert.assertEquals(tableNames[i],resultSet.getObject("tablename"));
            //LoggerFactory.getLogger(DatasourceInitializer.class).info(String.valueOf(i));
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

                    break;
            }
        }

        /*JSONArray result = new JSONArray();
        while (resultSet.next()) {
            JSONObject row = new JSONObject();
            colNames.forEach(cn -> {
                try {
                    row.put(cn, resultSet.getObject(cn));
                } catch (JSONException | SQLException e) {
                    e.printStackTrace();
                }
            });
            result.put(row);
        }*/

        /*List<Map<String, ?>> queryResult = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", (Database) liquibase.getDataSource())
                .queryForList(new RawParameterizedSqlStatement(String.format("select * from %s", tableName)));*/

//        LoggerFactory.getLogger(DatasourceInitializer.class).info(String.valueOf(result));
/*        Assert.assertEquals("Rollbacking for " + insertedValue, 2, queryResult.size());
        Assert.assertEquals(insertedValue.toString(), queryResult.get(1).get(colName));*/
    }

    @Test
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



    }


}
