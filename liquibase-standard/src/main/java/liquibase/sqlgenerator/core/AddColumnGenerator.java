package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.DatabaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddColumnGenerator extends AbstractSqlGenerator<AddColumnStatement> {

    private static final String REFERENCE_REGEX = "([\\w\\._]+)\\s*\\(\\s*([\\w_]+)\\s*\\)";
    public static final Pattern REFERENCE_PATTERN = Pattern.compile(REFERENCE_REGEX);

    @Override
    public ValidationErrors validate(AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if (statement.isMultiple()) {
            ValidationErrors validationErrors = new ValidationErrors();
            AddColumnStatement firstColumn = statement.getColumns().get(0);

            for (AddColumnStatement column : statement.getColumns()) {
                validationErrors.addAll(validateSingleColumn(column, database));
                if ((firstColumn.getTableName() != null) && !firstColumn.getTableName().equals(column.getTableName())) {
                    validationErrors.addError("All columns must be targeted at the same table");
                }
                if (column.isMultiple()) {
                    validationErrors.addError("Nested multiple add column statements are not supported");
                }
            }
            return validationErrors;
        } else {
            return validateSingleColumn(statement, database);
        }
    }

    private ValidationErrors validateSingleColumn(AddColumnStatement statement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        if (!ObjectUtil.defaultIfNull(statement.getComputed(), false)) {
            validationErrors.checkRequiredField("columnType", statement.getColumnType());
        }
        validationErrors.checkRequiredField("tableName", statement.getTableName());

        try {
            if (statement.isPrimaryKey() && ((database instanceof AbstractDb2Database) ||
                    (database instanceof DerbyDatabase) || (database instanceof SQLiteDatabase) ||
                    (database instanceof H2Database && database.getDatabaseMajorVersion() < 2))) {
                validationErrors.addError("Cannot add a primary key column");
            }
        } catch (DatabaseException e) {
            //do nothing
        }

        // TODO HsqlDatabase autoincrement on non primary key? other databases?
        if ((database instanceof MySQLDatabase) && statement.isAutoIncrement() && !statement.isPrimaryKey()) {
            validationErrors.addError("Cannot add a non-primary key identity column");
        }

        if (!(database instanceof MySQLDatabase || database instanceof H2Database)) {
            validationErrors.checkDisallowedField("addAfterColumn", statement.getAddAfterColumn(), database, database.getClass());
        }

        if (!((database instanceof H2Database || database instanceof HsqlDatabase))) {
            validationErrors.checkDisallowedField("addBeforeColumn", statement.getAddBeforeColumn(), database, database.getClass());
        }

        //no databases liquibase supports currently supports adding columns at a given position. Firebird only allows position on alters
        validationErrors.checkDisallowedField("addAtPosition", statement.getAddAtPosition(), database, database.getClass());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if (statement.isMultiple()) {
            return generateMultipleColumns(statement.getColumns(), database);
        } else {
            return generateSingleColumn(statement, database);
        }
    }

    private Sql[] generateMultipleColumns(List<AddColumnStatement> columns, Database database) {
        List<Sql> result = new ArrayList<>();
        if (database instanceof MySQLDatabase) {
            final StringBuilder alterTable = new StringBuilder(generateSingleColumBaseSQL(columns.get(0), database));
            for (int i = 0; i < columns.size(); i++) {
                alterTable.append(generateSingleColumnSQL(columns.get(i), database));
                if (i < (columns.size() - 1)) {
                    alterTable.append(",");
                }
            }
            result.add(new UnparsedSql(alterTable.toString(), getAffectedColumns(columns)));

            for (AddColumnStatement statement : columns) {
                addUniqueConstraintStatements(statement, database, result);
                addForeignKeyStatements(statement, database, result);
            }

        } else {
            for (AddColumnStatement column : columns) {
                result.addAll(Arrays.asList(generateSingleColumn(column, database)));
            }
        }
        return result.toArray(EMPTY_SQL);
    }

    protected Sql[] generateSingleColumn(AddColumnStatement statement, Database database) {
        String alterTable = generateSingleColumBaseSQL(statement, database);
        alterTable += generateSingleColumnSQL(statement, database);

        List<Sql> returnSql = new ArrayList<>();
        returnSql.add(new UnparsedSql(alterTable, getAffectedColumn(statement)));

        addUniqueConstraintStatements(statement, database, returnSql);
        addForeignKeyStatements(statement, database, returnSql);

        return returnSql.toArray(EMPTY_SQL);
    }

    protected String generateSingleColumBaseSQL(AddColumnStatement statement, Database database) {
        return "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
    }

    protected String generateSingleColumnSQL(AddColumnStatement statement, Database database) {
        DatabaseDataType columnType = null;

        if (statement.getColumnType() != null) {
            columnType = DataTypeFactory.getInstance().fromDescription(statement.getColumnType() + (statement.isAutoIncrement() ? "{autoIncrement:true}" : ""), database).toDatabaseDataType(database);
        }

        String alterTable = " ADD " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName());

        if (columnType != null) {
            alterTable += " " + columnType;
        }

        if (statement.isAutoIncrement() && database.supportsAutoIncrement()) {
            AutoIncrementConstraint autoIncrementConstraint = statement.getAutoIncrementConstraint();
            alterTable += " " + database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy(), autoIncrementConstraint.getGenerationType(), autoIncrementConstraint.getDefaultOnNull());
        }

        alterTable += getDefaultClause(statement, database);

        if (!statement.isNullable()) {
            for (ColumnConstraint constraint : statement.getConstraints()) {
                if (constraint instanceof NotNullConstraint) {
                    NotNullConstraint notNullConstraint = (NotNullConstraint) constraint;
                    if (StringUtil.isNotEmpty(notNullConstraint.getConstraintName())) {
                        alterTable += " CONSTRAINT " + database.escapeConstraintName(notNullConstraint.getConstraintName());
                        break;
                    }
                }
            }
            alterTable += " NOT NULL";
            if (database instanceof OracleDatabase) {
                alterTable += !statement.shouldValidateNullable() ? " ENABLE NOVALIDATE " : "";
            }
        } else {
            if ((database instanceof SybaseDatabase) || (database instanceof SybaseASADatabase) || (database
                    instanceof MySQLDatabase) || ((database instanceof MSSQLDatabase) && columnType != null && "timestamp".equalsIgnoreCase(columnType.toString()))) {
                alterTable += " NULL";
            }
        }

        if (statement.isPrimaryKey()) {
            alterTable += " PRIMARY KEY";
            if (database instanceof OracleDatabase) {
                alterTable += !statement.shouldValidatePrimaryKey() ? " ENABLE NOVALIDATE " : "";
            }
        }

        if ((database instanceof MySQLDatabase) && (statement.getRemarks() != null)) {
            alterTable += " COMMENT '" + database.escapeStringForDatabase(StringUtil.trimToEmpty(statement.getRemarks())) + "' ";
        }

        if ((statement.getAddBeforeColumn() != null) && !statement.getAddBeforeColumn().isEmpty()) {
            alterTable += " BEFORE " + database.escapeColumnName(statement.getSchemaName(), statement.getSchemaName(), statement.getTableName(), statement.getAddBeforeColumn()) + " ";
        }

        if ((statement.getAddAfterColumn() != null) && !statement.getAddAfterColumn().isEmpty()) {
            alterTable += " AFTER " + database.escapeColumnName(statement.getSchemaName(), statement.getSchemaName(), statement.getTableName(), statement.getAddAfterColumn());
        }

        return alterTable;
    }

    protected Column[] getAffectedColumns(List<AddColumnStatement> columns) {
        List<Column> cols = new ArrayList<>();
        for (AddColumnStatement c : columns) {
            cols.add(getAffectedColumn(c));
        }
        return cols.toArray(new Column[0]);
    }

    protected Column getAffectedColumn(AddColumnStatement statement) {
        return new Column()
                .setRelation(new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                .setName(statement.getColumnName());
    }

    protected void addUniqueConstraintStatements(AddColumnStatement statement, Database database, List<Sql> returnSql) {
        if (statement.isUnique()) {
            AddUniqueConstraintStatement addConstraintStmt = new AddUniqueConstraintStatement(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), ColumnConfig.arrayFromNames(statement.getColumnName()), statement.getUniqueStatementName());
            addConstraintStmt.setShouldValidate(statement.shouldValidateUnique());
            returnSql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(addConstraintStmt, database)));
        }
    }

    protected void addForeignKeyStatements(AddColumnStatement statement, Database database, List<Sql> returnSql) {
        for (ColumnConstraint constraint : statement.getConstraints()) {
            if (constraint instanceof ForeignKeyConstraint) {
                ForeignKeyConstraint fkConstraint = (ForeignKeyConstraint) constraint;
                String refSchemaName = null;
                String refTableName;
                String refColName;
                if (fkConstraint.getReferences() != null) {
                    Matcher referencesMatcher = REFERENCE_PATTERN.matcher(fkConstraint.getReferences());
                    if (!referencesMatcher.matches()) {
                        throw new UnexpectedLiquibaseException("Don't know how to find table and column names from " + fkConstraint.getReferences());
                    }
                    refTableName = referencesMatcher.group(1);
                    refColName = referencesMatcher.group(2);
                } else {
                    refTableName = ((ForeignKeyConstraint) constraint).getReferencedTableName();
                    refColName = ((ForeignKeyConstraint) constraint).getReferencedColumnNames();
                }

                if (refTableName.indexOf(".") > 0) {
                    refSchemaName = refTableName.split("\\.")[0];
                    refTableName = refTableName.split("\\.")[1];
                }


                AddForeignKeyConstraintStatement addForeignKeyConstraintStatement = new AddForeignKeyConstraintStatement(fkConstraint.getForeignKeyName(), statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), ColumnConfig.arrayFromNames(statement.getColumnName()), null, refSchemaName, refTableName, ColumnConfig.arrayFromNames(refColName));
                if (fkConstraint.isDeleteCascade()) {
                    addForeignKeyConstraintStatement.setOnDelete("CASCADE");
                }
                addForeignKeyConstraintStatement.setShouldValidate(fkConstraint.shouldValidateForeignKey());
                returnSql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(addForeignKeyConstraintStatement, database)));
            }
        }
    }

    private String getDefaultClause(AddColumnStatement statement, Database database) {
        String clause = "";
        Object defaultValue = statement.getDefaultValue();
        if (defaultValue != null) {
            if ((database instanceof OracleDatabase) && defaultValue.toString().startsWith("GENERATED ALWAYS ")) {
                clause += " " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
            } else {
                if (database instanceof MSSQLDatabase) {
                    String constraintName = statement.getDefaultValueConstraintName();
                    if (constraintName == null) {
                        constraintName = ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), statement.getColumnName());
                    }
                    clause += " CONSTRAINT " + constraintName;
                }
                if (defaultValue instanceof DatabaseFunction) {
                    clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
                } else {
                    clause += " DEFAULT " + DataTypeFactory.getInstance().fromDescription(statement.getColumnType(), database).objectToSql(defaultValue, database);
                }
            }
        }
        return clause;
    }
}
