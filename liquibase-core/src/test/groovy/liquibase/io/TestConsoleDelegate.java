package liquibase.io;

import liquibase.exception.LiquibaseException;
import liquibase.ui.ConsoleDelegate;

import java.io.Console;

public class TestConsoleDelegate extends ConsoleDelegate {
    private String returnValue;
    private int timerValue;
    public TestConsoleDelegate(String returnValue, int timerValue) {
        this.returnValue = returnValue;
        this.timerValue = timerValue;
    }

    @Override
    public String readLine() throws LiquibaseException {
        try {
           Thread.sleep((timerValue / 2) * 1000);
        }
        catch (Exception e) {

        }
        return returnValue;
    }
}
