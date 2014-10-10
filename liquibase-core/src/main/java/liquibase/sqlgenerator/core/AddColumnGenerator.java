package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.structure.core.Schema;
import liquibase.datatype.DataTypeFactory;
import liquibase.database.core.*;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.exception.ValidationErrors;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.ForeignKeyConstraint;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddColumnGenerator extends AbstractSqlGenerator<AddColumnStatement> {

    @Override
    public ValidationErrors validate(AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if (statement.isMultiple()) {
            ValidationErrors validationErrors = new ValidationErrors();
            AddColumnStatement firstColumn = statement.getColumns().get(0);

            for (AddColumnStatement column : statement.getColumns()) {
                validationErrors.addAll(validateSingleColumn(column, database));
                if (firstColumn.getTableName() != null && !firstColumn.getTableName().equals(column.getTableName())) {
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
        validationErrors.checkRequiredField("columnType", statement.getColumnType());
        validationErrors.checkRequiredField("tableName", statement.getTableName());

        if (statement.isPrimaryKey() && (database instanceof H2Database
                || database instanceof DB2Database
                || database instanceof DerbyDatabase
                || database instanceof SQLiteDatabase)) {
            validationErrors.addError("Cannot add a primary key column");
        }

        // TODO HsqlDatabase autoincrement on non primary key? other databases?
        if (database instanceof MySQLDatabase && statement.isAutoIncrement() && !statement.isPrimaryKey()) {
            validationErrors.addError("Cannot add a non-primary key identity column");
        }
        
        // TODO is this feature valid for other databases?
        if ((statement.getAddAfterColumn() != null) && !(database instanceof MySQLDatabase)) {
        	validationErrors.addError("Cannot add column on specific position");
        }
        if ((statement.getAddBeforeColumn() != null) && !((database instanceof H2Database) || (database instanceof HsqlDatabase))) {
        	validationErrors.addError("Cannot add column on specific position");
        }
        if ((statement.getAddAtPosition() != null) && !(database instanceof FirebirdDatabase)) {
        	validationErrors.addError("Cannot add column on specific position");
        }
        
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
        List<Sql> result = new ArrayList<Sql>();
        if (database instanceof MySQLDatabase) {
            String alterTable = generateSingleColumBaseSQL(columns.get(0), database);
            for (int i = 0; i < columns.size(); i++) {
                alterTable += generateSingleColumnSQL(columns.get(i), database);
                if (i < columns.size() - 1) {
                    alterTable += ",";
                }
            }
            result.add(new UnparsedSql(alterTable, getAffectedColumns(columns)));
        } else {
            for (AddColumnStatement column : columns) {
                result.addAll(Arrays.asList(generateSingleColumn(column, database)));
            }
        }
        return result.toArray(new Sql[result.size()]);
    }

    protected Sql[] generateSingleColumn(AddColumnStatement statement, Database database) {
        String alterTable = generateSingleColumBaseSQL(statement, database);
        alterTable += generateSingleColumnSQL(statement, database);

        List<Sql> returnSql = new ArrayList<Sql>();
        returnSql.add(new UnparsedSql(alterTable, getAffectedColumn(statement)));

        addUniqueConstrantStatements(statement, database, returnSql);
        addForeignKeyStatements(statement, database, returnSql);

        return returnSql.toArray(new Sql[returnSql.size()]);
    }

    protected String generateSingleColumBaseSQL(AddColumnStatement statement, Database database) {
        return "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
    }

    protected String generateSingleColumnSQL(AddColumnStatement statement, Database database) {
        String alterTable = " ADD " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnType() + (statement.isAutoIncrement() ? "{autoIncrement:true}" : ""), database).toDatabaseDataType(database);

        if (statement.isAutoIncrement() && database.supportsAutoIncrement()) {
            AutoIncrementConstraint autoIncrementConstraint = statement.getAutoIncrementConstraint();
            alterTable += " " + database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy());
        }

        if (!statement.isNullable()) {
            alterTable += " NOT NULL";
        } else {
            if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase || database instanceof MySQLDatabase) {
                alterTable += " NULL";
            }
        }

        if (statement.isPrimaryKey()) {
            alterTable += " PRIMARY KEY";
        }

        alterTable += getDefaultClause(statement, database);

        if( database instanceof MySQLDatabase && statement.getRemarks() != null ) {
            alterTable += " COMMENT '" + statement.getRemarks() + "' ";
        }

        if (statement.getAddAfterColumn() != null && !statement.getAddAfterColumn().isEmpty()) {
            alterTable += " AFTER `" + statement.getAddAfterColumn() + "` ";
        }

        return alterTable;
    }

    protected Column[] getAffectedColumns(List<AddColumnStatement> columns) {
        List<Column> cols = new ArrayList<Column>();
        for (AddColumnStatement c : columns) {
            cols.add(getAffectedColumn(c));
        }
        return cols.toArray(new Column[cols.size()]);
    }

    protected Column getAffectedColumn(AddColumnStatement statement) {
        return new Column()
                .setRelation(new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                .setName(statement.getColumnName());
    }

    protected void addUniqueConstrantStatements(AddColumnStatement statement, Database database, List<Sql> returnSql) {
        if (statement.isUnique()) {
            AddUniqueConstraintStatement addConstraintStmt = new AddUniqueConstraintStatement(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), ColumnConfig.arrayFromNames(statement.getColumnName()), statement.getUniqueStatementName());
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
                    Matcher referencesMatcher = Pattern.compile("([\\w\\._]+)\\(([\\w_]+)\\)").matcher(fkConstraint.getReferences());
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
                returnSql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(addForeignKeyConstraintStatement, database)));
            }
        }
    }

    private String getDefaultClause(AddColumnStatement statement, Database database) {
        String clause = "";
        Object defaultValue = statement.getDefaultValue();
        if (defaultValue != null) {
            if (database instanceof OracleDatabase && defaultValue.toString().startsWith("GENERATED ALWAYS ")) {
                clause += " " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
            } else {
                if (database instanceof MSSQLDatabase) {
                    clause += " CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), statement.getColumnName());
                }
                clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
            }
        }
        return clause;
    }
}
