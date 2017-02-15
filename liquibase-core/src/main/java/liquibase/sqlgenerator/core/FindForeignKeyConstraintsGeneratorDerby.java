package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;


/**
 * implements FindForeignKeyConstraintsGenerator for the Derby database.
 * 
 * @author Thomas Beckmann
 */
public class FindForeignKeyConstraintsGeneratorDerby extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
	@Override
	public int getPriority() {
        return PRIORITY_DATABASE;
	}

	@Override
	public boolean supports(FindForeignKeyConstraintsStatement statement, Database database) {
		return database instanceof DerbyDatabase;
	}

	@Override
	public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, Database database,
			SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
		return validationErrors;
	}

	@Override
	public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		final CatalogAndSchema schema = database.correctSchema(new CatalogAndSchema(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName()));
		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append("co.constraintname AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(", ");
		sb.append("t.tablename AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
		sb.append("t2.tablename AS ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(" ");
		sb.append("FROM sys.sysconstraints co ");
		sb.append("JOIN sys.sysschemas sc ON co.schemaid = sc.schemaid ");
		sb.append("JOIN sys.systables t ON co.tableid = t.tableid ");
		sb.append("JOIN sys.sysforeignkeys f ON co.constraintid = f.constraintid ");
		sb.append("JOIN sys.sysconglomerates cg ON f.conglomerateid = cg.conglomerateid ");
		sb.append("JOIN sys.sysconstraints co2 ON f.keyconstraintid = co2.constraintid ");
		sb.append("JOIN sys.systables t2 ON co2.tableid = t2.tableid ");
		sb.append("JOIN sys.syskeys k ON co2.constraintid = k.constraintid ");
		sb.append("JOIN sys.sysconglomerates cg2 ON k.conglomerateid = cg2.conglomerateid ");
		sb.append("WHERE co.type = 'F' ");
		if (schema.getCatalogName() != null) {
		  sb.append("AND sc.schemaname = '").append(schema.getCatalogName()).append("' ");
		}
		sb.append("AND t.tablename = '").append(statement.getBaseTableName()).append("'");
		return new Sql[] {
				new UnparsedSql(sb.toString())
		};
	}
}
