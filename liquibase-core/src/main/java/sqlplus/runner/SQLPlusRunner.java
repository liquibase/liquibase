package sqlplus.runner;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.SqlPlusException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import sqlplus.context.SqlPlusContext;
import sqlplus.stolen.StreamPumper;

import java.io.*;
import java.util.Map;


/**
 * @author gette
 *         For supporting independent execution of Liquiplus without Ant
 *         moving StreamPumper from ant to sqlplus.stolen
 * @see sqlplus.stolen.StreamPumper
 */
public class SQLPlusRunner {
    private static Logger log = LogFactory.getLogger();

    private static final String OSName = System.getProperty("os.name").toLowerCase();
    private static final String SQLPLUS = "sqlplus ";

    public static void run(String pathToFile) throws SqlPlusException {
        log.debug("SQLPlusRunner has been initialized.");
        try {
            //adding condition which protects from crashing on Linux OS when using manual mode
            if (SqlPlusContext.getInstance().isManual() && OSName.contains("win")) {
                Process notepad = new ProcessBuilder("notepad", pathToFile).start();
                notepad.waitFor();
                notepad.destroy();
            }
            executeSQLPlus(pathToFile);

        } catch (IOException e) {
            throw new SqlPlusException(e);
        } catch (InterruptedException e) {
            throw new SqlPlusException(e);
        }
    }

    public static void executeSQLPlus(String pathToFile) throws IOException, SqlPlusException, InterruptedException {
        try {
            int err = 0;
            log.debug("Launching SQLPLUS.");

            ProcessBuilder pb = new ProcessBuilder(SQLPLUS, SqlPlusContext.getInstance().getConnection().getConnectionAsString(), "@" + pathToFile);
            Map<String, String> env = pb.environment();
            env.put("NLS_LANG", "AMERICAN_AMERICA.AL32UTF8");

            Process sqlplus = pb.start();

            OutputStream os = new ByteArrayOutputStream(4096);
            StreamPumper inputPumper = new StreamPumper(sqlplus.getInputStream(), os);
            inputPumper.run();
            // Wait for everything to finish
            sqlplus.waitFor();
            log.sqlplus(os.toString());
            sqlplus.destroy();


            // check its exit value
            err = sqlplus.exitValue();
            if (err == 0) {
                log.sqlplus("SQLPlusRunner. Successful Execution");
            } else {
                throw new SqlPlusException("SQLPLUSERROR. Something went wrong: ORA-00" + err);
            }
        } catch (IOException e) {
            throw new SqlPlusException(e);
        } catch (SqlPlusException e) {
            throw new SqlPlusException(e);
        } catch (InterruptedException e) {
            throw new SqlPlusException(e);
        }
    }

    public static String makeChangeSetFile(Change change, Database database) {
        SqlStatement[] statements = change.generateStatements(database);
        String sqlplus = SqlPlusContext.getInstance().getConnection().getInitSQL();
        for (SqlStatement statement : statements) {
            sqlplus = sqlplus.concat(statement.toString() + System.getProperty("line.separator") + "/" + System.getProperty("line.separator"));
        }
        sqlplus = sqlplus.concat(SqlPlusContext.getInstance().getConnection().getExitSQL());
        String pathToFile = System.getProperty("user.dir") + "/changeSet.sql";
        writeToFile(pathToFile, sqlplus);
        return pathToFile;
    }

    public static void writeToFile(String filename, String content) {
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(filename),"UTF-8");
            osw.write(content);
            osw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (osw != null)
                    osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
