package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropDefaultValueStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

/**
 * A special implementation for the ALTER TABLE ... ALTER COLUMN ... DROP DEFAULT statement for IBM's DB2 family
 * of RDBMSs.
 */
public class DropDefaultValueGeneratorDB2 extends DropDefaultValueGenerator {

    /**
     * Inform the SqlGeneratorFactory that we are more important than our generic cousin.
      * @return PRIORITY_DATABASE
     */
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    /**
     * Inform the SqlGeneratorFactory that we support IBM DB2, and only that.
     * @param statement The statement to check
     * @param database The DBMS implementation to check
     * @return true, if database is a DB2Database. false in all other cases.
     */
    @Override
    public boolean supports(DropDefaultValueStatement statement, Database database) {
        return (database instanceof DB2Database);
    }

    /**
     * Generate the ALTER TABLE ... ALTER COLUMN ... DROP DEFAULT statement for IBM DB2.
     * The main reason is that, unlike other RDBMSs, DB2 complains if you try to
     * DROP DEFAULT on a column that has no default in the first place (even when the user makes no errors in
     * his ChangeLog, this can still happen in a rollback scenario under special circumstances.
     * We could just make the JdbcSqlExecutor ignore that, but that would not be a clean solution.
     * It would also not solve the problem of updateSQL where we still would generate SQL that causes an error
     * during execution.
     *
     * The solution is to employ a short block of "SQL PL" (a procedural database available in DB2 similar to
     * Oracle's PL/SQL). However, this is only possible on the LUW (Linux, Unix and Windows) variant; the other DB2
     * variants seem to lack support for "SQL PL".
     * @param statement
     * @param database
     * @param sqlGeneratorChain
     * @return
     */
    @Override
    public Sql[] generateSql(DropDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Generate SQL based on the flavour of DB2:
        DB2Database db = ((DB2Database)database);
        String sql;

        switch (db.getDataServerType()) {
            case DB2I: // DB2 on IBM iSeries
                // same as...
            case DB2Z: // DB2 on IBM zSeries
                // We cannot prevent the situation on these systems due to the lack of SQL PL. We can only
                // issue a warning (in the DrioDefaultValueChange validator) and pray for the best.
                String escapedTableName = database.escapeTableName(
                        statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
                sql = "ALTER TABLE " + escapedTableName + " ALTER COLUMN " +
                        database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(),
                                statement.getTableName(), statement.getColumnName()) + " DROP DEFAULT";
                break;
            case DB2LUW: // LUW == Linux/Unix/Windows
                // According to
                // https://www.ibm.com/support/knowledgecenter/SSEPGG_9.5.0/com.ibm.db2.luw.sql.ref.doc/doc/r0001038.html ,
                // we can query SYSCAT.COLUMNS.DEFAULT for the value we are looking for.
                String tabSchema = database.correctObjectName(
                        new CatalogAndSchema(statement.getCatalogName(),
                                statement.getSchemaName()).customize(database).getSchemaName(),
                        Schema.class);
                if (tabSchema != null)
                    tabSchema = tabSchema.replaceAll("'", "''");

                sql = String.format("BEGIN\n" +
                                "  DECLARE v_default CLOB;\n" +
                                "\n" +
                                "  SELECT C.default INTO v_default FROM syscat.columns C\n" +
                                "  WHERE tabname='%s'\n" +
                                "        AND colname='%s'\n" +
                                "        AND tabschema='%s';\n" +
                                "\n" +
                                "  IF v_default IS NOT NULL THEN\n" +
                                "    IF v_default <> 'NULL' THEN\n" +
                                "      EXECUTE IMMEDIATE 'ALTER TABLE %s ALTER COLUMN %s DROP DEFAULT';\n" +
                                "    END IF;\n" +
                                "  END IF;\n" +
                                "END\n",
                        database.correctObjectName(statement.getTableName(), Table.class).replaceAll("'", "''"),
                        database.correctObjectName(statement.getColumnName(), Column.class).replaceAll("'", "''"),
                        tabSchema,
                        database.escapeTableName(
                                statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()
                        ).replaceAll("'", "''"),
                        database.escapeObjectName(statement.getColumnName(), Column.class).replaceAll("'", "''")
                );
                break;
            default:
                throw new UnexpectedLiquibaseException("We do not know how to generate the SQL-PL code for this flavour of IBM DB2. Sorry.");
        }

        return new Sql[] {
                new UnparsedSql(sql, getAffectedColumn(statement))
        };
    }

}
