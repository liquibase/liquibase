package sqlplus.runner;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.SqlPlusException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import org.apache.tools.ant.taskdefs.StreamPumper;
import sqlplus.context.SqlPlusContext;

import java.io.*;


/**
 * @author gette
 */
public class SQLPlusRunner {
    private static Logger log = LogFactory.getLogger();

    public static void run(String pathToFile) throws SqlPlusException {
        log.debug("SQLPlusRunner has been initialized.");
        try {
            if (SqlPlusContext.getInstance().isManual()) {
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

            log.debug("Command:" + "cmd.exe /c sqlplus " + SqlPlusContext.getInstance().getConnection().getConnectionAsString() + " @" + pathToFile);
            Process sqlplus = Runtime.getRuntime().exec("cmd.exe /c sqlplus " + SqlPlusContext.getInstance().getConnection().getConnectionAsString() + " @" + pathToFile);

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
                throw new SqlPlusException("Something went wrong with SQLPLUS. ORA-00" + err);
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

//        String fileName = changeSet.getId().concat(changeSet.getAuthor()).concat(".sql");
//        boolean flag = true;
//        while (flag) {
//            int i = 0;
//            if (Files.exists(Paths.get(System.getProperty("user.dir") + "/" + fileName))) {
//                i++;
//                fileName = fileName.replace(".sql", "_" + i + ".sql");
//            }
//            flag = false;
//        }
        writeToFile(System.getProperty("user.dir") + "\\changeSet.sql", sqlplus);
        return System.getProperty("user.dir") + "\\changeSet.sql";
    }

    public static void writeToFile(String filename, String content) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(filename);
            bw = new BufferedWriter(fw);
            bw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
