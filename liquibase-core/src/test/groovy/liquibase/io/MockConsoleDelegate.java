package liquibase.io;

import liquibase.exception.LiquibaseException;
import liquibase.ui.ConsoleDelegate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockConsoleDelegate extends ConsoleDelegate {
    private String[] returnValues = null;
    private final int timerValue;
    private int iteration;

    public MockConsoleDelegate(int timerValue, String ... returnValues) {
        this.timerValue = timerValue;
        this.returnValues = returnValues;
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
    public boolean hasConsole() {
        return true;
    }

    @Override
    public String readLine() throws LiquibaseException {
        if (iteration >= returnValues.length) {
            return null;
        }
        String value = returnValues[iteration];
        iteration++;
        return value;
    }
}
