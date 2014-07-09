package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.core.SQLiteDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.Statement;
import liquibase.statement.core.CreateViewStatement;
import liquibase.statement.core.DropViewStatement;
import liquibase.structure.core.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a new view.
 */
@DatabaseChange(name="createView", description = "Create a new database view", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateViewChange extends AbstractChange {

    private String catalogName;
	private String schemaName;
	private String viewName;
	private String selectQuery;
	private Boolean replaceIfExists;


    @DatabaseChangeProperty(since = "3.0")
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

    @DatabaseChangeProperty(description = "Name of the view to create")
	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE, description = "SQL for generating the view", exampleValue = "select id, name from person where id > 10")
	public String getSelectQuery() {
		return selectQuery;
	}

	public void setSelectQuery(String selectQuery) {
		this.selectQuery = selectQuery;
	}

    @DatabaseChangeProperty(description = "Use 'create or replace' syntax", since = "1.5")
	public Boolean getReplaceIfExists() {
		return replaceIfExists;
	}

	public void setReplaceIfExists(Boolean replaceIfExists) {
		this.replaceIfExists = replaceIfExists;
	}

	@Override
    public Statement[] generateStatements(ExecutionEnvironment env) {
        List<Statement> statements = new ArrayList<Statement>();

		boolean replaceIfExists = false;
		if (getReplaceIfExists() != null && getReplaceIfExists()) {
			replaceIfExists = true;
		}

		if (!supportsReplaceIfExistsOption(env) && replaceIfExists) {
			statements.add(new DropViewStatement(getCatalogName(), getSchemaName(), getViewName()));
			statements.add(new CreateViewStatement(getCatalogName(), getSchemaName(), getViewName(), getSelectQuery(),
					false));
		} else {
			statements.add(new CreateViewStatement(
					getCatalogName(), getSchemaName(), getViewName(), getSelectQuery(), replaceIfExists));
		}

		return statements.toArray(new Statement[statements.size()]);
	}

	@Override
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

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
        ChangeStatus result = new ChangeStatus();
        try {
            View example = new View(getCatalogName(), getSchemaName(), getViewName());

            View snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, env.getTargetDatabase());
            result.assertComplete(snapshot != null, "View does not exist");

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

	private boolean supportsReplaceIfExistsOption(ExecutionEnvironment env) {
		return !(env.getTargetDatabase() instanceof SQLiteDatabase);
	}

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        Object value = parsedNode.getValue();
        if (value instanceof String) {
            this.setSelectQuery((String) value);
        }
    }
}
