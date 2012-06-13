package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Schema;
import liquibase.database.structure.Table;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorInformix extends AddDefaultValueGenerator {
	@Override
	public int getPriority() {
		return PRIORITY_DATABASE;
	}

	@Override
	public boolean supports(AddDefaultValueStatement statement, Database database) {
		return database instanceof InformixDatabase;
	}

	@Override
	public ValidationErrors validate(AddDefaultValueStatement addDefaultValueStatement, Database database,
			SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors validationErrors = super.validate(addDefaultValueStatement, database, sqlGeneratorChain);
		if (addDefaultValueStatement.getColumnDataType() == null) {
			validationErrors.checkRequiredField("columnDataType", addDefaultValueStatement.getColumnDataType());
		}
		return validationErrors;
	}

	@Override
	public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

		Column column = new Column().setRelation(new Table(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
				.setName(statement.getColumnName());
		Object defaultValue = statement.getDefaultValue();
		StringBuffer sql = new StringBuffer("ALTER TABLE ");
		sql.append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
		sql.append(" MODIFY (");
		sql.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(),
				statement.getColumnName()));
		sql.append(" ");
		sql.append(database.getDataTypeFactory().fromDescription(statement.getColumnDataType()));
		sql.append(" DEFAULT ");
		sql.append(database.getDataTypeFactory().fromObject(defaultValue, database)
				.objectToString(defaultValue, database));
		sql.append(")");
		UnparsedSql unparsedSql = new UnparsedSql(sql.toString(), column);
		return new Sql[] { unparsedSql };
	}
}