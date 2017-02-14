package liquibase.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import liquibase.change.ColumnConfig;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.resource.ResourceAccessor;

public class BatchInsertExecutablePreparedStatement extends InsertExecutablePreparedStatement {

	private static List<ColumnConfig> toColumnConfigList(List<LoadDataColumnConfig> columns) {
		List<ColumnConfig> columnConfigList = new ArrayList<ColumnConfig>(columns.size());
		for (LoadDataColumnConfig column : columns)
			columnConfigList.add(column);
		return columnConfigList;
	}

	private static List<InsertExecutablePreparedStatement> toInsertStatementList(List<SqlStatement> statements) {
		List<InsertExecutablePreparedStatement> insertStatements = new ArrayList<InsertExecutablePreparedStatement>(
				statements.size());
		for (SqlStatement statement : statements)
			insertStatements.add((InsertExecutablePreparedStatement) statement);
		return insertStatements;
	}

	private final List<InsertExecutablePreparedStatement> insertStatements;

	public BatchInsertExecutablePreparedStatement(Database database, String catalogName, String schemaName,
			String tableName, List<LoadDataColumnConfig> columns, ChangeSet changeSet,
			ResourceAccessor resourceAccessor, List<SqlStatement> insertStatements) {
		super(database, catalogName, schemaName, tableName, toColumnConfigList(columns), changeSet, resourceAccessor);
		this.insertStatements = toInsertStatementList(insertStatements);
	}

	@Override
	protected void attachParams(List<ColumnConfig> cols, PreparedStatement stmt)
			throws SQLException, DatabaseException {
		for (InsertExecutablePreparedStatement insertStatement : insertStatements) {
			attachParams(insertStatement.getColumns(), stmt);
			stmt.addBatch();
		}
	}

	@Override
	protected void executePreparedStatement(PreparedStatement stmt) throws SQLException {
		stmt.executeBatch();
	}

}