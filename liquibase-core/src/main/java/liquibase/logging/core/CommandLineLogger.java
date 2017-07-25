package liquibase.logging.core;

import liquibase.logging.LogTarget;

public class CommandLineLogger extends Slf4jLogger{

    private CommandLineLogger.Mode mode = CommandLineLogger.Mode.standard;

    public CommandLineLogger(org.slf4j.Logger logger) {
        super(logger);
    }

    public void setMode(CommandLineLogger.Mode mode) {
        this.mode = mode;
    }

    protected void printOutput(LogTarget target, String message) {
        if (this.mode == Mode.standard) {
            if (target == LogTarget.USER) {
                System.out.println(message);
            }
        } else if (this.mode == Mode.pipe) {
            if (target == LogTarget.USER) {
                System.err.println(message);
            } else if (target == LogTarget.PIPE) {
                System.out.println(message);
            }
        }
    }

    @Override
    public void error(LogTarget target, String message) {
        super.error(target, message);
        printOutput(target, message);
    }


    @Override
    public void error(LogTarget target, String message, Throwable e) {
        super.error(target, message, e);
        printOutput(target, message);
    }

    @Override
    public void warn(LogTarget target, String message) {
        super.warn(target, message);
        printOutput(target, message);
    }

    @Override
    public void warn(LogTarget target, String message, Throwable e) {
        super.warn(target, message, e);
        printOutput(target, message);
    }

    @Override
    public void info(LogTarget target, String message) {
        super.info(target, message);
        printOutput(target, message);
    }

    @Override
    public void info(LogTarget target, String message, Throwable e) {
        super.info(target, message, e);
        printOutput(target, message);
    }

    @Override
    public void sql(LogTarget target, String sqlStatement) {
        super.sql(target, sqlStatement);
        printOutput(target, sqlStatement);
    }

    @Override
    public void debug(LogTarget target, String message) {
        super.debug(target, message);
        printOutput(target, message);
    }

    @Override
    public void debug(LogTarget target, String message, Throwable e) {
        super.debug(target, message, e);
        printOutput(target, message);
    }

    public static enum Mode {
        standard,
        pipe
    }
}
