package liquibase.sqlgenerator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.statement.core.AlterTableStatement;
import liquibase.statement.core.DropColumnStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

public class AlterTableGenerator extends AbstractSqlGenerator<AlterTableStatement> {

    @Override
    public boolean supports(AlterTableStatement statement, Database database) {
        return database instanceof MySQLDatabase;
    }

    @Override
    public ValidationErrors validate(AlterTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        for (AddColumnStatement addColumnStatement : statement.getAddColumns()) {
            validationErrors.checkRequiredField("columnName", addColumnStatement.getColumnName());
            validationErrors.checkRequiredField("columnType", addColumnStatement.getColumnType());
            validationErrors.checkRequiredField("tableName", addColumnStatement.getTableName());
            if (!addColumnStatement.getTableName().equals(statement.getTableName())) {
                validationErrors.addError("Only one table is supported");
            }

            if (addColumnStatement.isAutoIncrement() && !addColumnStatement.isPrimaryKey()) {
                validationErrors.addError("Cannot add a non-primary key identity column");
            }

            if (addColumnStatement.getAddBeforeColumn() != null) {
                validationErrors.addError("Cannot add column on specific position");
            }
            if (addColumnStatement.getAddAtPosition() != null) {
                validationErrors.addError("Cannot add column on specific position");
            }
        }
        for (DropColumnStatement dropColumnStatement : statement.getDropColumns()) {
            validationErrors.checkRequiredField("tableName", dropColumnStatement.getTableName());
            validationErrors.checkRequiredField("columnName", dropColumnStatement.getColumnName());
            if (!dropColumnStatement.getTableName().equals(statement.getTableName())) {
                validationErrors.addError("Only one table is supported");
            }
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AlterTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ";

        if (!statement.getAddColumns().isEmpty()) {
            alterTable = generateAddColumns(statement, database, alterTable);
        }
        if (!statement.getDropColumns().isEmpty()) {
            alterTable = generateDropColumns(statement, database, alterTable);
        }

        UnparsedSql result = new UnparsedSql(alterTable, getAffectedColumns(statement));

        List<Sql> returnSql = new ArrayList<Sql>();
        returnSql.add(result);

        for (AddColumnStatement addColumnStatement : statement.getAddColumns()) {
            addUniqueConstrantStatements(addColumnStatement, database, returnSql);
            addForeignKeyStatements(addColumnStatement, database, returnSql);
        }

        return returnSql.toArray(new Sql[returnSql.size()]);
    }

    private String generateAddColumns(AlterTableStatement statement, Database database, String inAlterTable) {
        String alterTable = inAlterTable;
        for (int i = 0; i < statement.getAddColumns().size(); i++) {
            AddColumnStatement addColumnStatement = statement.getAddColumns().get(i);
            if (i > 0) {
                alterTable += ", ";
            }
        
            alterTable += "ADD " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(),
                    addColumnStatement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(addColumnStatement.getColumnType() + (addColumnStatement.isAutoIncrement() ? "{autoIncrement:true}" : "")).toDatabaseDataType(database);

            if (addColumnStatement.isAutoIncrement() && database.supportsAutoIncrement()) {
                AutoIncrementConstraint autoIncrementConstraint = addColumnStatement.getAutoIncrementConstraint();
                alterTable += " " + database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy());
            }

            if (!addColumnStatement.isNullable()) {
                alterTable += " NOT NULL";
            } else {
                if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase || database instanceof MySQLDatabase) {
                    alterTable += " NULL";
                }
            }

            if (addColumnStatement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }

            alterTable += getDefaultClause(addColumnStatement, database);

            if( database instanceof MySQLDatabase && addColumnStatement.getRemarks() != null ) {
                alterTable += " COMMENT '" + addColumnStatement.getRemarks() + "' ";
            }
        }
        return alterTable;
    }

    private String generateDropColumns(AlterTableStatement statement, Database database, String inAlterTable) {
        String alterTable = inAlterTable;
        for (int i = 0; i < statement.getDropColumns().size(); i++) {
            DropColumnStatement dropColumnStatement = statement.getDropColumns().get(i);
            if (i > 0 || !statement.getAddColumns().isEmpty()) {
                alterTable += ", ";
            }

            alterTable += "DROP COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(),
                    dropColumnStatement.getColumnName());
        }
        return alterTable;
    }

    protected Collection<DatabaseObject> getAffectedColumns(AlterTableStatement statement) {
        Collection<DatabaseObject> result = new ArrayList<DatabaseObject>();
        for (AddColumnStatement addColumnStatement : statement.getAddColumns()) {
            result.add(new Column()
                .setRelation(new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                .setName(addColumnStatement.getColumnName()));
        }
        return result;
    }

    protected void addUniqueConstrantStatements(AddColumnStatement statement, Database database, List<Sql> returnSql) {
        if (statement.isUnique()) {
            AddUniqueConstraintStatement addConstraintStmt = new AddUniqueConstraintStatement(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName(), statement.getUniqueStatementName());
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


                AddForeignKeyConstraintStatement addForeignKeyConstraintStatement = new AddForeignKeyConstraintStatement(fkConstraint.getForeignKeyName(), statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName(), null, refSchemaName, refTableName, refColName);
                returnSql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(addForeignKeyConstraintStatement, database)));
            }
        }
    }

    private String getDefaultClause(AddColumnStatement statement, Database database) {
        String clause = "";
        Object defaultValue = statement.getDefaultValue();
        if (defaultValue != null) {
            if (database instanceof MSSQLDatabase) {
                clause += " CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), statement.getColumnName());
            }
            clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
        }
        return clause;
    }

}
