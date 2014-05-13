package liquibase.parser.core.yaml;

import liquibase.ContextExpression;
import liquibase.change.*;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.CustomChangeException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.precondition.CustomPreconditionWrapper;
import liquibase.precondition.Precondition;
import liquibase.precondition.PreconditionFactory;
import liquibase.precondition.PreconditionLogic;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sql.visitor.SqlVisitorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.util.ObjectUtil;
import liquibase.util.StreamUtil;
import liquibase.util.file.FilenameUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

public class YamlChangeLogParser implements ChangeLogParser {

    protected Logger log = LogFactory.getLogger();

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        return changeLogFile.toLowerCase().endsWith("."+ getSupportedFileExtension());
    }

    protected String getSupportedFileExtension() {
        return "yaml";
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        Yaml yaml = new Yaml();

        try {
            InputStream changeLogStream = StreamUtil.singleInputStream(physicalChangeLogLocation, resourceAccessor);
            if (changeLogStream == null) {
                throw new ChangeLogParseException(physicalChangeLogLocation+" does not exist");
            }

            Map parsedYaml;
            try {
                parsedYaml = yaml.loadAs(changeLogStream, Map.class);
            } catch (Exception e) {
                throw new ChangeLogParseException("Syntax error in "+getSupportedFileExtension()+": " + e.getMessage(), e);
            }

            DatabaseChangeLog changeLog = new DatabaseChangeLog(physicalChangeLogLocation);
            changeLog.setChangeLogParameters(changeLogParameters);
            List rootList = (List) parsedYaml.get("databaseChangeLog");
            if (rootList == null) {
                throw new ChangeLogParseException("Could not find databaseChangeLog node");
            }

            ParsedNode databaseChangeLogNode = new ParsedNode(null, "databaseChangeLog");
            databaseChangeLogNode.setValue(rootList);

            changeLog.load(databaseChangeLogNode, resourceAccessor);

            return changeLog;
        } catch (Throwable e) {
            if (e instanceof ChangeLogParseException) {
                throw (ChangeLogParseException) e;
            }
            throw new ChangeLogParseException(e);
        }
    }
}
