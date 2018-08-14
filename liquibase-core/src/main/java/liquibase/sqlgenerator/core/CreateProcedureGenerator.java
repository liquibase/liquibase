package liquibase.sqlgenerator.core;

import liquibase.configuration.GlobalConfiguration;
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
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CreateProcedureGenerator extends AbstractSqlGenerator<CreateProcedureStatement> {
    @Override
    public ValidationErrors validate(CreateProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureText", statement.getProcedureText());
        if (statement.getReplaceIfExists() != null) {
            if (database instanceof MSSQLDatabase) {
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
        if ((schemaName == null) && LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class)
            .getAlwaysOverrideStoredLogicSchema()) {
            schemaName = database.getDefaultSchemaName();
        }

        String procedureText = addSchemaToText(statement.getProcedureText(), schemaName, "PROCEDURE", database);

        if ((statement.getReplaceIfExists() != null) && statement.getReplaceIfExists()) {
            String fullyQualifiedName = database.escapeObjectName(statement.getProcedureName(), StoredProcedure.class);
            if (schemaName != null) {
                fullyQualifiedName = database.escapeObjectName(schemaName, Schema.class) + "." + fullyQualifiedName;
            }
            sql.add(new UnparsedSql("if object_id('" + fullyQualifiedName + "', 'p') is null exec ('create procedure " + fullyQualifiedName + " as select 1 a')"));

            StringClauses parsedSql = SqlParser.parse(procedureText, true, true);
            StringClauses.ClauseIterator clauseIterator = parsedSql.getClauseIterator();
            Object next = "START";
            while ((next != null) && !("create".equalsIgnoreCase(next.toString()) || "alter".equalsIgnoreCase(next
                .toString())) && clauseIterator.hasNext()) {
                next = clauseIterator.nextNonWhitespace();
            }
            clauseIterator.replace("ALTER");

            procedureText = parsedSql.toString();
        }

        procedureText = removeTrailingDelimiter(procedureText, statement.getEndDelimiter());

        if ((database instanceof MSSQLDatabase) && procedureText.toLowerCase().contains("merge") && !procedureText
            .endsWith(";")) { //mssql "AS MERGE" procedures need a trailing ; (regardless of the end delimiter)
            StringClauses parsed = SqlParser.parse(procedureText);
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
        if (database instanceof Db2zDatabase & procedureText.toLowerCase().contains("replace")) {
            procedureText = procedureText.replace("OR REPLACE", "");
            procedureText = procedureText.replaceAll("[\\s]{2,}", " ");
        }
        sql.add(new UnparsedSql(procedureText, statement.getEndDelimiter()));
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
        if ((StringUtil.trimToNull(schemaName) != null) && !LiquibaseConfiguration.getInstance().getProperty(ChangeLogParserCofiguration.class, ChangeLogParserCofiguration.USE_PROCEDURE_SCHEMA).getValue(Boolean.class)) {
            String defaultSchema = database.getDefaultSchemaName();
            if (database instanceof OracleDatabase) {
                sql.add(0, new UnparsedSql("ALTER SESSION SET CURRENT_SCHEMA=" + database.escapeObjectName(schemaName, Schema.class)));
                sql.add(new UnparsedSql("ALTER SESSION SET CURRENT_SCHEMA=" + database.escapeObjectName(defaultSchema, Schema.class)));
            } else if (database instanceof AbstractDb2Database) {
                sql.add(0, new UnparsedSql("SET CURRENT SCHEMA " + schemaName));
                sql.add(new UnparsedSql("SET CURRENT SCHEMA " + defaultSchema));
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
        if ((StringUtil.trimToNull(schemaName) != null) && LiquibaseConfiguration.getInstance().getProperty(ChangeLogParserCofiguration.class, ChangeLogParserCofiguration.USE_PROCEDURE_SCHEMA).getValue(Boolean.class)) {
            StringClauses parsedSql = SqlParser.parse(procedureText, true, true);
            StringClauses.ClauseIterator clauseIterator = parsedSql.getClauseIterator();
            Object next = "START";
            while ((next != null) && !next.toString().equalsIgnoreCase(keywordBeforeName) && clauseIterator.hasNext()) {
                if (!"PACKAGE".equalsIgnoreCase(keywordBeforeName) && "PACKAGE".equalsIgnoreCase((String) next)) {
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