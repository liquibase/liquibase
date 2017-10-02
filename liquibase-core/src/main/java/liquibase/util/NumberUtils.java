package liquibase.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Miscellaneous utility methods for number conversion and parsing.
 * Mainly for internal use within the framework; consider Jakarta's
 * Commons Lang for a more comprehensive suite of string utilities.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1.2
 */
public abstract class NumberUtils {

    /**
     * Convert the given number into an instance of the given target class.
     *
     * @param number      the number to convert
     * @param targetClass the target class to convert to
     * @return the converted number
     * @throws IllegalArgumentException if the target class is not supported
     *                                  (i.e. not a standard Number subclass as included in the JDK)
     * @see java.lang.Byte
     * @see java.lang.Short
     * @see java.lang.Integer
     * @see java.lang.Long
     * @see java.math.BigInteger
     * @see java.lang.Float
     * @see java.lang.Double
     * @see java.math.BigDecimal
     */
    public static Number convertNumberToTargetClass(Number number, Class targetClass)
            throws IllegalArgumentException {

        if (targetClass.isInstance(number)) {
            return number;
        } else if (targetClass.equals(Byte.class)) {
            long value = number.longValue();
            if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE)) {
                raiseOverflowException(number, targetClass);
            }
            return number.byteValue();
        } else if (targetClass.equals(Short.class)) {
            long value = number.longValue();
            if ((value < Short.MIN_VALUE) || (value > Short.MAX_VALUE)) {
                raiseOverflowException(number, targetClass);
            }
            return number.shortValue();
        } else if (targetClass.equals(Integer.class)) {
            long value = number.longValue();
            if ((value < Integer.MIN_VALUE) || (value > Integer.MAX_VALUE)) {
                raiseOverflowException(number, targetClass);
            }
            return number.intValue();
        } else if (targetClass.equals(Long.class)) {
            return number.longValue();
        } else if (targetClass.equals(Float.class)) {
            return number.floatValue();
        } else if (targetClass.equals(Double.class)) {
            return number.doubleValue();
        } else if (targetClass.equals(BigInteger.class)) {
            return BigInteger.valueOf(number.longValue());
        } else if (targetClass.equals(BigDecimal.class)) {
            // using BigDecimal(String) here, to avoid unpredictability of BigDecimal(double)
            // (see BigDecimal javadoc for details)
            return new BigDecimal(number.toString());
        } else {
            throw new IllegalArgumentException("Could not convert number [" + number + "] of type [" +
                    number.getClass().getName() + "] to unknown target class [" + targetClass.getName() + "]");
        }
    }

    /**
     * Raise an overflow exception for the given number and target class.
     *
     * @param number      the number we tried to convert
     * @param targetClass the target class we tried to convert to
     */
    private static void raiseOverflowException(Number number, Class targetClass) {
        throw new IllegalArgumentException("Could not convert number [" + number + "] of type [" +
                number.getClass().getName() + "] to target class [" + targetClass.getName() + "]: overflow");
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
     * @see #decodeBigInteger(String)
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
            return decodeBigInteger(trimmed);
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

    /**
     * Parse the given text into a number instance of the given target class,
     * using the given NumberFormat. Trims the input <code>String</code>
     * before attempting to parse the number.
     *
     * @param text         the text to convert
     * @param targetClass  the target class to parse into
     * @param numberFormat the NumberFormat to use for parsing (if <code>null</code>,
     *                     this method falls back to <code>parseNumber(String, Class)</code>)
     * @return the parsed number
     * @throws IllegalArgumentException if the target class is not supported
     *                                  (i.e. not a standard Number subclass as included in the JDK)
     * @see java.text.NumberFormat#parse
     * @see #convertNumberToTargetClass
     * @see #parseNumber(String,Class)
     */
    public static Number parseNumber(String text, Class targetClass, NumberFormat numberFormat) {
        if (numberFormat != null) {
            try {
                Number number = numberFormat.parse(text.trim());
                return convertNumberToTargetClass(number, targetClass);
            }
            catch (ParseException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        } else {
            return parseNumber(text, targetClass);
        }
    }

    /**
     * Decode a {@link java.math.BigInteger} from a {@link String} value.
     * Supports decimal, hex and octal notation.
     *
     * @see BigInteger#BigInteger(String,int)
     */
    private static BigInteger decodeBigInteger(String value) {
        int radix = 10;
        int index = 0;
        boolean negative = false;

        // Handle minus sign, if present.
        if (value.startsWith("-")) {
            negative = true;
            index++;
        }

        // Handle radix specifier, if present.
        if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        } else if (value.startsWith("#", index)) {
            index++;
            radix = 16;
        } else if (value.startsWith("0", index) && (value.length() > (1 + index))) {
			index++;
			radix = 8;
		}

		BigInteger result = new BigInteger(value.substring(index), radix);
		return (negative ? result.negate() : result);
	}

    /**
     * Convenience method for converting a string to an Integer object
     * @param value string value
     * @return Null if the value is null, otherwise the converted integer
     */
    public static Integer readInteger(String value) {
        if (value == null) {
            return null;
        }
        return Integer.valueOf(value);
    }

}