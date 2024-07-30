package liquibase.change.core;

import liquibase.ChecksumVersion;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.change.*;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.PropertyExpandingStream;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateProcedureStatement;
import liquibase.util.FileUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;

@DatabaseChange(name = "createProcedure", description = "Defines a stored procedure.", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateProcedureChange extends AbstractChange implements DbmsTargetedChange, ReplaceIfExists {
    @Setter
    private String comments;
    @Setter
    private String catalogName;
    @Setter
    private String schemaName;
    @Setter
    private String procedureName;
    @Setter
    private String procedureText;
    private String dbms;

    @Setter
    private String path;
    @Setter
    private Boolean relativeToChangelogFile;
    @Setter
    private String encoding;
    private Boolean replaceIfExists;

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return false;
    }

    @DatabaseChangeProperty(description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(exampleValue = "new_customer",
        description = "Name of the stored procedure to create. Required if replaceIfExists=true")
    public String getProcedureName() {
        return procedureName;
    }

    @DatabaseChangeProperty(exampleValue = "utf8", description = "Encoding used in the file you specify in 'path'")
    public String getEncoding() {
        return encoding;
    }

    @DatabaseChangeProperty(
        description = "File containing the procedure text. You must either use this attribute or write inline SQL within the createProcedure definition.",
        exampleValue = "com/example/my-logic.sql"
    )
    public String getPath() {
        return path;
    }

    @DatabaseChangeProperty(description = "Specifies whether the file path is relative to the changelog file " +
        "rather than looked up in the search path. Default: false.")
    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE, version = {ChecksumVersion.V8})
    @DatabaseChangeProperty(isChangeProperty = false)
    /**
     * @deprecated Use getProcedureText() instead
     */
    @Deprecated
    public String getProcedureBody() {
        return procedureText;
    }

    /**
     * @deprecated Use setProcedureText() instead
     */
    @Deprecated
    public void setProcedureBody(String procedureText) {
        this.procedureText = procedureText;
    }

    private final String procedureTextDescription = "The SQL creating the procedure. You need to define either this attribute or 'path'. " +
            "procedureText is not supported in the XML format; however, you can specify the procedure SQL inline within the createProcedure definition.";
    @DatabaseChangeProperty(
        description = procedureTextDescription,
            isChangeProperty = false, version = {ChecksumVersion.V8})
    @DatabaseChangeProperty(
        description = procedureTextDescription,
            serializationType = SerializationType.DIRECT_VALUE,
            alternatePropertyNames = {"procedureBody"})
    public String getProcedureText() {
        return procedureText;
    }

    @Override
    @DatabaseChangeProperty(
        exampleValue = "h2, oracle",
        since = "3.1",
        description = "Specifies which database type(s) a changeset is to be used for. " +
        "See valid database type names on Supported Databases docs page. Separate multiple databases with commas. " +
        "Specify that a changeset is not applicable to a particular database type by prefixing with !. " +
        "The keywords 'all' and 'none' are also available."
    )
    public String getDbms() {
        return dbms;
    }

    @Override
    public void setDbms(final String dbms) {
        this.dbms = dbms;
    }

    @DatabaseChangeProperty(description = "Inline comments generated by update-sql. Not applied to the database")
    public String getComments() {
        return comments;
    }

    @DatabaseChangeProperty(description = "If the stored procedure defined by createProcedure already exists, " +
        "alter it instead of creating it. Default: false")
    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    @Override
    public void setReplaceIfExists(Boolean replaceIfExists) {
        this.replaceIfExists = replaceIfExists;
    }

    @Override
    public ValidationErrors validate(Database database) {
        // Not falling back to default because of path/procedureText option group. Need to specify everything.
        ValidationErrors validate = new ValidationErrors();

        validate.checkDisallowedField("catalogName", this.getCatalogName(), database, MSSQLDatabase.class);
        if(getDbms() != null) {
            DatabaseList.validateDefinitions(getDbms(), validate);
        }

        if ((StringUtil.trimToNull(getProcedureText()) != null) && (StringUtil.trimToNull(getPath()) != null)) {
            validate.addError(
                "Cannot specify both 'path' and a nested procedure text in " +
                    Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(this).getName()
            );
        }

        if ((StringUtil.trimToNull(getProcedureText()) == null) && (StringUtil.trimToNull(getPath()) == null)) {
            validate.addError(
                "Must specify either 'path' or a nested procedure text in " +
                    Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(this).getName()
            );
        }

        if ((this.getReplaceIfExists() != null) && (DatabaseList.definitionMatches(getDbms(), database, true))) {
            if (databaseSupportsReplaceIfExists(database)) {
                if (this.getReplaceIfExists() && (this.getProcedureName() == null)) {
                    validate.addError("procedureName is required if replaceIfExists = true");
                }
            } else {
                validate.checkDisallowedField("replaceIfExists", this.getReplaceIfExists(), database);
            }
        }
        return validate;
    }

    public InputStream openSqlStream() throws IOException {
        if (path == null) {
            return null;
        }

        try {
            ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();

            String path = getPath();
            final Boolean isRelative = isRelativeToChangelogFile();
            if (isRelative != null && isRelative) {
                return resourceAccessor.get(getChangeSet().getChangeLog().getPhysicalFilePath()).resolveSibling(path).openInputStream();
            } else {
                return resourceAccessor.getExisting(path).openInputStream();
            }
        } catch (IOException e) {
            throw new IOException(
                "<" + Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(this).getName() + " path=" +
                path +
                "> -Unable to read file",
                e
            );
        }
    }

    /**
     * Calculates the checksum based on the contained SQL.
     *
     * @see Change#generateCheckSum()
     */
    @Override
    public CheckSum generateCheckSum() {
        ChecksumVersion version = Scope.getCurrentScope().getChecksumVersion();
        if (version.lowerOrEqualThan(ChecksumVersion.V8)) {
            return generateCheckSumV8();
        }
        return generateCheckSumLatest(this.procedureText);
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
            String procedureText = this.procedureText;
            if ((stream == null) && (procedureText == null)) {
                procedureText = "";
            }

            String localEncoding = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
            if (procedureText != null) {
                try {
                    stream = new ByteArrayInputStream(procedureText.getBytes(localEncoding));
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(localEncoding +
                            " is not supported by the JVM, this should not happen according to the JavaDoc of " +
                            "the Charset class"
                    );
                }
            }

            CheckSum checkSum = CheckSum.compute(new NormalizingStreamV8(";", false, false, stream), false);

            return CheckSum.compute(super.generateCheckSum().toString() + ":" + checkSum);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                    // Do nothing
                }
            }
        }
    }

    protected CheckSum generateCheckSumLatest(String sqlText) {
        InputStream stream = null;
        CheckSum checkSum;
        try {
            if (getPath() == null) {
                Charset encoding = GlobalConfiguration.FILE_ENCODING.getCurrentValue();
                if (sqlText != null) {
                    stream = new ByteArrayInputStream(sqlText.getBytes(encoding));
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
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                    // Do nothing
                }
            }
        }
    }

    /**
     * Listing SQL content fields (for example procedureText, triggerBody, etc.) we don't want to include as part of
     * the checksum computes, because have a separate part that computes that checksum for that part doing the
     * "normalizing" logic, so it is not impacted by the reformatting of the SQL. We are also excluding fields from the
     * checksum generation which does not have a direct impact on the DB, such as dbms, path, comments, etc.
     *
     * Besides it has an impact on the DB, we have decided to do not add replaceIfExists as part of this list of fields
     * as we are already avoiding the recalculation of the checksum by listing the main content fields of the different
     * change types.
     */
    @Override
    public String[] getExcludedFieldFilters(ChecksumVersion version) {
        if (version.lowerOrEqualThan(ChecksumVersion.V8)) {
            return new String[0];
        }
        return new String[]{
                "path",
                "dbms",
                "relativeToChangelogFile",
                "procedureText",
                "encoding",
                "comments",
                "triggerBody",
                "functionBody",
                "packageText",
                "packageBodyText"
        };
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        String endDelimiter = ";";
        if (database instanceof OracleDatabase) {
            endDelimiter = "\n/";
        } else if (database instanceof AbstractDb2Database) {
            endDelimiter = "";
        }

        String procedureText;
        String path = getPath();
        if (path == null) {
            procedureText = StringUtil.trimToNull(getProcedureText());
        } else {
            if (getChangeSet() == null) {
                //only try to read a file when inside a changest. Not when analyzing supported
                procedureText = "NO CHANGESET";
            } else {
                try {
                    InputStream stream = openSqlStream();
                    if (stream == null) {
                        throw new IOException(FileUtil.getFileNotFoundMessage(path));
                    }
                    procedureText = StreamUtil.readStreamAsString(stream, encoding);
                    if (getChangeSet() != null) {
                        ChangeLogParameters parameters = getChangeSet().getChangeLogParameters();
                        if (parameters != null) {
                            procedureText = parameters.expandExpressions(procedureText, getChangeSet().getChangeLog());
                        }
                    }
                } catch (IOException e) {
                    throw new UnexpectedLiquibaseException(e);
                }
            }
        }
        return generateStatements(procedureText, endDelimiter, database);
    }

    protected SqlStatement[] generateStatements(String logicText, String endDelimiter, Database database) {
        CreateProcedureStatement statement =
            new CreateProcedureStatement(
                getCatalogName(),
                getSchemaName(),
                getProcedureName(),
                logicText,
                endDelimiter
            );
        statement.setReplaceIfExists(getReplaceIfExists());
        return new SqlStatement[]{
                statement,
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Cannot check createProcedure status");
    }

    @Override
    public String getConfirmationMessage() {
        return "Stored procedure created";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @SuppressWarnings("java:S2095")
    @Override
    protected Map<String, Object> createExampleValueMetaData(
        String parameterName, DatabaseChangeProperty changePropertyAnnotation) {

        if ("procedureText".equals(parameterName) || "procedureBody".equals(parameterName)) {
            Map<String, Object> returnMap = super.createExampleValueMetaData(parameterName, changePropertyAnnotation);
            returnMap.put(
                new HsqlDatabase().getShortName(),
                "CREATE PROCEDURE new_customer(firstname VARCHAR(50), lastname VARCHAR(50))\n" +
                    "   MODIFIES SQL DATA\n" +
                    "   INSERT INTO CUSTOMERS (first_name, last_name) VALUES (firstname, lastname)"
            );

            return returnMap;
        } else {
            return super.createExampleValueMetaData(parameterName, changePropertyAnnotation);
        }
    }

    private static boolean databaseSupportsReplaceIfExists(Database database) {
        if (database instanceof MSSQLDatabase) {
            return true;
        }
        if (database instanceof MySQLDatabase) {
            return true;
        }
        if (database instanceof DB2Database) {
            return true;
        }

        if (database instanceof Db2zDatabase) {
           try {
                int major = database.getDatabaseMajorVersion();
                if (major > 12) {
                    return true;
                }
                if (major < 12) {
                    return false;
                }
                return database.getDatabaseMinorVersion() >= 1;
            } catch (DatabaseException e) {
                return false;
            }
        }
        return false;
    }
}
