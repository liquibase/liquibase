package liquibase.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
}
