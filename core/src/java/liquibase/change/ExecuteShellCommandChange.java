package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.log.LogFactory;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Executes a given shell executable.
 */
public class ExecuteShellCommandChange extends AbstractChange {

    private String executable;
    private List<String> os;
    private List<String> args = new ArrayList<String>();

    public ExecuteShellCommandChange() {
        super("executeCommand", "Execute Shell Command");
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

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(executable) == null) {
            throw new InvalidChangeDefinitionException("executable is required", this);
        }

    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        boolean shouldRun = true;
        if (os != null && os.size() > 0) {
            String currentOS = System.getProperty("os.name");
            if (!os.contains(currentOS)) {
                shouldRun = false;
                LogFactory.getLogger().info("Not executing on os "+currentOS+" when "+os+" was specified");
            }
        }

        if (shouldRun) {
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
                throw new UnsupportedChangeException("Error executing command: " + e);
            }

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
