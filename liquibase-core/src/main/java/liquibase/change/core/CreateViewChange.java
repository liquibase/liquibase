package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateViewStatement;
import liquibase.statement.core.DropViewStatement;

/**
 * Creates a new view.
 */
@ChangeClass(name="createView", description = "Create View", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateViewChange extends AbstractChange {

    private String catalogName;
	private String schemaName;
	private String viewName;
	private String selectQuery;
	private Boolean replaceIfExists;


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

    @ChangeProperty(requiredForDatabase = "all")
	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

    @ChangeProperty(requiredForDatabase = "all")
	public String getSelectQuery() {
		return selectQuery;
	}

	public void setSelectQuery(String selectQuery) {
		this.selectQuery = selectQuery;
	}

	public Boolean getReplaceIfExists() {
		return replaceIfExists;
	}

	public void setReplaceIfExists(Boolean replaceIfExists) {
		this.replaceIfExists = replaceIfExists;
	}

	public SqlStatement[] generateStatements(Database database) {
		List<SqlStatement> statements = new ArrayList<SqlStatement>();

		boolean replaceIfExists = false;
		if (getReplaceIfExists() != null && getReplaceIfExists()) {
			replaceIfExists = true;
		}

		if (!supportsReplaceIfExistsOption(database) && replaceIfExists) {
			statements.add(new DropViewStatement(getCatalogName(), getSchemaName(), getViewName()));
			statements.add(new CreateViewStatement(getCatalogName(), getSchemaName(), getViewName(), getSelectQuery(),
					false));
		} else {
			statements.add(new CreateViewStatement(
					getCatalogName(), getSchemaName(), getViewName(), getSelectQuery(), replaceIfExists));
		}

		return statements.toArray(new SqlStatement[statements.size()]);
	}

	public String getConfirmationMessage() {
		return "View " + getViewName() + " created";
	}

	@Override
	protected Change[] createInverses() {
		DropViewChange inverse = new DropViewChange();
		inverse.setViewName(getViewName());
		inverse.setSchemaName(getSchemaName());

		return new Change[] { inverse };
	}

	private boolean supportsReplaceIfExistsOption(Database database) {
		return !(database instanceof SQLiteDatabase
		    || database instanceof MSSQLDatabase);
	}

}
