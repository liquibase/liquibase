package liquibase.statement;

import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.core.Column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateExecutablePreparedStatement extends ExecutablePreparedStatementBase {

    private String whereClause;

    private List<String> whereColumnNames = new ArrayList<String>();
    private List<Object> whereParameters = new ArrayList<Object>();

	public UpdateExecutablePreparedStatement(Database database, String catalogName, String schemaName, String tableName, List<ColumnConfig> columns, ChangeSet changeSet, ResourceAccessor resourceAccessor) {
		super(database, catalogName, schemaName, tableName, columns, changeSet, resourceAccessor);
	}

	@Override
	protected String generateSql(List<ColumnConfig> cols) {

		StringBuilder sql = new StringBuilder("UPDATE ").append(database.escapeTableName(getCatalogName(), getSchemaName(), getTableName()));

		StringBuilder params = new StringBuilder(" SET ");
	    for(ColumnConfig column : getColumns()) {
	    	params.append(database.escapeColumnName(getCatalogName(), getSchemaName(), getTableName(), column.getName()));
	    	params.append(" = ");
	        params.append("?, ");
	        cols.add(column);
	    }
	    params.deleteCharAt(params.lastIndexOf(" "));
	    params.deleteCharAt(params.lastIndexOf(","));
	    sql.append(params);
        if (getWhereClause() != null) {
            String fixedWhereClause = "WHERE " + getWhereClause().trim();
            Matcher matcher = Pattern.compile(":name|\\?|:value").matcher(fixedWhereClause);
            StringBuffer sb = new StringBuffer();
            Iterator<String> columnNameIter = getWhereColumnNames().iterator();
            Iterator<Object> paramIter = getWhereParameters().iterator();
            while (matcher.find()) {
                if (matcher.group().equals(":name")) {
                    while (columnNameIter.hasNext()) {
                        String columnName = columnNameIter.next();
                        if (columnName == null) {
                            continue;
                        }
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(database.escapeObjectName(columnName, Column.class)));
                        break;
                    }
                } else if (paramIter.hasNext()) {
                    Object param = paramIter.next();
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(DataTypeFactory.getInstance().fromObject(param, database).objectToSql(param, database)));
                }
            }
            matcher.appendTail(sb);
            fixedWhereClause = sb.toString();
            sql.append(" ").append(fixedWhereClause);
        }

		return sql.toString();
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
