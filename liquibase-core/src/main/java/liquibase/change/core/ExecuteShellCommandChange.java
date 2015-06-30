package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.logging.LogFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CommentStatement;
import liquibase.statement.core.RuntimeStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Executes a given shell executable.
 */
@DatabaseChange(name="executeCommand",
        description = "Executes a system command. Because this refactoring doesn't generate SQL like most, using LiquiBase commands such as migrateSQL may not work as expected. Therefore, if at all possible use refactorings that generate SQL.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class ExecuteShellCommandChange extends AbstractChange {

    private String executable;
    private List<String> os;
    private List<String> args = new ArrayList<String>();

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return true;
    }

    @DatabaseChangeProperty(description = "Name of the executable to run", exampleValue = "mysqldump", requiredForDatabase = "all")
    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void addArg(String arg) {
        this.args.add(arg);
    }

    public List<String> getArgs() {
        return Collections.unmodifiableList(args);
    }

    public void setOs(String os) {
        this.os = StringUtils.splitAndTrim(os, ",");
    }

    @DatabaseChangeProperty(description = "List of operating systems on which to execute the command (taken from the os.name Java system property)", exampleValue = "Windows 7")
    public List<String> getOs() {
        return os;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }


    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public SqlStatement[] generateStatements(final Database database) {
        boolean shouldRun = true;
        if (os != null && os.size() > 0) {
            String currentOS = System.getProperty("os.name");
            if (!os.contains(currentOS)) {
                shouldRun = false;
                LogFactory.getLogger().info("Not executing on os "+currentOS+" when "+os+" was specified");
            }
        }

    	// check if running under not-executed mode (logging output)
        boolean nonExecutedMode = false;
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        if (executor instanceof LoggingExecutor) {
            nonExecutedMode = true;
        }
        
        if (shouldRun && !nonExecutedMode) {


            return new SqlStatement[]{new RuntimeStatement() {

                @Override
                public Sql[] generate(Database database) {
                    List<String> commandArray = new ArrayList<String>();
                    commandArray.add(executable);
                    commandArray.addAll(getArgs());

                    try {
                        ProcessBuilder pb = new ProcessBuilder(commandArray);
                        pb.redirectErrorStream(true);
                        Process p = pb.start();
                        int returnCode = 0;
                        try {
                            returnCode = p.waitFor();
                        } catch (InterruptedException e) {
                            ;
                        }

                        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
                        ByteArrayOutputStream inputStream = new ByteArrayOutputStream();
                        StreamUtil.copy(p.getErrorStream(), errorStream);
                        StreamUtil.copy(p.getInputStream(), inputStream);

                        LogFactory.getLogger().severe(errorStream.toString());
                        LogFactory.getLogger().info(inputStream.toString());

                        if (returnCode != 0) {
                            throw new RuntimeException(getCommandString() + " returned an code of " + returnCode);
                        }
                    } catch (IOException e) {
                        throw new UnexpectedLiquibaseException("Error executing command: " + e);
                    }

                    return null;
                }
            }};
        }
        
        if (nonExecutedMode) {
        	return new SqlStatement[] {
        			new CommentStatement(getCommandString())
        	};
        }
        
        return new SqlStatement[0];
    }

    @Override
    public String getConfirmationMessage() {
        return "Shell command '" + getCommandString() + "' executed";
    }

    private String getCommandString() {
        return executable + " " + StringUtils.join(args, " ");
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ParsedNode argsNode = parsedNode.getChild(null, "args");
        if (argsNode == null) {
            argsNode = parsedNode;
        }

        for (ParsedNode arg : argsNode.getChildren(null, "arg")) {
            addArg(arg.getChildValue(null, "value", String.class));
        }
        List<String> os = StringUtils.splitAndTrim(StringUtils.trimToEmpty(parsedNode.getChildValue(null, "os", String.class)), ",");
        if (os.size() == 1 && os.get(0).equals("")) {
            this.os = null;
        } else  if (os.size() > 0) {
            this.os = os;
        }

    }
}
