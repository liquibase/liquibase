package liquibase.change.core;

import liquibase.Scope;
import liquibase.change.*;
import liquibase.changelog.ChangeLogParameters;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateViewStatement;
import liquibase.statement.core.DropViewStatement;
import liquibase.statement.core.SetTableRemarksStatement;
import liquibase.structure.core.View;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
    private Boolean fullDefinition;

    private String path;
    private Boolean relativeToChangelogFile;
    private String encoding;
    private String remarks;

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

    @DatabaseChangeProperty(description = "Set to true if selectQuery is the entire view definition. False if the CREATE VIEW header should be added", since = "3.3")
    public Boolean getFullDefinition() {
        return fullDefinition;
    }

    public void setFullDefinition(Boolean fullDefinition) {
        this.fullDefinition = fullDefinition;
    }

    @DatabaseChangeProperty(description = "Path to file containing view definition", since = "3.6")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = super.validate(database);
        if (!validate.hasErrors()) {
            if ((StringUtil.trimToNull(getSelectQuery()) != null) && (StringUtil.trimToNull(getPath()) != null)) {
                validate.addError("Cannot specify both 'path' and a nested view definition in " + Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(this).getName());
            }
            if ((StringUtil.trimToNull(getSelectQuery()) == null) && (StringUtil.trimToNull(getPath()) == null)) {
                validate.addError("For a createView change, you must specify either 'path' or a nested view " +
                        "definition in " +
                        "" + Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(this).getName());
            }

        }
        return validate;
    }

    protected InputStream openSqlStream() throws IOException {
        if (path == null) {
            return null;
        }

        try {
            return StreamUtil.openStream(getPath(), getRelativeToChangelogFile(), getChangeSet(), getResourceAccessor());
        } catch (IOException e) {
            throw new IOException("<" + Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(this).getName() + " path=" + path + "> -Unable to read file", e);
        }
    }

    /**
     * Calculates the checksum based on the contained SQL.
     *
     * @see liquibase.change.AbstractChange#generateCheckSum()
     */
    @Override
    public CheckSum generateCheckSum() {
        if (this.path == null) {
            return super.generateCheckSum();
        }

        InputStream stream = null;
        try {
            stream = openSqlStream();
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        try {
            String selectQuery = this.selectQuery;
            if ((stream == null) && (selectQuery == null)) {
                selectQuery = "";
            }

            String encoding = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding();
            if (selectQuery != null) {
                try {
                    stream = new ByteArrayInputStream(selectQuery.getBytes(encoding));
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(encoding+" is not supported by the JVM, this should not happen according to the JavaDoc of the Charset class");
                }
            }

			CheckSum checkSum = CheckSum.compute(new AbstractSQLChange.NormalizingStream(";", false, false, stream), false);

            return CheckSum.compute(super.generateCheckSum().toString() + ":" + checkSum.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }

    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<>();

        boolean replaceIfExists = false;
        if ((getReplaceIfExists() != null) && getReplaceIfExists()) {
            replaceIfExists = true;
        }

        boolean fullDefinition = false;
        if (this.fullDefinition != null) {
            fullDefinition = this.fullDefinition;
        }

		String selectQuery;
		String path = getPath();
		if (path == null) {
			selectQuery = StringUtil.trimToNull(getSelectQuery());
		} else {
			try {
				InputStream stream = openSqlStream();
				if (stream == null) {
					throw new IOException("File does not exist: " + path);
				}
				selectQuery = StreamUtil.getStreamContents(stream, encoding);
			    if (getChangeSet() != null) {
					ChangeLogParameters parameters = getChangeSet().getChangeLogParameters();
					if (parameters != null) {
						selectQuery = parameters.expandExpressions(selectQuery, getChangeSet().getChangeLog());
					}
				}
			} catch (IOException e) {
				throw new UnexpectedLiquibaseException(e);
			}
		}

        if (!supportsReplaceIfExistsOption(database) && replaceIfExists) {
            statements.add(new DropViewStatement(getCatalogName(), getSchemaName(), getViewName()));
            statements.add(createViewStatement(getCatalogName(), getSchemaName(), getViewName(), selectQuery, false)
                    .setFullDefinition(fullDefinition));
        } else {
            statements.add(createViewStatement(getCatalogName(), getSchemaName(), getViewName(), selectQuery, replaceIfExists)
                    .setFullDefinition(fullDefinition));
        }

        if ((database instanceof OracleDatabase) && (StringUtil.trimToNull(remarks) != null)) {
            SetTableRemarksStatement remarksStatement = new SetTableRemarksStatement(catalogName, schemaName, viewName, remarks);
            if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                statements.add(remarksStatement);
            }
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    protected CreateViewStatement createViewStatement(String catalogName, String schemaName, String viewName, String selectQuery, boolean b) {
        return new CreateViewStatement(catalogName, schemaName, viewName, selectQuery, b);
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
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            View example = new View(getCatalogName(), getSchemaName(), getViewName());

            View snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            result.assertComplete(snapshot != null, "View does not exist");

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    private boolean supportsReplaceIfExistsOption(Database database) {
        return !(database instanceof SQLiteDatabase);
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
