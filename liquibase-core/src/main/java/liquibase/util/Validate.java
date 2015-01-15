package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;

/**
 * Similar to assert logic, but will run regardless of JVM settings. Throws {@link liquibase.exception.UnexpectedLiquibaseException} if assertions fail.
 * Based off org.apache.commons.lang.Validate.
 */
public class Validate {


    /**
     * Throws exception if passed object is null
     */
    public static void notNull(Object object, String failMessage) throws UnexpectedLiquibaseException {
        if (object == null) {
            fail(failMessage);
        }
    }

    /**
     * Throws exception if test is not true.
     */
    public static void isTrue(boolean test, String failMessage) throws UnexpectedLiquibaseException{
        if (!test) {
            fail(failMessage);
        }

    }

    public static void fail(String failMessage) {
        throw new UnexpectedLiquibaseException(failMessage);
    }

    public static UnexpectedLiquibaseException failure(String failMessage) {
        throw new UnexpectedLiquibaseException(failMessage);
    }
}
