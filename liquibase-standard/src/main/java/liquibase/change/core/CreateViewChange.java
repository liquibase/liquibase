package liquibase.change.core;

import liquibase.ChecksumVersion;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.change.*;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.PropertyExpandingStream;
import liquibase.database.Database;
import liquibase.database.core.*;
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
import liquibase.statement.core.SetViewRemarksStatement;
import liquibase.structure.core.View;
import liquibase.util.FileUtil;
import liquibase.util.ObjectUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static liquibase.statement.SqlStatement.EMPTY_SQL_STATEMENT;

/**
 * Creates a new view.
 */
@DatabaseChange(name = "createView", description = "Create a new database view", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateViewChange extends AbstractChange implements ReplaceIfExists {

    @Setter
    private String catalogName;
    @Setter
    private String schemaName;
    @Setter
    private String viewName;
    @Setter
    private String selectQuery;
    private Boolean replaceIfExists;
    @Setter
    private Boolean fullDefinition;

    @Setter
    private String path;
    @Setter
    private Boolean relativeToChangelogFile;
    @Setter
    private String encoding;
    @Setter
    private String remarks;

    @DatabaseChangeProperty(since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the view to create")
    public String getViewName() {
        return viewName;
    }

    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE, description = "SQL for generating the view",
        exampleValue = "SELECT id, name FROM person WHERE id > 10")
    public String getSelectQuery() {
        return selectQuery;
    }

    @DatabaseChangeProperty(description = "Use 'CREATE OR REPLACE' syntax", since = "1.5")
    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    @Override
    public void setReplaceIfExists(Boolean replaceIfExists) {
        this.replaceIfExists = replaceIfExists;
    }

    @DatabaseChangeProperty(since = "3.3",
        description = "Set to true if selectQuery is the entire view definition. Set to false if the CREATE VIEW header should be added")
    public Boolean getFullDefinition() {
        return fullDefinition;
    }

    @DatabaseChangeProperty(description = "Path to the file containing the view definition. Specifying 'path' is an alternative to selectQuery.", since = "3.6")
    public String getPath() {
        return path;
    }

    @DatabaseChangeProperty(description = "Specifies whether the file path is relative to the changelog file " +
        "rather than looked up in the search path. Default: false.")
    public Boolean getRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    @DatabaseChangeProperty(description = "Encoding used in the file you specify in 'path'")
    public String getEncoding() {
        return encoding;
    }

    @DatabaseChangeProperty(description = "A brief descriptive comment stored in the view metadata")
    public String getRemarks() {
        return remarks;
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
                        Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(this).getName());
            }

        }
        return validate;
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return false;
    }

    protected InputStream openSqlStream() throws IOException {
        if (path == null) {
            return null;
        }

        try {
            ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();

            if (ObjectUtil.defaultIfNull(getRelativeToChangelogFile(), false)) {
                return resourceAccessor.get(getChangeSet().getChangeLog().getPhysicalFilePath()).resolveSibling(getPath()).openInputStream();
            } else {
                return resourceAccessor.getExisting(getPath()).openInputStream();
            }
        } catch (IOException e) {
            throw new IOException("<" + Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(this).getName() + " path=" + path + "> -Unable to read file", e);
        }
    }

    @Override
    public String[] getExcludedFieldFilters(ChecksumVersion version) {
        if (version.lowerOrEqualThan(ChecksumVersion.V8)) {
            return new String[0];
        }
        return new String[] {
                "path",
                "relativeToChangelogFile",
                "selectQuery",
                "encoding"
        };
    }

    /**
     * Calculates the checksum based on the contained SQL.
     *
     * @see Change#generateCheckSum(ChecksumVersion)
     */
    @Override
    public CheckSum generateCheckSum() {
        ChecksumVersion version = Scope.getCurrentScope().getChecksumVersion();
        if (version.lowerOrEqualThan(ChecksumVersion.V8)) {
            return generateCheckSumV8();
        }
        return generateCheckSumLatest(version);
    }

    private CheckSum generateCheckSumLatest(ChecksumVersion version) {
        InputStream stream = null;
        CheckSum checkSum;
        try {
            if (getPath() == null) {
                String selectQuery = this.selectQuery;
                Charset encoding = GlobalConfiguration.FILE_ENCODING.getCurrentValue();
                if(selectQuery != null) {
                    stream = new ByteArrayInputStream(selectQuery.getBytes(encoding));
                }
            }
            else {
                stream = openSqlStream();
                stream = new PropertyExpandingStream(this.getChangeSet(), stream);
            }
            checkSum = CheckSum.compute(new AbstractSQLChange.NormalizingStream(stream), false);
            return CheckSum.compute(super.generateCheckSum().toString() + ":" + checkSum);

        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    @Deprecated
    private CheckSum generateCheckSumV8() {
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

            String encoding = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
            if (selectQuery != null) {
                try {
                    stream = new ByteArrayInputStream(selectQuery.getBytes(encoding));
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(encoding+" is not supported by the JVM, this should not happen according to the JavaDoc of the Charset class");
                }
            }

            CheckSum checkSum = CheckSum.compute(new NormalizingStreamV8(";", false, false, stream), false);

            return CheckSum.compute(super.generateCheckSum().toString() + ":" + checkSum);
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

        boolean replaceIfExists = (getReplaceIfExists() != null) && getReplaceIfExists();

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
					throw new IOException(FileUtil.getFileNotFoundMessage(path));
				}
				selectQuery = StreamUtil.readStreamAsString(stream, encoding);
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

        List<Class<?>> databaseSupportsViewComments = Arrays.asList(OracleDatabase.class, PostgresDatabase.class, MSSQLDatabase.class, DB2Database.class,
                SybaseASADatabase.class);
        boolean supportsViewComments = databaseSupportsViewComments.stream().anyMatch(clazz -> clazz.isInstance(database));

        if (supportsViewComments && (StringUtil.trimToNull(remarks) != null)) {
            SetViewRemarksStatement remarksStatement = new SetViewRemarksStatement(catalogName, schemaName, viewName, remarks);
            if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                statements.add(remarksStatement);
            }
        }

        return statements.toArray(EMPTY_SQL_STATEMENT);
    }

    protected CreateViewStatement createViewStatement(String catalogName, String schemaName, String viewName, String selectQuery, boolean replaceIfExists) {
        return new CreateViewStatement(catalogName, schemaName, viewName, selectQuery, replaceIfExists);
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
