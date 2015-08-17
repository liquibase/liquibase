package liquibase.structure.core;

import liquibase.AbstractExtensibleObject;
import liquibase.structure.DateWithTimezone;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataType extends AbstractExtensibleObject {

    public String name;
    public StandardType standardType;
    public Class valueType;
    public String origin;
    public StringClauses clausesBeforeParameters = new StringClauses();
    public List<String> parameters = new ArrayList<>();
    public StringClauses clausesAfterParameters = new StringClauses();

    public DataType() {
    }

    public DataType(String name, Object... parameters) {
        this.name = name;
        if (parameters != null) {
            for (Object param : parameters) {
                if (param == null) {
                    this.parameters.add(null);

                } else {
                    this.parameters.add(param.toString());
                }
            }
        }
    }

    public DataType(StandardType type, Object... parameters) {
        this(type.name(), parameters);
    }

    public DataType(String name, StringClauses clausesBeforeParameters, String[] parameters, StringClauses clausesAfterParameters) {
        this(name, parameters);
        this.clausesBeforeParameters = clausesBeforeParameters;
        this.clausesAfterParameters = clausesAfterParameters;
    }

    public static DataType parse(String typeString) {
        typeString = StringUtils.trimToEmpty(typeString);

        DataType dataType = new DataType();
        Matcher nameMatcher = Pattern.compile("^(\\w+)").matcher(typeString);
        if (nameMatcher.find()) {
            dataType.name = nameMatcher.group(1);
        } else {
            return dataType;
        }

        Matcher matcher = Pattern.compile("(.*)\\((.*)\\)(.*)").matcher(typeString);
        if (matcher.find()) {
            String beforeParens = matcher.group(1);
            String inParens = matcher.group(2);
            String afterParens = matcher.group(3);
            List<String> params = StringUtils.splitAndTrim(inParens, "\\s*,\\s*");
            dataType.parameters = params;
            dataType.clausesBeforeParameters = new StringClauses(" ").append(StringUtils.trimToNull(beforeParens.substring(dataType.name.length())));
            dataType.clausesAfterParameters = new StringClauses(" ").append(afterParens);
        } else {
            dataType.clausesBeforeParameters = new StringClauses(" ").append(StringUtils.trimToNull(typeString.substring(dataType.name.length())));
        }

        dataType.standardType = standardType(dataType.name);
        if (dataType.standardType != null) {
            dataType.valueType = dataType.standardType.valueType;
        }
        return dataType;
    }

    /**
     * Guesses at the standard type that corresponds to the given name. Returns null if no known corresponding type.
     */
    public static StandardType standardType(String name) {
        if (name == null) {
            return null;
        }
        if (name.equalsIgnoreCase("INT")) {
            name = "INTEGER";
        }
        if (name.equalsIgnoreCase("DATETIME")) {
            name = "TIMESTAMP";
        }
        if (name.equalsIgnoreCase("DATETIME WITH TIMEZONE")) {
            name = "TIMESTAMPZ";
        }
        if (name.equalsIgnoreCase("TIME WITH TIMEZONE")) {
            name = "TIMEZ";
        }
        try {
            return StandardType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        String returnString = StringUtils.trimToEmpty(name);
        if (clausesBeforeParameters != null && !clausesBeforeParameters.isEmpty()) {
            returnString += " "+clausesBeforeParameters+" ";
        }

        if (this.parameters != null && this.parameters.size() > 0) {
            StringClauses parameters = new StringClauses("(", ", ", ")");
            for (String string : this.parameters) {
                parameters.append(string);
            }

            returnString += parameters.toString() + " ";
        }

        if (clausesAfterParameters != null && !clausesAfterParameters.isEmpty()) {
            returnString += clausesAfterParameters;
        }

        return StringUtils.trimToEmpty(returnString);
    }

    public enum StandardType {
        BIGINT(BigInteger.class),
        BIT(Short.class),
        BLOB(Object.class),
        BOOLEAN(Boolean.class),
        CHAR(String.class),
        CLOB(String.class),
        CURRENCY(BigDecimal.class),
        DATE(Date.class),
        DECIMAL(BigDecimal.class),
        DOUBLE(Double.class),
        FLOAT(Float.class),
        INTEGER(Integer.class),
        MEDIUMINT(Integer.class),
        NCHAR(String.class),
        NUMERIC(BigDecimal.class),
        NVARCHAR(String.class),
        REAL(BigDecimal.class),
        SMALLINT(Short.class),
        TIMESTAMP(Date.class),
        TIMESTAMPZ(DateWithTimezone.class),
        TIME(Date.class),
        TIMEZ(DateWithTimezone.class),
        TINYINT(Short.class),
        UUID(java.util.UUID.class),
        VARCHAR(String.class),
        XML(String.class);

        public final Class valueType;

        StandardType(Class valueType) {
            this.valueType = valueType;
        }
    }


}
