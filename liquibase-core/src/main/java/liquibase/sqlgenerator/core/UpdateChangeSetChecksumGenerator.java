package liquibase.sqlgenerator.core;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateChangeSetChecksumStatement;
import liquibase.statement.core.UpdateStatement;

public class UpdateChangeSetChecksumGenerator extends AbstractSqlGenerator<UpdateChangeSetChecksumStatement> {
	public ValidationErrors validate(UpdateChangeSetChecksumStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("changeSet", statement.getChangeSet());

		return validationErrors;
	}

	public Sql[] generateSql(UpdateChangeSetChecksumStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		ChangeSet changeSet = statement.getChangeSet();

		SqlStatement runStatement = null;
		runStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
				.addNewColumnValue("MD5SUM", changeSet.generateCheckSum().toString()).setWhereClause("ID=? AND AUTHOR=? AND FILENAME=?")
				.addWhereParameters(database.escapeStringForDatabase(changeSet.toString()), changeSet.getAuthor(), changeSet.getFilePath());
		return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
	}

}