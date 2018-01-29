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
import liquibase.logging.LogService;
import liquibase.logging.LogType;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private List<String> args = new ArrayList<String>();
    private String timeout;
    private static final Pattern TIMEOUT_PATTERN = Pattern.compile("^\\s*(\\d+)\\s*([sSmMhH]?)\\s*$");
    private static final Long SECS_IN_MILLIS = 1000L;
    private static final Long MIN_IN_MILLIS = SECS_IN_MILLIS * 60;
    private static final Long HOUR_IN_MILLIS = MIN_IN_MILLIS * 60;

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

    @DatabaseChangeProperty(description = "Timeout value for executable to run", exampleValue = "10s")
    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    @DatabaseChangeProperty(description = "List of operating systems on which to execute the command (taken from the os.name Java system property)", exampleValue = "Windows 7")
    public List<String> getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = StringUtils.splitAndTrim(os, ",");
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (!StringUtils.isEmpty(timeout)) {
            // check for the timeout values, accept only positive value with one letter unit (s/m/h)
            Matcher matcher = TIMEOUT_PATTERN.matcher(timeout);
            if (!matcher.matches()) {
                validationErrors.addError("Invalid value specified for timeout: " + timeout);
            }
        }

        return validationErrors;
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
                LogService.getLog(getClass()).info(LogType.LOG, "Not executing on os " + currentOS + " when " + os + " was " +
                        "specified");
            }
        }

        // check if running under not-executed mode (logging output)
        boolean nonExecutedMode = false;
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        if (executor instanceof LoggingExecutor) {
            nonExecutedMode = true;
        }

        this.finalCommandArray = createFinalCommandArray(database);

        if (shouldRun && !nonExecutedMode) {


            return new SqlStatement[]{new RuntimeStatement() {

                @Override
                public Sql[] generate(Database database) {

                    try {
                        executeCommand(database);
                    } catch (Exception e) {
                        throw new UnexpectedLiquibaseException("Error executing command: " + e.getLocalizedMessage(), e);
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

    protected List<String> createFinalCommandArray(Database database) {
        List<String> commandArray = new ArrayList<>();
        commandArray.add(getExecutable());
        commandArray.addAll(getArgs());
        return commandArray;
    }

    protected void executeCommand(Database database) throws Exception {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        ByteArrayOutputStream inputStream = new ByteArrayOutputStream();

        ProcessBuilder pb = createProcessBuilder(database);
        Process p = pb.start();
        int returnCode = 0;
        try {
            //output both stdout and stderr data from proc to stdout of this process
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), errorStream);
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), inputStream);

            errorGobbler.start();
            outputGobbler.start();

            // check if timeout is specified
            // can't use Process's new api with timeout, so just workaround it for now
            long timeoutInMillis = getTimeoutInMillis();
            if (timeoutInMillis > 0) {
                returnCode = waitForOrKill(p, timeoutInMillis);
            } else {
                // do default behavior for any value equal to or less than 0
                returnCode = p.waitFor();
            }

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

        if (errorStreamOut != null && !errorStreamOut.isEmpty()) {
            LogService.getLog(getClass()).severe(LogType.LOG, errorStreamOut);
        }
        LogService.getLog(getClass()).info(LogType.LOG, infoStreamOut);

        processResult(returnCode, errorStreamOut, infoStreamOut, database);
    }

    /**
     * Waits for the process to complete and kills it if the process is not finished after the specified <code>timeoutInMillis</code>.
     * <p>
     * Creates a scheduled task to destroy the process in given timeout milliseconds.
     * This killer task will be cancelled if the process returns before the timeout value.
     *
     * @param process
     * @param timeoutInMillis waits for specified timeoutInMillis before destroying the process.
     *                        It will wait indefinitely if timeoutInMillis is 0.
     */
    private int waitForOrKill(final Process process, final long timeoutInMillis) throws ExecutionException, TimeoutException {
        int ret = -1;
        final AtomicBoolean timedOut = new AtomicBoolean(false);
        Timer timer = new Timer();
        if (timeoutInMillis > 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // timed out
                    timedOut.set(true);
                    process.destroy();
                }
            }, timeoutInMillis);
        }

        boolean stop = false;
        while (!stop) {
            try {
                ret = process.waitFor();
                stop = true;
                // if process already returned, then cancel the killer task if it is still running
                timer.cancel();
                // check if we timed out or not
                if (timedOut.get()) {
                    String timeoutStr = timeout != null ? timeout : timeoutInMillis + " ms";
                    throw new TimeoutException("Process timed out (" + timeoutStr + ")");
                }
            } catch (InterruptedException ignore) {
                // check again
            }
        }

        return ret;
    }

    /**
     * @return the timeout value in millisecond
     */
    protected long getTimeoutInMillis() {
        if (timeout != null) {
            //Matcher matcher = TIMEOUT_PATTERN.matcher("10s");
            Matcher matcher = TIMEOUT_PATTERN.matcher(timeout);
            if (matcher.find()) {
                String val = matcher.group(1);
                try {
                    long valLong = Long.parseLong(val);
                    String unit = matcher.group(2);
                    if (StringUtils.isEmpty(unit)) {
                        return valLong * SECS_IN_MILLIS;
                    }
                    char u = unit.toLowerCase().charAt(0);
                    // only s/m/h possible here
                    switch (u) {
                        case 'h':
                            valLong = valLong * HOUR_IN_MILLIS;
                            break;
                        case 'm':
                            valLong = valLong * MIN_IN_MILLIS;
                        default:
                            valLong = valLong * SECS_IN_MILLIS;
                    }

                    return valLong;
                } catch (NumberFormatException ignore) {
                }

            }
        }
        return 0;
    }

    /**
     * Called by {@link #executeCommand(Database)} after running the command. Default implementation throws an error if returnCode != 0
     */
    protected void processResult(int returnCode, String errorStreamOut, String infoStreamOut, Database database) {
        if (returnCode != 0) {
            throw new RuntimeException(getCommandString() + " returned an code of " + returnCode);
        }
    }

    protected ProcessBuilder createProcessBuilder(Database database) {
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

    @Override
    public String toString() {
        return "external process '" + getExecutable() + "' " + getArgs();
    }
}
