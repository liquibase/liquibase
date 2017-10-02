package liquibase.sqlgenerator.core;

import liquibase.change.AddColumnConfig;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.structure.core.Index;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Firebird-specific CREATE INDEX SQL generator.
 */
public class CreateIndexGeneratorFirebird extends CreateIndexGenerator {

    // Just a little value object for passing a complex result of the column analysis
    private final class ColumnAnalysisResult {
        private String columnExpression;
        private boolean foundAscColumns;
        private boolean foundDescColumns;
        private int numComputedCols;
        private int numRegularCols;

        public String getColumnExpression() {
            return columnExpression;
        }

        public void setColumnExpression(String columnExpression) {
            this.columnExpression = columnExpression;
        }

        public boolean isFoundAscColumns() {
            return foundAscColumns;
        }

        public void setFoundAscColumns(boolean foundAscColumns) {
            this.foundAscColumns = foundAscColumns;
        }

        public boolean isFoundDescColumns() {
            return foundDescColumns;
        }

        public void setFoundDescColumns(boolean foundDescColumns) {
            this.foundDescColumns = foundDescColumns;
        }

        public int getNumComputedCols() {
            return numComputedCols;
        }

        public void setNumComputedCols(int numComputedCols) {
            this.numComputedCols = numComputedCols;
        }

        public int getNumRegularCols() {
            return numRegularCols;
        }

        public void setNumRegularCols(int numRegularCols) {
            this.numRegularCols = numRegularCols;
        }
    }

    /**
     * Informs the SqlGeneratorFactory that we are the preferred generator for CREATE INDEX statements in a
     * Firebird database.
     * @return The PRIORITY_DATABASE priority
     */
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    /**
     * Informs the SqlGeneratorFactory that we this class works on Firebird SQL databases only.
     * @param statement The SqlStatement object (ignored in this case)
     * @param database The database object to be compared
     * @return true if database is a Firebird database, else in other cases.
     */
    @Override
    public boolean supports(CreateIndexStatement statement, Database database) {
        return database instanceof FirebirdDatabase;
    }

