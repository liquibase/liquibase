package sqlplus.runner;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import sqlplus.context.SqlPlusContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by sbt-gette-is on 12.09.2017.
 */
public class SQLPlusRunner {
    private static Logger log = LogFactory.getLogger();

    public static void run(SqlStatement[] statements) {
        log.debug("FORK! SQLPlusRunner funktioniert!");
        int err = 0;

        String chanka = "C:\\tools\\dbruner\\src\\com\\company\\scripts\\run.sql";

        try {

            Process p = Runtime.getRuntime().exec("sqlplus " + SqlPlusContext.getInstance().getConnection() + "@" + chanka);

            // copy input and error to the output stream
            StreamPumper inputPumper =
                    new StreamPumper(p.getInputStream());
            StreamPumper errorPumper =
                    new StreamPumper(p.getErrorStream());

            // starts pumping away the generated output/error
            inputPumper.start();
            errorPumper.start();

            // Wait for everything to finish
            p.waitFor();
            inputPumper.join();
            errorPumper.join();
            p.destroy();

            // check its exit value
            err = p.exitValue();
            if (err == 0) {
                System.out.println("================================================");
                System.out.println("Конвертация закончена успешно: " + err);
            } else {
                System.out.println("================================================");
                System.out.println("Ошибка в процессе конвертации: " + err);
                Process proc = new ProcessBuilder("C:\\WINDOWS\\system32\\notepad.exe", chanka).start();
                //TODO опрос, что делать дальше
            }

        } catch (IOException e) {
            System.err.println(e.getStackTrace());
        } catch (InterruptedException e) {
            System.err.println(e.getStackTrace());
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
        writeToFile(System.getProperty("user.dir") + "/changeSet.sql", sqlplus);
        return System.getProperty("user.dir") + "/changeSet.sql";
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
