package liquibase.change.core;

import liquibase.action.Action;
import liquibase.action.ExecuteAction;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import  liquibase.ExecutionEnvironment;
import liquibase.executor.ExecuteResult;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.logging.LogFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.Statement;
import liquibase.statement.core.CommentStatement;
import liquibase.statement.core.RawActionStatement;
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
    public boolean generateStatementsVolatile(ExecutionEnvironment env) {
        return true;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(ExecutionEnvironment env) {
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
    public ValidationErrors validate(ExecutionEnvironment env) {
        return new ValidationErrors();
    }


    @Override
    public Warnings warn(ExecutionEnvironment env) {
        return new Warnings();
    }

    @Override
    public Statement[] generateStatements(final ExecutionEnvironment env) {
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
        Executor executor = ExecutorService.getInstance().getExecutor(env.getTargetDatabase());
        if (executor instanceof LoggingExecutor) {
            nonExecutedMode = true;
        }
        
        if (shouldRun && !nonExecutedMode) {

            Action action = new ExecuteAction() {
                @Override
                public ExecuteResult execute(ExecutionEnvironment env) throws DatabaseException {
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

                        return new ExecuteResult();
                    } catch (IOException e) {
                        throw new UnexpectedLiquibaseException("Error executing command: " + e);
                    }
                }

                @Override
                public String describe() {
                    return executable+" "+StringUtils.join(getArgs(), " ");
                }
            };

            return new Statement[] {new RawActionStatement(action)};
        }
        
        if (nonExecutedMode) {
        	return new Statement[] {
        			new CommentStatement(getCommandString())
        	};
        }
        
        return new Statement[0];
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
        if (os.size() > 0) {
            this.os = os;
        }
    }
}
