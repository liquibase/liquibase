package liquibase.statement;

import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.resource.ResourceAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static liquibase.util.SqlUtil.replacePredicatePlaceholders;

public class UpdateExecutablePreparedStatement extends ExecutablePreparedStatementBase {

    private String whereClause;

    private List<String> whereColumnNames = new ArrayList<>();
    private List<Object> whereParameters = new ArrayList<>();

	public UpdateExecutablePreparedStatement(Database database, String catalogName, String schemaName, String tableName, List<ColumnConfig> columns, ChangeSet changeSet, ResourceAccessor resourceAccessor) {
		super(database, catalogName, schemaName, tableName, columns, changeSet, resourceAccessor);
	}

    @Override
    public boolean continueOnError() {
        return false;
    }


    public String getWhereClause() {
        return whereClause;
    }

    public UpdateExecutablePreparedStatement setWhereClause(String whereClause) {
        this.whereClause = whereClause;

        return this;
    }

    public UpdateExecutablePreparedStatement addWhereParameter(Object value) {
        this.whereParameters.add(value);

        return this;
    }

    public UpdateExecutablePreparedStatement addWhereParameters(Object... value) {
        this.whereParameters.addAll(Arrays.asList(value));

        return this;
    }

    public UpdateExecutablePreparedStatement addWhereColumnName(String value) {
        this.whereColumnNames.add(value);

        return this;
    }

    public List<Object> getWhereParameters() {
        return whereParameters;
    }

    public List<String> getWhereColumnNames() {
        return whereColumnNames;
    }
}
