package sqlplus.runner;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.SqlPlusException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import sqlplus.context.SqlPlusContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by sbt-gette-is on 12.09.2017.
 */
public class SQLPlusRunner {
    private static Logger log = LogFactory.getLogger();

    public static void run(String pathToFile) throws SqlPlusException {
        log.debug("FORK! SQLPlusRunner funktioniert!");
        try {
            Process notepad = new ProcessBuilder("notepad", pathToFile).start();
            notepad.waitFor();
            notepad.destroy();
            executeSQLPlus(pathToFile);

        } catch (IOException e) {
            System.err.println(e.getStackTrace());
        } catch (InterruptedException e) {
            System.err.println(e.getStackTrace());
        }
    }

    public static void executeSQLPlus(String pathToFile) throws IOException, SqlPlusException, InterruptedException {
        try {
            int err = 0;
            log.debug("ЗАПУСК!");
            //Process sqlplus = new ProcessBuilder("sqlplus " + SqlPlusContext.getInstance().getConnection() + "@" + pathToFile).start();
            log.debug("FULL CMD:" + "cmd.exe /c sqlplus " + SqlPlusContext.getInstance().getConnection().getConnectionAsString() + " @" + pathToFile);
            Process sqlplus = Runtime.getRuntime().exec("cmd.exe /c sqlplus " + SqlPlusContext.getInstance().getConnection().getConnectionAsString() + " @" + pathToFile);
            //Process sqlplus = Runtime.getRuntime().exec("sqlplus");
            // copy input and error to the output stream
            StreamPumper inputPumper =
                    new StreamPumper(sqlplus.getInputStream());
            StreamPumper errorPumper =
                    new StreamPumper(sqlplus.getErrorStream());

            // starts pumping away the generated output/error
            inputPumper.start();
            errorPumper.start();

            // Wait for everything to finish
            sqlplus.waitFor();
            inputPumper.join();
            errorPumper.join();
            sqlplus.destroy();

            // check its exit value
            err = sqlplus.exitValue();
            if (err == 0) {
                System.out.println("================================================");
                System.out.println("Конвертация закончена успешно: " + err);
            } else {
                throw new SqlPlusException("Получен код выполнения, отличный от нуля. Код выполнения: " + err);
                //TODO опрос, что делать дальше
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SqlPlusException(e);
        } catch (SqlPlusException e) {
            e.printStackTrace();
            throw new SqlPlusException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        log.debug("SQLPLUS Script: " + sqlplus);
        ChangeSet changeSet = change.getChangeSet();
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
