package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BooleanType;
import liquibase.datatype.core.CharType;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

public class AddDefaultValueGenerator extends AbstractSqlGenerator<AddDefaultValueStatement> {

    @Override
    public ValidationErrors validate(AddDefaultValueStatement addDefaultValueStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = addDefaultValueStatement.getDefaultValue();

        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("defaultValue", defaultValue);
        validationErrors.checkRequiredField("columnName", addDefaultValueStatement.getColumnName());
        validationErrors.checkRequiredField("tableName", addDefaultValueStatement.getTableName());
        if (!database.supportsSequences() && (defaultValue instanceof SequenceNextValueFunction)) {
            validationErrors.addError("Database "+database.getShortName()+" does not support sequences");
        }
        if (database instanceof HsqlDatabase) {
            if (defaultValue instanceof SequenceNextValueFunction) {
                validationErrors.addError("Database " + database.getShortName() + " does not support adding sequence-based default values");
            } else if ((defaultValue instanceof DatabaseFunction) && !HsqlDatabase.supportsDefaultValueComputed
                (addDefaultValueStatement.getColumnDataType(), defaultValue.toString())) {
                validationErrors.addError("Database " + database.getShortName() + " does not support adding function-based default values");
            }
        }

        String columnDataType = addDefaultValueStatement.getColumnDataType();
        if (columnDataType != null) {
            LiquibaseDataType dataType = DataTypeFactory.getInstance().fromDescription(columnDataType, database);
            boolean typeMismatch = false;
            if (dataType instanceof BooleanType) {
                if (!(defaultValue instanceof Boolean)) {
                    typeMismatch = true;
                }
            } else if (dataType instanceof CharType) {
                if (!(defaultValue instanceof String) && !(defaultValue instanceof DatabaseFunction)) {
                    typeMismatch = true;
                }
            }

            if (typeMismatch) {
                validationErrors.addError("Default value of "+defaultValue+" does not match defined type of "+columnDataType);
            }
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = statement.getDefaultValue();
        return new Sql[] {
                new UnparsedSql("ALTER TABLE "
                        + database.escapeTableName(
                                statement.getCatalogName(),
                                statement.getSchemaName(),
                                statement.getTableName() )
                        + " ALTER COLUMN  "
                        + database.escapeColumnName(
                                statement.getCatalogName(),
                                statement.getSchemaName(),
                                statement.getTableName(),
                                statement.getColumnName()
                        ) + " SET DEFAULT "
                        + DataTypeFactory.getInstance()
                            .fromObject(defaultValue, database)
                            .objectToSql(defaultValue, database),
                    getAffectedColumn(statement) )
        };
    }

    protected Column getAffectedColumn(AddDefaultValueStatement statement) {
        return new Column()
                .setRelation(new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                .setName(statement.getColumnName());
    }
}
