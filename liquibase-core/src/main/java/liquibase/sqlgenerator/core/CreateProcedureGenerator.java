package liquibase.sqlgenerator.core;

import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.parser.ChangeLogParserCofiguration;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateProcedureStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.StoredProcedure;
import liquibase.util.SqlParser;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateProcedureGenerator extends AbstractStoLoSqlGenerator<CreateProcedureStatement> {

    @Override
    public ValidationErrors validate(CreateProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureText", statement.getProcedureText());
        if (statement.getReplaceIfExists() != null) {
            if (database instanceof MSSQLDatabase) {
                if (statement.getReplaceIfExists() && statement.getProcedureName() == null) {
                    validationErrors.addError("procedureName is required if replaceIfExists = true");
                }
            } else {
                validationErrors.checkDisallowedField("replaceIfExists", statement.getReplaceIfExists(), null);
            }

        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<Sql> sql = new ArrayList<Sql>();
        List<String> mssqlSetStatementsBefore = new ArrayList<>();
        List<String> mssqlSetStatementsAfter = new ArrayList<>();

        String schemaName = statement.getSchemaName();
        if (schemaName == null) {
            schemaName = database.getDefaultSchemaName();
        }

        String procedureText = addSchemaToText(statement.getProcedureText(), schemaName, "PROCEDURE", database);

        if (database instanceof MSSQLDatabase) {
            MssqlSplitStatements mssqlSplitStatements =
                    splitSetStatementsOutForMssql(procedureText, statement.getEndDelimiter(), Arrays.asList("PROC", "PROCEDURE"));

            procedureText = mssqlSplitStatements.getBody();
            mssqlSetStatementsBefore = mssqlSplitStatements.getSetStatementsBefore();
            mssqlSetStatementsAfter = mssqlSplitStatements.getSetStatementsAfter();
        }

        if (statement.getReplaceIfExists() != null && statement.getReplaceIfExists()) {
            String fullyQualifiedName = database.escapeObjectName(statement.getProcedureName(), StoredProcedure.class);
            if (schemaName != null) {
                fullyQualifiedName = database.escapeObjectName(schemaName, Schema.class) + "." + fullyQualifiedName;
            }
            sql.add(new UnparsedSql("if object_id('" + fullyQualifiedName + "', 'p') is null exec ('create procedure " + fullyQualifiedName + " as select 1 a')"));

            StringClauses parsedProcedureDefinition = SqlParser.parse(procedureText, true, true);
            replaceCreateByAlterIfNotCreateOrReplaceStatement(parsedProcedureDefinition);
            procedureText = parsedProcedureDefinition.toString();
        }

        procedureText = removeTrailingDelimiter(procedureText, statement.getEndDelimiter());
        procedureText = StringUtils.trimToEmpty(procedureText);
        if (database instanceof MSSQLDatabase && procedureText.toLowerCase().contains("merge") &&
                !procedureText.endsWith(";")) { //mssql "AS MERGE" procedures need a trailing ; (regardless of the end delimiter)
            StringClauses parsed = SqlParser.parse(procedureText);
            StringClauses.ClauseIterator clauseIterator = parsed.getClauseIterator();
            boolean reallyMerge = false;
            while (clauseIterator.hasNext()) {
                Object clause = clauseIterator.nextNonWhitespace();
                String clauseString = clause == null ? null : clause.toString();
                if ("merge".equalsIgnoreCase(clauseString)) {
                    reallyMerge = true;
                }
            }
            if (reallyMerge) {
                procedureText = procedureText + ";";
            }
        }
        if (database instanceof Db2zDatabase && procedureText.toLowerCase().contains("replace")) {
            procedureText = procedureText.replace("OR REPLACE", "");
            procedureText = procedureText.replaceAll("[\\s]{2,}", " ");
        }

        if (!mssqlSetStatementsBefore.isEmpty()) {
            mssqlSetStatementsBefore
                    .forEach(mssqlSetStatement ->
                            sql.add(new UnparsedSql(mssqlSetStatement, statement.getEndDelimiter())));
        }

        sql.add(new UnparsedSql(procedureText, statement.getEndDelimiter()));

        if (!mssqlSetStatementsAfter.isEmpty()) {
            mssqlSetStatementsAfter
                    .forEach(mssqlSetStatement ->
                            sql.add(new UnparsedSql(mssqlSetStatement, statement.getEndDelimiter())));
        }

        surroundWithSchemaSets(sql, statement.getSchemaName(), database);
        return sql.toArray(new Sql[sql.size()]);
    }


    public static String removeTrailingDelimiter(String procedureText, String endDelimiter) {
        if (procedureText == null) {
            return null;
        }
        if (endDelimiter == null) {
            return procedureText;
        }

        endDelimiter = endDelimiter.replace("\\r", "\r").replace("\\n", "\n");
        // DVONE-5036
        String endCommentsTrimmedText = StringUtils.stripSqlCommentsAndWhitespacesFromTheEnd(procedureText);
        // Note: need to trim the delimiter since the return of the above call trims whitespaces, and to match
        // we have to trim the endDelimiter as well.
        String trimmedDelimiter = StringUtils.trimRight(endDelimiter);
        if (endCommentsTrimmedText.endsWith(trimmedDelimiter)) {
            return endCommentsTrimmedText.substring(0, endCommentsTrimmedText.length() - trimmedDelimiter.length());
        } else {
            return procedureText;
        }
    }

    /**
     * Convenience method for when the schemaName is set but we don't want to parse the body
     */
    public static void surroundWithSchemaSets(List<Sql> sql, String schemaName, Database database) {
        if ((StringUtils.trimToNull(schemaName) != null) &&
                !LiquibaseConfiguration.getInstance().getProperty(ChangeLogParserCofiguration.class, ChangeLogParserCofiguration.USE_PROCEDURE_SCHEMA).getValue(Boolean.class)) {
            String defaultSchema = database.getDefaultSchemaName();
            if (database instanceof OracleDatabase) {
                sql.add(0, new UnparsedSql("ALTER SESSION SET CURRENT_SCHEMA=" + database.escapeObjectName(schemaName, Schema.class)));
                sql.add(new UnparsedSql("ALTER SESSION SET CURRENT_SCHEMA=" + database.escapeObjectName(defaultSchema, Schema.class)));
            }
            else if (database instanceof AbstractDb2Database) {
                sql.add(0, new UnparsedSql("SET CURRENT SCHEMA " + schemaName));
                sql.add(new UnparsedSql("SET CURRENT SCHEMA " + defaultSchema));
            }
        }
    }

    /**
     * Convenience method for other classes similar to this that want to be able to modify the procedure text to add the schema
     */
    public static String addSchemaToText(String procedureText, String schemaName, String keywordBeforeName, Database database) {
        if ((StringUtils.trimToNull(schemaName) != null) && LiquibaseConfiguration.getInstance().getProperty(ChangeLogParserCofiguration.class, ChangeLogParserCofiguration.USE_PROCEDURE_SCHEMA).getValue(Boolean.class)) {
            StringClauses parsedSql = SqlParser.parse(procedureText, true, true);
            StringClauses.ClauseIterator clauseIterator = parsedSql.getClauseIterator();
            Object next = "START";
            while (next != null && !next.toString().equalsIgnoreCase(keywordBeforeName) && clauseIterator.hasNext()) {
                next = clauseIterator.nextNonWhitespace();
            }
            if (next != null && clauseIterator.hasNext()) {
                Object procNameClause = clauseIterator.nextNonWhitespace();
                if (procNameClause instanceof StringClauses.QuotedIdentifier || procNameClause instanceof String) {
                    String procName = procNameClause.toString();
                    String[] nameParts = procName.split("\\.");
                    String finalName;
                    if (nameParts.length == 1) {
                        finalName = database.escapeObjectName(schemaName, Schema.class) + "." + nameParts[0];
                    } else if (nameParts.length == 2) {
                        finalName = database.escapeObjectName(schemaName, Schema.class) + "." + nameParts[1];
                    } else if (nameParts.length == 3) {
                        finalName = nameParts[0] + "." + database.escapeObjectName(schemaName, Schema.class) + "." + nameParts[2];
                    } else {
                        finalName = procName; //just go with what was there
                    }
                    clauseIterator.replace(finalName);
                }
                procedureText = parsedSql.toString();
            }
        }
        return procedureText;
    }

    /**
     * Split <code>SET ANSI_NULLS/QUOTED_IDENTIFIER ON/OFF</code> statements out of SQL statement
     * into before statements procedure/function/trigger/view body and adds SET ANSI_NULLS ON and
     * SET QUOTED_IDENTIFIER ON into before list to isolate settings change between scripts
     * by keeping default ANSI_NULLS and QUOTED_IDENTIFIER settings behaviour.
     *
     * @param body - SQL to split if SET statements are present
     * @param endDelimiter - end line delimiter
     * @return - before statements if any are present and procedure/function/trigger/view body
     */
    public static MssqlSplitStatements splitSetStatementsOutForMssql(String body, String endDelimiter,
                                                                     List<String> bodyStartStatements) {
        MssqlSplitStatements mssqlSplitStatements = new MssqlSplitStatements();
        List<String> beforeBodyStartStatements = Arrays.asList("CREATE", "ALTER");

        StringClauses sqlClauses = SqlParser.parse(body, true, true);
        StringClauses.ClauseIterator clauseIterator = sqlClauses.getClauseIterator();
        Object next = "";
        List<String> beforeStatements = new ArrayList<>();

        while (next != null && clauseIterator.hasNext()) {
            next = clauseIterator.nextNonWhitespace();

            next = splitOutIfSetStatement(next, clauseIterator, endDelimiter, beforeStatements);

            if (next != null && beforeBodyStartStatements.contains(next.toString().toUpperCase())) {
                next = clauseIterator.nextNonWhitespace();
                if (next != null && bodyStartStatements.contains(next.toString().toUpperCase())) {
                    break;
                }
            }
        }

        List<String> afterStatements = new ArrayList<>();
        afterStatements.add("SET ANSI_NULLS ON");
        afterStatements.add("SET QUOTED_IDENTIFIER ON");

        mssqlSplitStatements.setSetStatementsBefore(beforeStatements);
        mssqlSplitStatements.setBody(sqlClauses.toString().trim());
        mssqlSplitStatements.setSetStatementsAfter(afterStatements);

        return mssqlSplitStatements;
    }

    private static Object splitOutIfSetStatement(Object next, StringClauses.ClauseIterator clauseIterator,
                                                 String endDelimiter, List<String> setStatements) {
        while (next != null && next.toString().equalsIgnoreCase("SET")) {
            StringBuilder bufferedSetStatement = new StringBuilder();
            boolean bufferIsUsed = false;

            bufferedSetStatement.append(next).append(" ");
            clauseIterator.remove();
            next = clauseIterator.nextNonWhitespace();

            if (next != null
                    && (next.toString().equalsIgnoreCase("ANSI_NULLS")
                    || next.toString().equalsIgnoreCase("QUOTED_IDENTIFIER"))) {
                bufferedSetStatement.append(next).append(" ");
                clauseIterator.remove();
                next = clauseIterator.nextNonWhitespace();

                if (next != null
                        && (next.toString().equalsIgnoreCase("ON")
                        || next.toString().equalsIgnoreCase("OFF"))) {
                    bufferedSetStatement.append(next);
                    clauseIterator.remove();
                    setStatements.add(bufferedSetStatement.toString());
                    bufferIsUsed = true;
                    next = clauseIterator.nextNonWhitespace();

                    if (next != null && next.toString().equalsIgnoreCase(endDelimiter)) {
                        clauseIterator.remove();
                    }
                }
            }

            if (StringUtils.isNotEmpty(bufferedSetStatement.toString()) && !bufferIsUsed) {
                clauseIterator.replace(bufferedSetStatement.append(next).toString());
            }
        }

        return next;
    }

    public static class MssqlSplitStatements {
        private List<String> setStatementsBefore;
        private String body;
        private List<String> setStatementsAfter;


        MssqlSplitStatements() {
        }

        public List<String> getSetStatementsBefore() {
            return setStatementsBefore;
        }

        public void setSetStatementsBefore(List<String> setStatementsBefore) {
            this.setStatementsBefore = setStatementsBefore;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public List<String> getSetStatementsAfter() {
            return setStatementsAfter;
        }

        public void setSetStatementsAfter(List<String> setStatementsAfter) {
            this.setStatementsAfter = setStatementsAfter;
        }
    }
}
