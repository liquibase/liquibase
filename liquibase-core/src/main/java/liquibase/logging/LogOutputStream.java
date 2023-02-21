package liquibase.logging;

import liquibase.util.StringUtil;

import java.io.PrintStream;

public enum LogOutputStream {
    STDOUT(System.out),
    STDERR(System.err);

    private final PrintStream outputStream;

    LogOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    public static LogOutputStream getStream(String output) throws IllegalArgumentException {
        try {
            return LogOutputStream.valueOf(output.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("WARNING: The LogStream value '" + output + "' is not valid. Valid values include: '" + StringUtil.join(LogOutputStream.values(), "', '", Object::toString) + "'");
        }
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    /**
     * Set the system output stream or error stream based on enum's value
     */
    public void configureSystemOutputStream() {
        switch (this) {
            case STDOUT:
                System.setErr(this.getOutputStream());
            case STDERR:
                System.setOut(this.getOutputStream());
        }
    }
}
