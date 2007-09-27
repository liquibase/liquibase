package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.StringUtils;
import liquibase.util.StreamUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;

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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (os != null && os.size() > 0) {
            if (os.contains(System.getProperty("os.name"))) {
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
                    StreamUtil.copy(p.getErrorStream(), System.err);
                    StreamUtil.copy(p.getInputStream(), System.err);

                    if (returnCode != 0) {
                        throw new RuntimeException(getCommandString()+" returned an code of "+returnCode);
                    }
                } catch (IOException e) {
                    throw new UnsupportedChangeException("Error executing command: " + e);
                }
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

    public Element createNode(Document currentChangeLogDOM) {
        Element root = currentChangeLogDOM.createElement(getTagName());
        root.setAttribute("executable", getExecutable());

        for (String arg : args) {
            Element argElement = currentChangeLogDOM.createElement("arg");
            argElement.setAttribute("value", arg);
            root.appendChild(argElement);
        }

        return root;

    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }
}
