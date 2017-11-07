package sqlplus.runner;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.SqlPlusException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import sqlplus.context.SqlPlusContext;

import java.io.*;
import java.util.Map;


/**
 * @author gette
 */
public class SQLPlusRunner {
    private static Logger log = LogFactory.getLogger();

    private static final String OSName = System.getProperty("os.name").toLowerCase();
    private static final String SQLPLUS = "sqlplus ";

    /**
     * Executing SQL*Plus via command line (supports bash and cmd)
     * Manual mode is available only in WinOS family
     * Because we are using light version of Notepad++
     * @param pathToFile
     * @throws SqlPlusException
     */
    public static void run(String pathToFile) throws SqlPlusException {
        log.debug("SQLPlusRunner has been initialized.");
        try {
            //adding condition which protects from crashing on Linux OS when using manual mode
            if (SqlPlusContext.getInstance().isManual() && OSName.contains("win")) {
                Process notepad = new ProcessBuilder(System.getProperty("user.dir") + "/npp/notepad++.exe", "-nosession","-notabbar", pathToFile).start();
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

    /**
     * Building command and executing SQL*Plus
     * @param pathToFile sql-file which need to be proceed
     * @throws IOException
     * @throws SqlPlusException
     * @throws InterruptedException
     */
    public static void executeSQLPlus(String pathToFile) throws IOException, SqlPlusException, InterruptedException {
        try {
            int err = 0;
            log.debug("Launching SQLPLUS.");
            log.debug("EXECUTING: " + SQLPLUS + SqlPlusContext.getInstance().getConnection().getConnectionAsString() + " @" + pathToFile);

            ProcessBuilder pb = new ProcessBuilder(SQLPLUS, SqlPlusContext.getInstance().getConnection().getConnectionAsString(), "@" + pathToFile);
            Map<String, String> env = pb.environment();
            env.put("NLS_LANG", "AMERICAN_AMERICA.AL32UTF8");

            Process sqlplus = pb.start();

            StreamPumper inputPumper = new StreamPumper(sqlplus.getInputStream());
            StreamPumper errorPumper = new StreamPumper(sqlplus.getErrorStream());
            inputPumper.start();
            errorPumper.start();
            sqlplus.waitFor();
            inputPumper.join();
            errorPumper.join();
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

    /**
     * Using default method from Change and generating statements
     * Then packing these statements into file
     * @param change standard liquibase Change-object
     * @param database standard liquibase Database-object
     * @return
     */
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
            osw = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");
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

    /**
     * Using ant StreamPumper for real-time output of SQL*Plus execution
     */
    static class StreamPumper extends Thread {
        private BufferedReader din;
        private boolean endOfStream = false;
        private static final int SLEEP_TIME = 5;

        public StreamPumper(InputStream is) {
            this.din = new BufferedReader(new InputStreamReader(is));
        }

        public void pumpStream() throws IOException {

            if (!endOfStream) {
                String line = din.readLine();

                if (line != null) {
                    System.out.println(line);
                    log.sqlplus(line);
                } else {
                    endOfStream = true;
                }
            }
        }

        @Override
        public void run() {
            try {
                try {
                    while (!endOfStream) {
                        pumpStream();
                        sleep(SLEEP_TIME);
                    }
                } catch (InterruptedException ie) {
                    //ignore
                }
                din.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
}

