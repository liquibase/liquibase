package liquibase.sqlgenerator.core;

import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.core.IntType;
import liquibase.sql.Sql;
import liquibase.exception.Warnings;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateTableStatement;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeSet;

public class CreateTableGeneratorPostgresTest {

    @Test
    public void testGenerateSqlAppendsPartitionByClauseForPostgres() {
        // Given a CreateTableStatement with a partitionBy spec, against PostgresDatabase
        CreateTableStatement statement = new CreateTableStatement(null, "public", "test_tbl_part")
                .setPartitionBy("RANGE (test_date_int)");
        statement.addColumn("test_date_int", new IntType());

        // When the base CreateTableGenerator produces SQL for Postgres
        Sql[] result = new CreateTableGenerator().generateSql(statement, new PostgresDatabase(), null);

        // Then the PARTITION BY clause is appended verbatim, after the column list
        Assert.assertEquals(
                "CREATE TABLE public.test_tbl_part (test_date_int INTEGER) PARTITION BY RANGE (test_date_int)",
                result[0].toSql()
        );
    }

    @Test
    public void testGenerateSqlWithoutPartitionByOnPostgresProducesPlainCreateTable() {
        // Given a statement with NO partitionBy
        CreateTableStatement statement = new CreateTableStatement(null, "public", "plain_tbl");
        statement.addColumn("id", new IntType());

        // When generating SQL for Postgres
        Sql[] result = new CreateTableGenerator().generateSql(statement, new PostgresDatabase(), null);

        // Then no PARTITION BY clause appears
        String sql = result[0].toSql();
        Assert.assertFalse("plain create-table must not emit PARTITION BY: " + sql,
                sql.contains("PARTITION BY"));
    }

    @Test
    public void testGenerateSqlEmitsVerbatimForMultiColumnRangeKey() {
        // pg_get_partkeydef returns multi-column keys as "RANGE (a, b)"; must round-trip verbatim.
        CreateTableStatement statement = new CreateTableStatement(null, "public", "multi_col_part")
                .setPartitionBy("RANGE (a, b)");
        statement.addColumn("a", new IntType());
        statement.addColumn("b", new IntType());

        Sql[] result = new CreateTableGenerator().generateSql(statement, new PostgresDatabase(), null);

        Assert.assertEquals(
                "CREATE TABLE public.multi_col_part (a INTEGER, b INTEGER) PARTITION BY RANGE (a, b)",
                result[0].toSql()
        );
    }

    @Test
    public void testGenerateSqlEmitsVerbatimForFunctionalKey() {
        // Functional partition keys (RANGE (lower(email))) must pass through unchanged — we trust
        // pg_get_partkeydef output rather than parsing/reformatting it.
        CreateTableStatement statement = new CreateTableStatement(null, "public", "func_part")
                .setPartitionBy("RANGE (lower(email))");
        statement.addColumn("email", new IntType()); // type doesn't matter for this assertion

        Sql[] result = new CreateTableGenerator().generateSql(statement, new PostgresDatabase(), null);

        Assert.assertTrue("expected PARTITION BY RANGE (lower(email)); got: " + result[0].toSql(),
                result[0].toSql().endsWith("PARTITION BY RANGE (lower(email))"));
    }

    @Test
    public void testGenerateSqlEmitsVerbatimForQuotedIdentifier() {
        // Case-sensitive identifiers are emitted by pg_get_partkeydef with double quotes preserved.
        CreateTableStatement statement = new CreateTableStatement(null, "public", "quoted_part")
                .setPartitionBy("RANGE (\"MixedCaseCol\")");
        statement.addColumn("MixedCaseCol", new IntType());

        Sql[] result = new CreateTableGenerator().generateSql(statement, new PostgresDatabase(), null);

        Assert.assertTrue("quoted identifier must survive verbatim; got: " + result[0].toSql(),
                result[0].toSql().endsWith("PARTITION BY RANGE (\"MixedCaseCol\")"));
    }

    @Test
    public void testGenerateSqlEmitsListAndHashStrategiesVerbatim() {
        // LIST and HASH strategies (alongside RANGE) all round-trip identically.
        for (String spec : new String[]{"LIST (region)", "HASH (user_id)"}) {
            CreateTableStatement statement = new CreateTableStatement(null, "public", "tbl")
                    .setPartitionBy(spec);
            statement.addColumn("c", new IntType());

            Sql[] result = new CreateTableGenerator().generateSql(statement, new PostgresDatabase(), null);

            Assert.assertTrue("expected ... PARTITION BY " + spec + "; got: " + result[0].toSql(),
                    result[0].toSql().endsWith("PARTITION BY " + spec));
        }
    }

    @Test
    public void testWarnEmittedForPartitionByOnNonPostgresDatabase() {
        // Given a statement with partitionBy targeting a non-Postgres database
        CreateTableStatement statement = new CreateTableStatement(null, "public", "tbl")
                .setPartitionBy("RANGE (dt)");
        statement.addColumn("dt", new IntType());

        // When CreateTableGenerator validates against MySQL (empty downstream chain — we test the leaf generator's contribution)
        Warnings warnings = new CreateTableGenerator().warn(statement, new MySQLDatabase(), new SqlGeneratorChain<>(new TreeSet<>()));

        // Then a warning is produced — partitionBy is silently dropped if we don't warn
        Assert.assertTrue("expected a warning about partitionBy not supported on MySQL; got: " + warnings.getMessages(),
                warnings.getMessages().stream().anyMatch(m -> m.toLowerCase().contains("partition")));
    }
}
