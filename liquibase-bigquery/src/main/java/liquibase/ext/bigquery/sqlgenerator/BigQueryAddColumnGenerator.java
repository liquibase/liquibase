package liquibase.ext.bigquery.sqlgenerator;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

import liquibase.database.Database;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.sqlgenerator.core.AddColumnGenerator;
import liquibase.statement.core.AddColumnStatement;

public class BigQueryAddColumnGenerator extends AddColumnGenerator {

	@Override
	protected String generateSingleColumnSQL(AddColumnStatement statement, Database database) {
		return super.generateSingleColumnSQL(statement, database).replace(" ADD ", " ADD COLUMN ");
	}

	@Override
	public int getPriority() {
		return BIGQUERY_PRIORITY_DATABASE;
	}

	@Override
	public boolean supports(AddColumnStatement statement, Database database) {
		return database instanceof BigqueryDatabase;
	}
}
