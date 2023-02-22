package liquibase.logging;

import liquibase.util.StringUtil;

import java.io.PrintStream;

public enum SystemOutputStream {
    STDOUT(System.out),
    STDERR(System.err);

    private final PrintStream outputStream;

    SystemOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    public static SystemOutputStream getStream(String output) throws IllegalArgumentException {
        try {
            return SystemOutputStream.valueOf(output.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("WARNING: The output stream value '" + output + "' is not valid. Valid values include: '" + StringUtil.join(SystemOutputStream.values(), "', '", Object::toString) + "'");
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
