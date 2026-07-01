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
    public void testGenerateSqlNormalizesBlankPartitionByToNull() {
        // Whitespace-only partitionBy must not emit a stray "PARTITION BY " — confirmed by
        // the trim-to-null guard added in response to CodeRabbit on PR #7759.
        for (String blank : new String[]{"", " ", "   ", "\t", "\n", " \n\t "}) {
            CreateTableStatement statement = new CreateTableStatement(null, "public", "blank_tbl")
                    .setPartitionBy(blank);
            statement.addColumn("id", new IntType());

            Sql[] result = new CreateTableGenerator().generateSql(statement, new PostgresDatabase(), null);

            Assert.assertFalse("blank partitionBy (\"" + blank.replace("\n", "\\n").replace("\t", "\\t")
                            + "\") must not emit PARTITION BY: " + result[0].toSql(),
                    result[0].toSql().contains("PARTITION BY"));
        }
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
    public void testGenerateSqlEmitsPartitionByBeforeTablespace() {
        // Per Postgres grammar:
        //   (columns) [PARTITION BY ...] [USING ...] [WITH ...] [ON COMMIT ...] [TABLESPACE ...]
        // The PARTITION BY clause must precede the TABLESPACE clause when both are present.
        // Regression guard for @filipelautert's review on PR #7759.
        CreateTableStatement statement = new CreateTableStatement(null, "public", "part_with_ts")
                .setPartitionBy("RANGE (created_at)")
                .setTablespace("my_ts");
        statement.addColumn("id", new IntType());
        statement.addColumn("created_at", new IntType());

        Sql[] result = new CreateTableGenerator().generateSql(statement, new PostgresDatabase(), null);

        String sql = result[0].toSql();
        Assert.assertEquals(
                "CREATE TABLE public.part_with_ts (id INTEGER, created_at INTEGER) PARTITION BY RANGE (created_at) TABLESPACE my_ts",
                sql
        );
        // Defensive: verify both clauses present in the right order, in case the exact-string
        // assertion drifts on whitespace in the future.
        int partIdx = sql.indexOf("PARTITION BY");
        int tsIdx = sql.indexOf("TABLESPACE");
        Assert.assertTrue("PARTITION BY must appear before TABLESPACE; got: " + sql,
                partIdx > 0 && tsIdx > 0 && partIdx < tsIdx);
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
