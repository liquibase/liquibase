package liquibase.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Miscellaneous utility methods for number conversion and parsing.
 * Mainly for internal use within the framework; consider Jakarta's
 * Commons Lang for a more comprehensive suite of string utilities.
 */
public abstract class NumberUtil {

    /**
     * Convert the given number into an instance of the given target class.
     *
     * @deprecated use {@link ObjectUtil#convert(Object, Class)}
     */
    public static Number convertNumberToTargetClass(Number number, Class targetClass) throws IllegalArgumentException {
        return (Number) ObjectUtil.convert(number, targetClass);
    }

    /**
     * Parse the given text into a number instance of the given target class,
     * using the corresponding default <code>decode</code> methods. Trims the
     * input <code>String</code> before attempting to parse the number. Supports
     * numbers in hex format (with leading 0x) and in octal format (with leading 0).
     *
     * @param text        the text to convert
     * @param targetClass the target class to parse into
     * @return the parsed number
     * @throws IllegalArgumentException if the target class is not supported
     *                                  (i.e. not a standard Number subclass as included in the JDK)
     * @see java.lang.Byte#decode
     * @see java.lang.Short#decode
     * @see java.lang.Integer#decode
     * @see java.lang.Long#decode
     * @see java.lang.Float#valueOf
     * @see java.lang.Double#valueOf
     * @see java.math.BigDecimal#BigDecimal(String)
     */
    public static Number parseNumber(String text, Class targetClass) {
        String trimmed = text.trim();

        if (targetClass.equals(Byte.class)) {
            return Byte.decode(trimmed);
        } else if (targetClass.equals(Short.class)) {
            return Short.decode(trimmed);
        } else if (targetClass.equals(Integer.class)) {
            return Integer.decode(trimmed);
        } else if (targetClass.equals(Long.class)) {
            return Long.decode(trimmed);
        } else if (targetClass.equals(BigInteger.class)) {
            return new BigInteger(trimmed);
        } else if (targetClass.equals(Float.class)) {
            return Float.valueOf(trimmed);
        } else if (targetClass.equals(Double.class)) {
            return Double.valueOf(trimmed);
        } else if (targetClass.equals(BigDecimal.class) || targetClass.equals(Number.class)) {
            return new BigDecimal(trimmed);
        } else {
            throw new IllegalArgumentException(
                    "Cannot convert String [" + text + "] to target class [" + targetClass.getName() + "]");
        }
    }
}
