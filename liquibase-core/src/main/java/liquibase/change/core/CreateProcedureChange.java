package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.DB2Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateProcedureStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.io.UnsupportedEncodingException;

@DatabaseChange(name = "createProcedure",
        description = "Defines the definition for a stored procedure. This command is better to use for creating procedures than the raw sql command because it will not attempt to strip comments or break up lines.\n\nOften times it is best to use the CREATE OR REPLACE syntax along with setting runOnChange='true' on the enclosing changeSet tag. That way if you need to make a change to your procedure you can simply change your existing code rather than creating a new REPLACE PROCEDURE call. The advantage to this approach is that it keeps your change log smaller and allows you to more easily see what has changed in your procedure code through your source control system's diff command.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateProcedureChange extends AbstractChange implements DbmsTargetedChange {
    private String comments;
    private String catalogName;
    private String schemaName;
    private String procedureName;
    private String procedureText;
	private String dbms;

    private String path;
    private Boolean relativeToChangelogFile;
    private String encoding = null;
    private Boolean replaceIfExists;

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return false;
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

    @DatabaseChangeProperty(exampleValue = "new_customer")
    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    @DatabaseChangeProperty(exampleValue = "utf8")
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @DatabaseChangeProperty(description = "File containing the procedure text. Either this attribute or a nested procedure text is required.", exampleValue = "com/example/my-logic.sql")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }


    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE,
    exampleValue = "CREATE OR REPLACE PROCEDURE testHello\n" +
            "    IS\n" +
            "    BEGIN\n" +
            "      DBMS_OUTPUT.PUT_LINE('Hello From The Database!');\n" +
            "    END;")
    /**
     * @deprecated Use getProcedureText() instead
     */
    public String getProcedureBody() {
        return procedureText;
    }

    /**
     * @deprecated Use setProcedureText() instead
     */
    public void setProcedureBody(String procedureText) {
        this.procedureText = procedureText;
    }

    @DatabaseChangeProperty(isChangeProperty = false)
    public String getProcedureText() {
        return procedureText;
    }

    public void setProcedureText(String procedureText) {
        this.procedureText = procedureText;
    }

    @DatabaseChangeProperty(since = "3.1", exampleValue = "h2, oracle")
	public String getDbms() {
		return dbms;
	}

	public void setDbms(final String dbms) {
		this.dbms = dbms;
	}

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @DatabaseChangeProperty
    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    public void setReplaceIfExists(Boolean replaceIfExists) {
        this.replaceIfExists = replaceIfExists;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = new ValidationErrors(); //not falling back to default because of path/procedureText option group. Need to specify everything
        if (StringUtils.trimToNull(getProcedureText()) != null && StringUtils.trimToNull(getPath()) != null) {
            validate.addError("Cannot specify both 'path' and a nested procedure text in "+ChangeFactory.getInstance().getChangeMetaData(this).getName());
        }

        if (StringUtils.trimToNull(getProcedureText()) == null && StringUtils.trimToNull(getPath()) == null) {
            validate.addError("Cannot specify either 'path' or a nested procedure text in "+ChangeFactory.getInstance().getChangeMetaData(this).getName());
        }

        if (this.getReplaceIfExists() != null) {
            if (database instanceof MSSQLDatabase) {
                if (this.getReplaceIfExists() && this.getProcedureName() == null) {
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
            return StreamUtil.openStream(getPath(), isRelativeToChangelogFile(), getChangeSet(), getResourceAccessor());
        } catch (IOException e) {
            throw new IOException("<"+ChangeFactory.getInstance().getChangeMetaData(this).getName()+" path=" + path + "> -Unable to read file", e);
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

        String procedureText = this.procedureText;
        if (stream == null && procedureText == null) {
            procedureText = "";
        }

        if (procedureText != null) {
            try {
                stream = new ByteArrayInputStream(procedureText.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is not supported by the JVM, this should not happen according to the JavaDoc of the Charset class");
            }
        }

        CheckSum checkSum = CheckSum.compute(new AbstractSQLChange.NormalizingStream(";", false, false, stream), false);

        return CheckSum.compute(super.generateCheckSum().toString()+":"+checkSum.toString());

    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        String endDelimiter = ";";
        if (database instanceof OracleDatabase) {
            endDelimiter = "\n/";
        } else if (database instanceof DB2Database) {
            endDelimiter = "";
        }

        String procedureText;
        String path = getPath();
        if (path == null) {
            procedureText = StringUtils.trimToNull(getProcedureText());
        } else {
            try {
                InputStream stream = openSqlStream();
                if (stream == null) {
                    throw new IOException("File does not exist: "+path);
                }
                procedureText = StreamUtil.getStreamContents(stream, encoding);
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return generateStatements(procedureText, endDelimiter, database);
    }

    protected SqlStatement[] generateStatements(String logicText, String endDelimiter, Database database) {
        CreateProcedureStatement statement = new CreateProcedureStatement(getCatalogName(), getSchemaName(), getProcedureName(), logicText, endDelimiter);
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

    @Override
    protected Map<String, Object> createExampleValueMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {

        if (parameterName.equals("procedureText") || parameterName.equals("procedureBody")) {
            Map<String, Object> returnMap = super.createExampleValueMetaData(parameterName, changePropertyAnnotation);
            returnMap.put(new HsqlDatabase().getShortName(), "CREATE PROCEDURE new_customer(firstname VARCHAR(50), lastname VARCHAR(50))\n" +
                    "   MODIFIES SQL DATA\n" +
                    "   INSERT INTO CUSTOMERS (first_name, last_name) VALUES (firstname, lastname)");

            return returnMap;
        } else {
            return super.createExampleValueMetaData(parameterName, changePropertyAnnotation);
        }
    }
}
