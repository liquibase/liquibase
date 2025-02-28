package liquibase.precondition.core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtil;

public class UniqueConstraintExistsPrecondition extends AbstractPrecondition {

	private String catalogName;
	private String schemaName;
	private String tableName;
	private String columnNames;
	private String constraintName;

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}

	@Override
	public String getSerializedObjectNamespace() {
		return STANDARD_CHANGELOG_NAMESPACE;
	}

	@Override
	public String getName() {
		return "uniqueConstraintExists";
	}

	@Override
	public Warnings warn(Database database) {
		return new Warnings();
	}

	@Override
	public ValidationErrors validate(Database database) {
		ValidationErrors validationErrors = new ValidationErrors(this);
		validationErrors.checkRequiredField("tableName", getTableName());

		if (StringUtil.trimToNull(getConstraintName()) == null && StringUtil.trimToNull(getColumnNames()) == null) {
			validationErrors.addError("constraintName OR columnNames is required for "+getName());
		}
		return validationErrors;
	}

	@Override
	public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
		throws PreconditionFailedException, PreconditionErrorException {

		String schemaName = getSchema(database);

		UniqueConstraint example = new UniqueConstraint(
			StringUtil.trimToNull(getConstraintName()),
			StringUtil.trimToNull(getCatalogName()),
			StringUtil.trimToNull(schemaName),
			StringUtil.trimToNull(getTableName()));

		String columnNames = StringUtil.trimToNull(getColumnNames());
		if (columnNames != null) {
			example.setColumns(toColumns(database, columnNames));
		}

		try {
			if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
				throw new PreconditionFailedException(String.format("%s does not exist", example), changeLog, this);
			}
		} catch (DatabaseException | InvalidExampleException e) {
			throw new PreconditionErrorException(e, changeLog, this);
		}
	}

	private String getSchema(Database database) {
		String schemaName = getSchemaName();
		if (schemaName == null) {
			schemaName = database.getDefaultSchemaName();
		}
		return schemaName;
	}

	private List<Column> toColumns(Database database, String columnNames) {
		return Arrays.stream(columnNames.split("\\s*,\\s*"))
			.map(columnName -> database.correctObjectName(columnName, Column.class))
			.map(Column::new)
			.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		String string = "Unique Constraint Exists Precondition: ";

		if (getConstraintName() != null) {
			string += getConstraintName();
		}

		if (tableName != null) {
			string += " on "+getTableName();

			if (StringUtil.trimToNull(getColumnNames()) != null) {
				string += " columns "+getColumnNames();
			}
		}

		return string;
	}
}
