package liquibase.change;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.executor.WriteExecutor;
import liquibase.statement.CommentStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import liquibase.util.log.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes a given shell executable.
 */
public class ExecuteShellCommandChange extends AbstractChange {

    private String executable;
    private List<String> os;
    private List<String> args = new ArrayList<String>();

    public ExecuteShellCommandChange() {
        super("executeCommand", "Execute Shell Command", ChangeMetaData.PRIORITY_DEFAULT);
    }


    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void addArg(String arg) {
        this.args.add(arg);
    }


    public void setOs(String os) {
        this.os = StringUtils.splitAndTrim(os, ",");
    }

    public SqlStatement[] generateStatements(Database database) {
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
        WriteExecutor writeExecutor = ExecutorService.getInstance().getWriteExecutor(database);
        if (writeExecutor instanceof LoggingExecutor) {
        	nonExecutedMode = true;
        }
        
        if (shouldRun && !nonExecutedMode) {
        	
        	
            List<String> commandArray = new ArrayList<String>();
            commandArray.add(executable);
            commandArray.addAll(args);

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

                LogFactory.getLogger().info(errorStream.toString());
                LogFactory.getLogger().info(inputStream.toString());

                if (returnCode != 0) {
                    throw new RuntimeException(getCommandString()+" returned an code of "+returnCode);
                }
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException("Error executing command: " + e);
            }

        }
        
        if (nonExecutedMode) {
        	return new SqlStatement[] {
        			new CommentStatement(getCommandString())
        	};
        }
        
        return new SqlStatement[0];
    }

    public String getConfirmationMessage() {
        return "Shell command '" + getCommandString() + "' executed";
    }

    private String getCommandString() {
        return executable + " " + StringUtils.join(args, " ");
    }
}
