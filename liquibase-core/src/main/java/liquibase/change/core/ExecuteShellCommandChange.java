package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.logging.LogFactory;
import liquibase.logging.LogTarget;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CommentStatement;
import liquibase.statement.core.RuntimeStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Executes a given shell executable.
 */
@DatabaseChange(name = "executeCommand",
        description = "Executes a system command. Because this refactoring doesn't generate SQL like most, using " +
            "Liquibase commands such as migrateSQL may not work as expected. Therefore, if at all possible use " +
                "refactorings that generate SQL.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class ExecuteShellCommandChange extends AbstractChange {

    protected List<String> finalCommandArray;
    private String executable;
    private List<String> os;
    private List<String> args = new ArrayList<>();

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return true;
    }

    @DatabaseChangeProperty(description = "Name of the executable to run",
            exampleValue = "mysqldump", requiredForDatabase = "all")
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

    @DatabaseChangeProperty(description = "List of operating systems on which to execute the command " +
            "(taken from the os.name Java system property)", exampleValue = "Windows 7")
    public List<String> getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = StringUtils.splitAndTrim(os, ",");
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
        if ((os != null) && (!os.isEmpty())) {
            String currentOS = System.getProperty("os.name");
            if (!os.contains(currentOS)) {
                shouldRun = false;
                LogFactory.getLog(getClass()).info(LogTarget.LOG, "Not executing on os " + currentOS + " when " + os + " was " +
                        "specified");
            }
        }

        // check if running under not-executed mode (logging output)
        boolean nonExecutedMode = false;
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        if (executor instanceof LoggingExecutor) {
            nonExecutedMode = true;
        }

        this.finalCommandArray = createFinalCommandArray();

        if (shouldRun && !nonExecutedMode) {


            return new SqlStatement[]{new RuntimeStatement() {

                @Override
                public Sql[] generate(Database database) {

                    try {
                        executeCommand();
                    } catch (Exception e) {
                        throw new UnexpectedLiquibaseException("Error executing command: " + e.getLocalizedMessage(),
                                e);
                    }

                    return null;
                }
            }};
        }

        if (nonExecutedMode) {
            try {
                return new SqlStatement[]{
                        new CommentStatement(getCommandString())
                };
            } finally {
                nonExecutedCleanup();
            }
        }

        return new SqlStatement[0];
    }

    protected void nonExecutedCleanup() {

    }

    protected List<String> createFinalCommandArray() {
        List<String> commandArray = new ArrayList<>();
        commandArray.add(getExecutable());
        commandArray.addAll(getArgs());
        return commandArray;
    }

    protected void executeCommand() throws Exception {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        ByteArrayOutputStream inputStream = new ByteArrayOutputStream();

        ProcessBuilder pb = createProcessBuilder();
        Process p = pb.start();
        int returnCode = 0;
        try {
            //output both stdout and stderr data from proc to stdout of this process
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), errorStream);
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), inputStream);

            errorGobbler.start();
            outputGobbler.start();

            returnCode = p.waitFor();

            errorGobbler.finish();
            outputGobbler.finish();

        } catch (InterruptedException e) {
            // Restore thread interrupt status
            Thread.currentThread().interrupt();
        }

        String errorStreamOut = errorStream.toString(LiquibaseConfiguration.getInstance().getConfiguration
                (GlobalConfiguration.class).getOutputEncoding());
        String infoStreamOut = inputStream.toString(LiquibaseConfiguration.getInstance().getConfiguration
                (GlobalConfiguration.class).getOutputEncoding());

        LogFactory.getLog(getClass()).error(LogTarget.LOG, errorStreamOut);
        LogFactory.getLog(getClass()).info(LogTarget.LOG, infoStreamOut);

        throwExceptionIfError(returnCode);
    }

    protected void throwExceptionIfError(int returnCode) {
        if (returnCode != 0) {
            throw new RuntimeException(getCommandString() + " returned an code of " + returnCode);
        }
    }

    protected ProcessBuilder createProcessBuilder() {
        ProcessBuilder pb = new ProcessBuilder(finalCommandArray);
        pb.redirectErrorStream(true);
        return pb;
    }

    @Override
    public String getConfirmationMessage() {
        return "Shell command '" + getCommandString() + "' executed";
    }

    protected String getCommandString() {
        return getExecutable() + " " + StringUtils.join(args, " ");
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws
            ParsedNodeException {
        ParsedNode argsNode = parsedNode.getChild(null, "args");
        if (argsNode == null) {
            argsNode = parsedNode;
        }

        for (ParsedNode arg : argsNode.getChildren(null, "arg")) {
            addArg(arg.getChildValue(null, "value", String.class));
        }
        String passedValue = StringUtils.trimToNull(parsedNode.getChildValue(null, "os", String.class));
        if (passedValue == null) {
            this.os = new ArrayList<>();
        } else {
            List<String> os = StringUtils.splitAndTrim(StringUtils.trimToEmpty(parsedNode.getChildValue(null, "os",
                    String.class)), ",");
            if ((os.size() == 1) && ("".equals(os.get(0)))) {
                this.os = null;
            } else if (!os.isEmpty()) {
                this.os = os;
            }
        }
    }

    @Override
    public String toString() {
        return "external process '" + getExecutable() + "' " + getArgs();
    }

    private class StreamGobbler extends Thread {
        private static final int THREAD_SLEEP_MILLIS = 100;
        private final OutputStream outputStream;
        private InputStream processStream;

        private StreamGobbler(InputStream processStream, ByteArrayOutputStream outputStream) {
            this.processStream = processStream;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(processStream);
                while (processStream != null) {
                    if (bufferedInputStream.available() > 0) {
                        StreamUtil.copy(bufferedInputStream, outputStream);
                    }
                    try {
                        Thread.sleep(THREAD_SLEEP_MILLIS);
                    } catch (InterruptedException e) {
                        // Restore thread interrupt status
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public void finish() {
            InputStream procStream = this.processStream;
            this.processStream = null;

            try {
                StreamUtil.copy(procStream, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
