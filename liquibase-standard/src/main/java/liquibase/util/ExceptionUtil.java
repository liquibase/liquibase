package liquibase.util;

import liquibase.command.CommandFailedException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.Callable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionUtil {
    /**
     * Find an exception in the stack of exception causes whose cause matches the desired one. Note that the exception
     * whose cause matches the desired cause is returned, NOT the cause itself.
     * @param exceptionToSearchIn the exception to search through
     * @param desiredCause the cause that should be found in the stack of causes in the exception to search in
     * @return the desired exception, if found, or null otherwise
     */
    public static Throwable findExceptionInCauseChain(Throwable exceptionToSearchIn, Class<?> desiredCause) {
        if (exceptionToSearchIn == null || exceptionToSearchIn.getCause() == null) {
            return null;
        } else if (desiredCause.isAssignableFrom(exceptionToSearchIn.getCause().getClass())) {
            return exceptionToSearchIn;
        } else {
            return findExceptionInCauseChain(exceptionToSearchIn.getCause(), desiredCause);
        }
    }

    //
    // Honor the expected flag on a CommandFailedException
    //
    public static boolean showExceptionInLog(Throwable exception) {
        Throwable t = exception;
        while (t != null) {
            if (t instanceof CommandFailedException && ((CommandFailedException) t).isExpected()) {
                return false;
            }
            t = t.getCause();
        }
        return true;
    }

    /**
     * Executes a given {@link Callable} and returns its result, swallowing any exceptions that occur.
     * If an exception is thrown, this method returns {@code null}.
     *
     * @param callback the code to execute
     * @param <T>      the return type of the {@link Callable}
     * @return the value returned by the callback, or {@code null} if an exception is thrown
     */
    public static <T> T doSilently(Callable<T> callback) {
        try {
            return callback.call();
        } catch (Exception ignored) {
            // Exception is silently ignored
        }
        return null;
    }

    /**
     * Executes a given {@link ExceptionRunnable}, swallowing any exceptions that occur.
     * This method does not return a value.
     *
     * @param callback the code to execute
     * @param <T>      the return type, if needed (otherwise it can be omitted)
     */
    public static <T> void doSilently(ExceptionRunnable callback) {
        try {
            callback.run();
        } catch (Exception ignored) {
            // Exception is silently ignored
        }
    }

    /**
     * Functional interface for code blocks that may throw an {@link Exception}.
     */
    @FunctionalInterface
    public interface ExceptionRunnable {
        /**
         * Executes the code block, potentially throwing an {@link Exception}.
         *
         * @throws Exception if an error occurs during execution
         */
        void run() throws Exception;
    }
}
