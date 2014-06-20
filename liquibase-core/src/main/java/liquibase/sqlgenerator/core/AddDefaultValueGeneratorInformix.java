package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorInformix extends AddDefaultValueGenerator {
	@Override
	public int getPriority() {
		return PRIORITY_DATABASE;
	}

	@Override
	public boolean supports(AddDefaultValueStatement statement, ExecutionOptions options) {
		return options.getRuntimeEnvironment().getTargetDatabase() instanceof InformixDatabase;
	}

	@Override
	public ValidationErrors validate(AddDefaultValueStatement addDefaultValueStatement, ExecutionOptions options,
			ActionGeneratorChain chain) {
		ValidationErrors validationErrors = super.validate(addDefaultValueStatement, options, chain);
		if (addDefaultValueStatement.getColumnDataType() == null) {
			validationErrors.checkRequiredField("columnDataType", addDefaultValueStatement.getColumnDataType());
		}
		return validationErrors;
	}

    @Override
    public Action[] generateActions(AddDefaultValueStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        Object defaultValue = statement.getDefaultValue();
		StringBuffer sql = new StringBuffer("ALTER TABLE ");
		sql.append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
		sql.append(" MODIFY (");
		sql.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(),
				statement.getColumnName()));
		sql.append(" ");
		sql.append(DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database));
		sql.append(" DEFAULT ");
		sql.append(DataTypeFactory.getInstance().fromObject(defaultValue, database)
				.objectToSql(defaultValue, database));
		sql.append(")");
		UnparsedSql unparsedSql = new UnparsedSql(sql.toString());
		return new Action[] { unparsedSql };
	}
}