    /**
     * Generate a CREATE INDEX SQL statement for Firebird databases.
     * @param statement A CreateIndexStatement with the desired properties of the SQL to be generated
     * @param database A database object (must be of FirebirdDatabase type, or we will error out)
     * @param sqlGeneratorChain The other generators in the current chain (ignored by this implementation)
     * @return An array with one entry containing the generated CREATE INDEX statement for Firebird.
     */
    @Override
    public Sql[] generateSql(CreateIndexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain)  {
        /*
         * According to https://firebirdsql.org/refdocs/langrefupd20-create-table.html#langrefupd20-ct-using-index ,
         * Firebird automatically creates indexes for PRIMARY KEY, UNIQUE KEY and FOREIGN KEY constraints,
         * so we should not duplicate that functionality (=we should not issue CREATE INDEX statements for them)
         */
        List<String> associatedWith = StringUtils.splitAndTrim(statement.getAssociatedWith(), ",");
        if ((associatedWith != null) && (associatedWith.contains(Index.MARK_PRIMARY_KEY) || associatedWith.contains
            (Index.MARK_UNIQUE_CONSTRAINT) || associatedWith.contains(Index.MARK_FOREIGN_KEY))) {
            return new Sql[0];
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append("CREATE ");

        // If the statement wants a UNIQUE index, issue a CREATE UNIQUE ... INDEX statement.
        if ((statement.isUnique() != null) && statement.isUnique()) {
            buffer.append("UNIQUE ");
        }

        /*
         * Examine the direction (ASCending or DESCending) of all columns of the planned index. If _all_ of them
         * are DESC, issue a CREATE DESCENDING INDEX statement. If all of them are ASC, we do not need to do anything
         * special (ASCENDING is the default). However, since Firebird does not seem to support mixed ASC/DESC
         * columns within a single index, we must error out.
         *
         * The syntax we want to create is this:
         * CREATE [UNIQUE] [ASC[ENDING] | DESC[ENDING]] INDEX index_name ON table_name
          * {(col [, col …]) | COMPUTED BY (<expression>)};
         */
        ColumnAnalysisResult result = analyseColumns(statement, database);

        if (result.isFoundDescColumns())
            buffer.append("DESCENDING ");

        buffer.append("INDEX ");

        if (statement.getIndexName() != null) {
            String indexSchema = statement.getTableSchemaName();
            buffer.append(database.escapeIndexName(statement.getTableCatalogName(), indexSchema,
                    statement.getIndexName())).append(" ");
        }

        buffer.append("ON ");
        // append table name
        buffer.append(database.escapeTableName(statement.getTableCatalogName(), statement.getTableSchemaName(),
                statement.getTableName()));

        if (result.getNumComputedCols() > 0)
            buffer.append("COMPUTED BY ");

        buffer.append(String.format("(%s)", result.getColumnExpression()));

        return new Sql[]{new UnparsedSql(buffer.toString(), getAffectedIndex(statement))};
    }

    /**
     * Analyzes the column list for the statement and returns a ColumnAnalysisResult which contains:
     * - The final column list (or computed expression)
     * - How many regular columns were found
     * - How many computed expressions were found
     * - If any column had an ASCending sorting
     * - If any column had a DESCending sorting
     * @param statement the CreateIndexStatement to analyse
     * @param database the Database object (FirebirdDatabase is expected)
     * @return the result of the analysis
     */
    private ColumnAnalysisResult analyseColumns(CreateIndexStatement statement,
                                                Database database)  {
        ColumnAnalysisResult result = new ColumnAnalysisResult();

        StringBuffer idxColsBuf = new StringBuffer();
        result.foundAscColumns = false;
        result.foundDescColumns = false;
        result.numComputedCols = 0;
        result.numRegularCols = 0;

        Iterator<AddColumnConfig> iterator = Arrays.asList(statement.getColumns()).iterator();

        while (iterator.hasNext()) {
            AddColumnConfig column = iterator.next();
            boolean columnExpressionIsComputed;
            if (column.getComputed() == null) {
                /* We absolutely must know whether we have a column identifier or something else (expression index)
                 * in front of us, because the correct syntax depends on it.
                 */
                columnExpressionIsComputed = applyIsComputedExpressionHeuristic(column, database);
            } else {
                columnExpressionIsComputed = column.getComputed();
            }

            if (columnExpressionIsComputed) {
                idxColsBuf.append(column.getName()); // Use the expression as-is
                result.setNumComputedCols(result.getNumComputedCols()+1);
            } else {
                result.setNumRegularCols((result.getNumRegularCols())+1);
                idxColsBuf.append(database.escapeColumnName(statement.getTableCatalogName(), statement.getTableSchemaName(), statement.getTableName(), column.getName()));
            }

            if ((column.getDescending() != null) && column.getDescending()) {
                result.setFoundDescColumns(true);
            } else {
                result.setFoundAscColumns(true);
            }

            if (iterator.hasNext()) {
                idxColsBuf.append(", ");
            }
        }

        result.setColumnExpression(idxColsBuf.toString());
        return result;
    }

    @Override
    public ValidationErrors validate(CreateIndexStatement createIndexStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = super.validate(createIndexStatement, database, sqlGeneratorChain);
        errors.checkRequiredField("name", createIndexStatement.getIndexName());

        ColumnAnalysisResult result = analyseColumns(createIndexStatement, database);

        // Error out if we cannot build the CREATE INDEX statement due to syntax problems.
        if (result.isFoundAscColumns() && result.isFoundDescColumns()) {
            errors.addError("Firebird cannot create indexes with mixed ASCending / DESCending columns.");
        }
        if ((result.getNumComputedCols() > 0) && (result.getNumRegularCols() > 0)) {
            errors.addError("Firebird cannot create indexes with both computed expressions and regular columns.");
        }
        if (result.getNumComputedCols() > 1) {
            errors.addError("Firebird cannot create indexes on more than 1 computed expression.");
        }

        return errors;
    }

    /**
         * An imperfect heuristic to determine if an expression is more likely a column name or a computed expression.
         * @return true if it is more likely an expression, false if it is more likely a column name (identifier).
         */

    private boolean applyIsComputedExpressionHeuristic(ColumnConfig column, Database database) {
        String expr = column.getName();

        /*
         * https://firebirdsql.org/file/documentation/reference_manuals/fblangref25-en/html/fblangref25-structure-identifiers.html
         * says the following about what makes a valid identifier in Firebird:
         * - At most 31 chars
         * - Starts with a 7-bit character
         * - After that, letters, digits, underscores or dollar signs are valid characters
         */
        String regex = "^(?i)[ABCDEFGHIJKLMNOPQRSTUVWXYZ]" // Starting character
                + "[ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_$]{0,30}$"; // following characters
        if (!expr.matches(regex))
            return true;

        /* At this point, we know that expr at least has the form of an identifier. If it is a function, it must
         * be in the list of database functions.
         */
        if (database.isFunction(expr))
            return true;
        else
            return false;
    }
}

