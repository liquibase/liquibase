package liquibase.io;

import liquibase.exception.LiquibaseException;
import liquibase.ui.ConsoleDelegate;

import java.io.IOException;

public class MockConsoleDelegate extends ConsoleDelegate {
    private final String returnValue;
    private final int timerValue;

    public MockConsoleDelegate(String returnValue, int timerValue) {
        this.returnValue = returnValue;
        this.timerValue = timerValue;
    }

    @Override
    public boolean ready() throws LiquibaseException, IOException {
        if (timerValue == 0) {
            return true;
        }
        if (timerValue < 0) {
            return false;
        }
        try {
            Thread.sleep(timerValue * 1000);
        }
        catch (Exception e) {
            //
        }
        return true;
    }

    @Override
    public String readLine() throws LiquibaseException {
        return returnValue;
    }
}
