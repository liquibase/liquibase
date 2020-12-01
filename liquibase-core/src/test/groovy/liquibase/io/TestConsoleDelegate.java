package liquibase.io;

import liquibase.exception.LiquibaseException;
import liquibase.ui.ConsoleDelegate;

import java.io.IOException;

public class TestConsoleDelegate extends ConsoleDelegate {
    private String returnValue;
    private int timerValue;
    public TestConsoleDelegate(String returnValue, int timerValue) {
        this.returnValue = returnValue;
        this.timerValue = timerValue;
    }

    @Override
    public boolean ready() throws LiquibaseException, IOException {
        if (timerValue == 0) {
            return true;
        }
        if (timerValue < 0) {
            timerValue = (timerValue * 2) + 1;
        }
        try {
            Thread.sleep((timerValue / 2) * 1000);
        }
        catch (Exception e) {

        }
        return true;
    }

    @Override
    public String readLine() throws LiquibaseException {

        return returnValue;
    }
}
