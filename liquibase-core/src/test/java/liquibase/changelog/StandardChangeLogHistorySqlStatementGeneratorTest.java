package liquibase.changelog;

import liquibase.changelog.definition.ChangeLogTableDefinition;
import liquibase.database.core.DB2Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import liquibase.sdk.database.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Table;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardChangeLogHistorySqlStatementGeneratorTest {

    private final ChangeLogTableDefinition standardChangeLogDefinition = new ChangeLogTableDefinition();

    @Test
    public void shouldCreateAllColumnsIfMissing() throws DatabaseException {
        //given
        StandardChangeLogHistorySqlStatementGenerator generator = new StandardChangeLogHistorySqlStatementGenerator();

        String databaseChangeLogTableName = "DATABASECHANGELOG";
        String liquibaseCatalogName = null;
        String liquibaseSchemaName = null;
        String charTypeName = "varchar";

        Table table = new Table(null, null, databaseChangeLogTableName);

        //when
        List<SqlStatement> sqlStatements = generator.changeLogTableUpdate(new MockDatabase(), table, new ChangeLogTableDefinition());

        //then
        assertThat(sqlStatements).isNotEmpty();
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "DESCRIPTION", charTypeName + "(255)", null));
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "TAG", charTypeName + "(255)", null));
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "COMMENTS", charTypeName + "(255)", null));
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "LIQUIBASE", charTypeName + "(20)", null));
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "MD5SUM", charTypeName + "(35)", null));

        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "ORDEREXECUTED", "int", null));
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new UpdateStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName).addNewColumnValue("ORDEREXECUTED", -1));
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new SetNullableStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "ORDEREXECUTED", "int", false));

        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "EXECTYPE", charTypeName + "(10)", null));
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new UpdateStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName).addNewColumnValue("EXECTYPE", "EXECUTED"));
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new SetNullableStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "EXECTYPE", charTypeName + "(10)", false));

        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "CONTEXTS", charTypeName + "(" + 255 +")", null));
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "LABELS", charTypeName + "(" + 255 + ")", null));
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "DEPLOYMENT_ID", "VARCHAR(10)", null));

    }

    @Test
    public void shouldUpdateLiquibaseColumnIfNotRightSize() throws DatabaseException {
        shouldUpdateColumnIfNotRightSize("LIQUIBASE", 20);
    }

    @Test
    public void shouldUpdateMD5SUMColumnIfNotRightSize() throws DatabaseException {
        shouldUpdateColumnIfNotRightSize("MD5SUM", 35);

    }

    private void shouldUpdateColumnIfNotRightSize(String columnName, Integer finalSize) throws DatabaseException {
        //given
        StandardChangeLogHistorySqlStatementGenerator generator = new StandardChangeLogHistorySqlStatementGenerator();

        String databaseChangeLogTableName = "DATABASECHANGELOG";
        String liquibaseCatalogName = null;
        String liquibaseSchemaName = null;
        String charTypeName = "varchar";

        Table table = new Table(null, null, databaseChangeLogTableName);
        Column column = new Column(columnName);
        DataType type = new DataType();
        type.setTypeName("varchar");
        type.setColumnSize(finalSize - 10);
        column.setType(type);
        table.addColumn(column);

        //when
        List<SqlStatement> sqlStatements = generator.changeLogTableUpdate(new MockDatabase(), table, new ChangeLogTableDefinition());

        //then
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, charTypeName + "(" + finalSize + ")"));
    }


    @Test
    public void shouldNotUpdateLiquibaseColumnIfWrongType() throws DatabaseException {
        shouldNotUpdateColumnIfWrongType("LIQUIBASE", "(20)");
    }

    @Test
    public void shouldNotUpdateMD5SUMColumnIfWrongType() throws DatabaseException {
        shouldNotUpdateColumnIfWrongType("MD5SUM", "(35)");
    }

    private void shouldNotUpdateColumnIfWrongType(String liquibase, String s) throws DatabaseException {
        //given
        StandardChangeLogHistorySqlStatementGenerator generator = new StandardChangeLogHistorySqlStatementGenerator();

        String databaseChangeLogTableName = "DATABASECHANGELOG";
        String liquibaseCatalogName = null;
        String liquibaseSchemaName = null;
        String charTypeName = "varchar";

        Table table = new Table(null, null, databaseChangeLogTableName);
        Column column = new Column(liquibase);
        DataType type = new DataType();
        type.setTypeName("test");
        column.setType(type);
        table.addColumn(column);

        //when
        List<SqlStatement> sqlStatements = generator.changeLogTableUpdate(new MockDatabase(), table, standardChangeLogDefinition);

        //then
        assertThat(sqlStatements).usingFieldByFieldElementComparator().doesNotContain(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, liquibase, charTypeName + s));
    }

    @Test
    public void shouldNotUpdateLiquibaseColumnIfSQLiteDatabase() throws DatabaseException {
        shouldNotUpdateColumnIfSQLLiteDatabase("LIQUIBASE", 20);

    }

    @Test
    public void shouldNotUpdateMD5SUMColumnIfSQLiteDatabase() throws DatabaseException {
        //given
        shouldNotUpdateColumnIfSQLLiteDatabase("MD5SUM", 35);
    }

    private void shouldNotUpdateColumnIfSQLLiteDatabase(String liquibase, Integer s) throws DatabaseException {
        StandardChangeLogHistorySqlStatementGenerator generator = new StandardChangeLogHistorySqlStatementGenerator();

        String databaseChangeLogTableName = "DATABASECHANGELOG";
        String liquibaseCatalogName = null;
        String liquibaseSchemaName = null;
        String charTypeName = "varchar";

        Table table = new Table(null, null, databaseChangeLogTableName);

        //when
        List<SqlStatement> sqlStatements = generator.changeLogTableUpdate(new SQLiteDatabase(), table, standardChangeLogDefinition);

        //then
        assertThat(sqlStatements).usingFieldByFieldElementComparator().doesNotContain(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, liquibase, charTypeName + "(" + s + ")"));
    }

    @Test
    public void shouldNotUpdateLiquibaseColumnIfRightSize() throws DatabaseException {
        shouldNotUpdateColumnIfRightSize("LIQUIBASE", 20);
    }

    @Test
    public void shouldNotUpdateMD5SUMColumnIfRightSize() throws DatabaseException {
        shouldNotUpdateColumnIfRightSize("MD5SUM", 35);
    }

    @Test
    public void shouldNotUpdateContextsColumnIfRightSize() throws DatabaseException {
        shouldNotUpdateColumnIfRightSize("CONTEXTS", 255);
    }

    @Test
    public void shouldNotUpdateLabelsColumnIfRightSize() throws DatabaseException {
        shouldNotUpdateColumnIfRightSize("LABELS", 255);
    }

    @Test
    public void shouldNotUpdateContextsColumnIfDoesntHaveSize() throws DatabaseException {
        //given
        StandardChangeLogHistorySqlStatementGenerator generator = new StandardChangeLogHistorySqlStatementGenerator();

        String databaseChangeLogTableName = "DATABASECHANGELOG";
        String liquibaseCatalogName = null;
        String liquibaseSchemaName = null;
        String charTypeName = "varchar";


        Table table = new Table(null, null, databaseChangeLogTableName);
        Column column = new Column("CONTEXTS");
        DataType type = new DataType();
        type.setTypeName("varchar");
        type.setColumnSize(null);
        column.setType(type);
        table.addColumn(column);


        //when
        List<SqlStatement> sqlStatements = generator.changeLogTableUpdate(new MockDatabase(), table, standardChangeLogDefinition);

        //then
        assertThat(sqlStatements).usingFieldByFieldElementComparator().doesNotContain(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "CONTEXTS", charTypeName + "("+255+")"));
    }

    private void shouldNotUpdateColumnIfRightSize(String columnName, Integer size) throws DatabaseException {
        //given
        StandardChangeLogHistorySqlStatementGenerator generator = new StandardChangeLogHistorySqlStatementGenerator();

        String databaseChangeLogTableName = "DATABASECHANGELOG";
        String liquibaseCatalogName = null;
        String liquibaseSchemaName = null;
        String charTypeName = "varchar";


        Table table = new Table(null, null, databaseChangeLogTableName);
        Column column = new Column(columnName);
        DataType type = new DataType();
        type.setTypeName("varchar");
        type.setColumnSize(size);
        column.setType(type);
        table.addColumn(column);


        //when
        List<SqlStatement> sqlStatements = generator.changeLogTableUpdate(new MockDatabase(), table, standardChangeLogDefinition);

        //then
        assertThat(sqlStatements).usingFieldByFieldElementComparator().doesNotContain(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, charTypeName + "(" + size + ")"));
    }

    @Test
    public void shouldUpdateContextsColumnIfWrongSize() throws DatabaseException {
        //given
        StandardChangeLogHistorySqlStatementGenerator generator = new StandardChangeLogHistorySqlStatementGenerator();

        String databaseChangeLogTableName = "DATABASECHANGELOG";
        String liquibaseCatalogName = null;
        String liquibaseSchemaName = null;
        String charTypeName = "varchar";


        Table table = new Table(null, null, databaseChangeLogTableName);
        Column column = new Column("CONTEXTS");
        DataType type = new DataType();
        type.setColumnSize(10);
        column.setType(type);
        table.addColumn(column);


        //when
        List<SqlStatement> sqlStatements = generator.changeLogTableUpdate(new MockDatabase(), table, standardChangeLogDefinition);

        //then
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "CONTEXTS", charTypeName + "("+255+")"));
    }

    @Test
    public void shouldNotUpdateLabelsColumnIfDoesntHaveSize() throws DatabaseException {
        //given
        StandardChangeLogHistorySqlStatementGenerator generator = new StandardChangeLogHistorySqlStatementGenerator();

        String databaseChangeLogTableName = "DATABASECHANGELOG";
        String liquibaseCatalogName = null;
        String liquibaseSchemaName = null;
        String charTypeName = "varchar";


        Table table = new Table(null, null, databaseChangeLogTableName);
        Column column = new Column("LABELS");
        DataType type = new DataType();
        type.setTypeName("varchar");
        type.setColumnSize(null);
        column.setType(type);
        table.addColumn(column);


        //when
        List<SqlStatement> sqlStatements = generator.changeLogTableUpdate(new MockDatabase(), table, standardChangeLogDefinition);

        //then
        assertThat(sqlStatements).usingFieldByFieldElementComparator().doesNotContain(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "LABELS", charTypeName + "("+255+")"));
    }

    @Test
    public void shouldUpdateLabelsColumnIfWrongSize() throws DatabaseException {
        //given
        StandardChangeLogHistorySqlStatementGenerator generator = new StandardChangeLogHistorySqlStatementGenerator();

        String databaseChangeLogTableName = "DATABASECHANGELOG";
        String liquibaseCatalogName = null;
        String liquibaseSchemaName = null;
        String charTypeName = "varchar";


        Table table = new Table(null, null, databaseChangeLogTableName);
        Column column = new Column("LABELS");
        DataType type = new DataType();
        type.setColumnSize(10);
        column.setType(type);
        table.addColumn(column);


        //when
        List<SqlStatement> sqlStatements = generator.changeLogTableUpdate(new MockDatabase(), table, standardChangeLogDefinition);

        //then
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, "LABELS", charTypeName + "("+255+")"));
    }

    @Test
    public void shouldReorganizeTableIfDb2DatabaseAndDoesntHaveDeploymentIdColumn() throws DatabaseException {
        //given
        StandardChangeLogHistorySqlStatementGenerator generator = new StandardChangeLogHistorySqlStatementGenerator();

        String databaseChangeLogTableName = "DATABASECHANGELOG";
        String liquibaseCatalogName = null;
        String liquibaseSchemaName = null;
        String charTypeName = "varchar";

        Table table = new Table(null, null, databaseChangeLogTableName);

        //when
        List<SqlStatement> sqlStatements = generator.changeLogTableUpdate(new DB2Database(), table, standardChangeLogDefinition);

        //then
        assertThat(sqlStatements).usingFieldByFieldElementComparator().contains(new ReorganizeTableStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName));
    }
}