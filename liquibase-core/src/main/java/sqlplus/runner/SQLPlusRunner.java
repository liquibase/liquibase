package sqlplus.runner;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

import java.io.IOException;

/**
 * Created by sbt-gette-is on 12.09.2017.
 */
public class SQLPlusRunner {
    private Logger log = LogFactory.getLogger();

    public static void run(Change change, Database database) {
        int err = 0;

        String dbserver_username = "KRAEV_AA";
        String dbserver_password = "ERIBTEST1";
        String dbserver_tns = "ONLINEA_ERIBPSI_B1";
        String chanka = "C:\\tools\\dbruner\\src\\com\\company\\scripts\\run.sql";

        try {

            Process p = Runtime.getRuntime().exec("sqlplus " + database.getConnection() + "/" + dbserver_password + '@' + dbserver_tns + " @" + chanka);

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
}
