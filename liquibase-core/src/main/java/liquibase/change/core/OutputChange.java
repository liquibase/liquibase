package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RuntimeStatement;
import liquibase.util.StringUtils;

@DatabaseChange(name="output", description = "Logs a message and continues execution.", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "3.3")
public class OutputChange extends AbstractChange {

    private String message;
    private String target = "";

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = super.validate(database);
        validate.checkRequiredField("message", getMessage());
        return validate;
    }

    @DatabaseChangeProperty(description = "Message to output", exampleValue = "Make sure you feed the cat", serializationType = LiquibaseSerializable.SerializationType.DIRECT_VALUE)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = StringUtils.trimToNull(message);
    }

    @DatabaseChangeProperty(description = "Target for message. Possible values: STDOUT, STDERR, FATAL, WARN, INFO, DEBUG. Default value: STDERR", exampleValue = "STDERR")
    public String getTarget() {
        if (target == null) {
            return "STDERR";
        }
        return target;
    }

    public void setTarget(String target) {
        this.target = StringUtils.trimToNull(target);
    }


    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] { new RuntimeStatement() {
            @Override
            public Sql[] generate(Database database) {
                String target = getTarget();
                if (target.equalsIgnoreCase("STDOUT")) {
                    System.out.println(getMessage());
                } else if (target.equalsIgnoreCase("STDERR")) {
                    System.err.println(getMessage());
                } else if (target.equalsIgnoreCase("DEBUG")) {
                    LogFactory.getInstance().getLog().debug(getMessage());
                } else if (target.equalsIgnoreCase("INFO")) {
                    LogFactory.getInstance().getLog().info(getMessage());
                } else if (target.equalsIgnoreCase("WARN") || target.equalsIgnoreCase("WARNING")) {
                    LogFactory.getInstance().getLog().warning(getMessage());
                } else if (target.equalsIgnoreCase("SEVERE") || target.equalsIgnoreCase("FATAL") || target.equalsIgnoreCase("ERROR")) {
                    LogFactory.getInstance().getLog().severe(getMessage());
                } else {
                    throw new UnexpectedLiquibaseException("Unknown target: "+target);
                }
                return null;
            }
        }};
    }

    @Override
    public String getConfirmationMessage() {
        return "Output: "+getMessage();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        Object value = super.getSerializableFieldValue(field);
        if (field.equals("target") && value.equals("")) {
            return null;
        }
        return value;
    }
}
