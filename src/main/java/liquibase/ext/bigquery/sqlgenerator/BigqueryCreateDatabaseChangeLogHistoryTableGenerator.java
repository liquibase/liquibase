package liquibase.ext.bigquery.sqlgenerator;

import com.datical.liquibase.ext.sqlgenerator.CreateDatabaseChangeLogHistoryTableGenerator;
import liquibase.database.Database;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

public class BigqueryCreateDatabaseChangeLogHistoryTableGenerator extends CreateDatabaseChangeLogHistoryTableGenerator {

    @Override
    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
    }


    @Override
    protected String getCharTypeName(Database database) {
        return "string";
    }

}
