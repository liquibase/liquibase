package liquibase.sqlgenerator.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.parser.LiquibaseSqlParser;
import liquibase.parser.SqlParserFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateProcedureStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.Schema;
import liquibase.structure.core.StoredProcedure;
import liquibase.util.StringClauses;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CreateProcedureGenerator extends AbstractSqlGenerator<CreateProcedureStatement> {

    @Override
    public boolean supports(CreateProcedureStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(CreateProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureText", statement.getProcedureText());
        if (statement.getReplaceIfExists() != null) {
            if (database instanceof MSSQLDatabase || database instanceof MySQLDatabase || database instanceof DB2Database || database instanceof Db2zDatabase) {
                if (statement.getReplaceIfExists() && (statement.getProcedureName() == null)) {
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
        List<Sql> sql = new ArrayList<>();

        String schemaName = statement.getSchemaName();
        if ((schemaName == null) && GlobalConfiguration.ALWAYS_OVERRIDE_STORED_LOGIC_SCHEMA.getCurrentValue()) {
            schemaName = database.getDefaultSchemaName();
        }

        String procedureText = addSchemaToText(statement.getProcedureText(), schemaName, "PROCEDURE", database);
        SqlParserFactory sqlParserFactory = Scope.getCurrentScope().getSingleton(SqlParserFactory.class);
        LiquibaseSqlParser sqlParser = sqlParserFactory.getSqlParser();

        if ((statement.getReplaceIfExists() != null) && statement.getReplaceIfExists()) {
            if (database instanceof MSSQLDatabase) {
                String fullyQualifiedName = database.escapeObjectName(statement.getProcedureName(), StoredProcedure.class);
                if (schemaName != null) {
                    fullyQualifiedName = database.escapeObjectName(schemaName, Schema.class) + "." + fullyQualifiedName;
                }
                sql.add(new UnparsedSql("if object_id('" + fullyQualifiedName + "', 'p') is null exec ('create procedure " + fullyQualifiedName + " as select 1 a')"));

                StringClauses parsedSql = sqlParser.parse(procedureText, true, true);
                StringClauses.ClauseIterator clauseIterator = parsedSql.getClauseIterator();
                Object next = "START";
                while ((next != null) && !("create".equalsIgnoreCase(next.toString()) || "alter".equalsIgnoreCase(next
                        .toString())) && clauseIterator.hasNext()) {
                    next = clauseIterator.nextNonWhitespace();
                }
                clauseIterator.replace("ALTER");
                procedureText = parsedSql.toString();
            } else if (!(database instanceof DB2Database) && !(database instanceof Db2zDatabase)) {
                String fullyQualifiedName = database.escapeObjectName(statement.getProcedureName(), StoredProcedure.class);
                sql.add(new UnparsedSql("DROP PROCEDURE IF EXISTS " + fullyQualifiedName));
            }
        }

        procedureText = removeTrailingDelimiter(procedureText, statement.getEndDelimiter());
        if (procedureText == null) {
            return sql.toArray(EMPTY_SQL);
        }

        if ((database instanceof MSSQLDatabase) &&
            procedureText.toLowerCase().contains("merge") &&
                !procedureText.endsWith(";")) { //mssql "AS MERGE" procedures need a trailing ; (regardless of the end delimiter)
            StringClauses parsed = sqlParser.parse(procedureText);
            StringClauses.ClauseIterator clauseIterator = parsed.getClauseIterator();
            boolean reallyMerge = false;
            while (clauseIterator.hasNext()) {
                Object clause = clauseIterator.nextNonWhitespace();
                if ("merge".equalsIgnoreCase((String) clause)) {
                    reallyMerge = true;
                }
            }
            if (reallyMerge) {
                procedureText = procedureText + ";";
            }
        }
        sql.add(new UnparsedSql(procedureText, statement.getEndDelimiter()));
        surroundWithSchemaSets(sql, statement.getSchemaName(), database);
        return sql.toArray(EMPTY_SQL);
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
        String endCommentsTrimmedText = StringUtil.stripSqlCommentsAndWhitespacesFromTheEnd(procedureText);
        // Note: need to trim the delimiter since the return of the above call trims whitespaces, and to match
        // we have to trim the endDelimiter as well.
        String trimmedDelimiter = StringUtil.trimRight(endDelimiter);
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
        if ((StringUtil.trimToNull(schemaName) != null) &&
                !ChangeLogParserConfiguration.USE_PROCEDURE_SCHEMA.getCurrentValue()) {
            String defaultSchema = database.getDefaultSchemaName();
            if (database instanceof OracleDatabase) {
                sql.add(0, new UnparsedSql("ALTER SESSION SET CURRENT_SCHEMA=" + database.escapeObjectName(schemaName, Schema.class)));
                sql.add(new UnparsedSql("ALTER SESSION SET CURRENT_SCHEMA=" + database.escapeObjectName(defaultSchema, Schema.class)));
            } else if (database instanceof AbstractDb2Database) {
                sql.add(0, new UnparsedSql("SET CURRENT SCHEMA " + schemaName));
                sql.add(new UnparsedSql("SET CURRENT SCHEMA " + defaultSchema));
            } else if (database instanceof PostgresDatabase) {
                final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
                String originalSearchPath = null;
                if (executor instanceof JdbcExecutor) {
                    try {
                        originalSearchPath = executor.queryForObject(new RawParameterizedSqlStatement("SHOW SEARCH_PATH"), String.class);
                    } catch (Throwable e) {
                        Scope.getCurrentScope().getLog(CreateProcedureGenerator.class).warning("Cannot get search_path", e);
                    }
                }
                if (originalSearchPath == null) {
                    originalSearchPath = defaultSchema;
                }

                if (!originalSearchPath.equals(schemaName) && !originalSearchPath.startsWith(schemaName + ",") && !originalSearchPath.startsWith("\"" + schemaName + "\",")) {
                    if (database instanceof EnterpriseDBDatabase){
                        sql.add(0, new UnparsedSql("ALTER SESSION SET SEARCH_PATH TO " + database.escapeObjectName(defaultSchema, Schema.class) + ", " + originalSearchPath));
                        sql.add(new UnparsedSql("ALTER SESSION SET CURRENT SCHEMA " + originalSearchPath));
                    } else {
                        sql.add(0, new UnparsedSql("SET SEARCH_PATH TO " + database.escapeObjectName(schemaName, Schema.class) + ", " + originalSearchPath));
                        sql.add(new UnparsedSql("SET CURRENT SCHEMA " + originalSearchPath));
                    }
                }
            }
        }
    }

    /**
     * Convenience method for other classes similar to this that want to be able to modify the procedure text to add the schema
     */
    public static String addSchemaToText(String procedureText, String schemaName, String keywordBeforeName, Database database) {
        if (schemaName == null) {
            return procedureText;
        }
        if ((StringUtil.trimToNull(schemaName) != null) && ChangeLogParserConfiguration.USE_PROCEDURE_SCHEMA.getCurrentValue()) {
            SqlParserFactory sqlParserFactory = Scope.getCurrentScope().getSingleton(SqlParserFactory.class);
            LiquibaseSqlParser sqlParser = sqlParserFactory.getSqlParser();
            StringClauses parsedSql = sqlParser.parse(procedureText, true, true);
            StringClauses.ClauseIterator clauseIterator = parsedSql.getClauseIterator();
            Object next = "START";
            while ((next != null) && !next.toString().equalsIgnoreCase(keywordBeforeName) && clauseIterator.hasNext()) {
                //don't add schema to CREATE PROCEDURE statements inside PACKAGE/PACKAGE-BODY sql
                if ("PACKAGE".equalsIgnoreCase((String) next) &&
                        !("PACKAGE".equalsIgnoreCase(keywordBeforeName) || "BODY".equalsIgnoreCase(keywordBeforeName))
                ) {
                    return procedureText;
                }


                next = clauseIterator.nextNonWhitespace();
            }
            if ((next != null) && clauseIterator.hasNext()) {
                Object procNameClause = clauseIterator.nextNonWhitespace();
                if (procNameClause instanceof String) {
                    String[] nameParts = ((String) procNameClause).split("\\.");
                    String finalName;
                    if (nameParts.length == 1) {
                        finalName = database.escapeObjectName(schemaName, Schema.class) + "." + nameParts[0];
                    } else if (nameParts.length == 2) {
                        finalName = database.escapeObjectName(schemaName, Schema.class) + "." + nameParts[1];
                    } else if (nameParts.length == 3) {
                        finalName = nameParts[0] + "." + database.escapeObjectName(schemaName, Schema.class) + "." + nameParts[2];
                    } else {
                        finalName = (String) procNameClause; //just go with what was there
                    }
                    clauseIterator.replace(finalName);
                }
                procedureText = parsedSql.toString();
            }
        }
        return procedureText;
    }
}
