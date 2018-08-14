package liquibase.datatype;

import liquibase.Scope;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.datatype.core.BigIntType;
import liquibase.datatype.core.CharType;
import liquibase.datatype.core.IntType;
import liquibase.datatype.core.UnknownType;
import liquibase.exception.ServiceNotFoundException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.structure.core.DataType;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataTypeFactory {

    private static DataTypeFactory instance;

    private Map<String, List<Class<? extends LiquibaseDataType>>> registry = new ConcurrentHashMap<>();

    /**
     * Build the factory registry from all classes in the classpath that implement
     * {@link LiquibaseDataType}
     */
    protected DataTypeFactory() {
        try {
            for (LiquibaseDataType type : Scope.getCurrentScope().getServiceLocator().findInstances(LiquibaseDataType.class)) {
                //noinspection unchecked
                register(type);
            }

        } catch (ServiceNotFoundException e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    /**
     * Get this factory singleton
     * @return a reference to this factory
     */
    public static synchronized DataTypeFactory getInstance() {
        if (instance == null) {
            instance = new DataTypeFactory();
        }
        return instance;
    }

    /**
     * Discards the active factory and creates a new singleton instance.
     */
    public static synchronized void reset() {
        instance = new DataTypeFactory();
    }

    /**
     * Registers an implementation of {@link LiquibaseDataType} with both its name and all aliases for the data type
     * as a handler in the factory's registry. Classes implement the {@link LiquibaseDataType#getPriority()}, which will
     * cause the class with the highest priority to become the primary handler for the data type.
     * @param type the implementation to register
     */
    public void register(LiquibaseDataType type) {
        try {
            List<String> names = new ArrayList<>();
            names.add(type.getName());
            names.addAll(Arrays.asList(type.getAliases()));

            Comparator<Class<? extends LiquibaseDataType>> comparator = (o1, o2) -> {
                try {
                    return -1 * Integer.compare(o1.newInstance().getPriority(), o2.newInstance().getPriority());
                } catch (Exception e) {
                    throw new UnexpectedLiquibaseException(e);
                }
            };

            for (String name : names) {
                name = name.toLowerCase(Locale.US);
                registry.computeIfAbsent(name, k -> new ArrayList<>());
                List<Class<? extends LiquibaseDataType>> classes = registry.get(name);
                classes.add(type.getClass());
                classes.sort(comparator);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Remove
     * @param name
     */
    public void unregister(String name) {
        registry.remove(name.toLowerCase(Locale.US));
    }

    /**
     * Translates a column data type definition (e.g. varchar(255), java.sql.Types.NVARCHAR(10),
     * VARCHAR2(255 BYTE)... ) into a normalized data type in object form. Note that, due to variety of allowed ways
     * to specify a data type (SQL-Standard, Java type, native RDBMS type...), the dataTypeDefinition we receive for
     * processing may already be the native type for the target RDBMS.
     * @param dataTypeDefinition the definition from the changeSet
     * @param database the {@link Database} object from for which the native definition is to be generated
     * @return the corresponding Liquibase data type in object form.
     */
    public LiquibaseDataType fromDescription(String dataTypeDefinition, Database database) {
        if (dataTypeDefinition == null) {
            return null;
        }
        String dataTypeName = dataTypeDefinition;

        // Remove the first occurrence of (anything within parentheses). This will remove the size information from
        // most data types, e.g. VARCHAR2(255 CHAR) -> VARCHAR2. We will retrieve that length information again later,
        // but for the moment, we are only interested in the "naked" data type name.
        if (dataTypeName.matches(".+\\(.*\\).*")) {
            dataTypeName = dataTypeName.replaceFirst("\\s*\\(.*\\)", "");
        }

        // Remove everything { after the first opening curly bracket
        // e.g. int{autoIncrement:true}" -> "int"
        if (dataTypeName.matches(".+\\{.*")) {
            dataTypeName = dataTypeName.replaceFirst("\\s*\\{.*", "");
        }

        // If the remaining string ends with " identity", then remove the " identity" and remember than we want
        // to set the autoIncrement property later.
        boolean autoIncrement = false;
        if (dataTypeName.toLowerCase(Locale.US).endsWith(" identity")) {
            dataTypeName = dataTypeName.toLowerCase(Locale.US).replaceFirst(" identity$", "");
            autoIncrement = true;
        }

        // unquote delimited identifiers
        final String[][] quotePairs = new String[][] {
            { "\"", "\"" }, // double quotes
            { "[",  "]"  }, // square brackets (a la mssql)
            { "`",  "`"  }, // backticks (a la mysql)
            { "'",  "'"  }  // single quotes
        };

        for (String[] quotePair : quotePairs) {
            String openQuote = quotePair[0];
            String closeQuote = quotePair[1];
            if (dataTypeName.startsWith(openQuote)) {
                int indexOfCloseQuote = dataTypeName.indexOf(closeQuote, openQuote.length());
                if ((indexOfCloseQuote != -1) && (dataTypeName.indexOf(closeQuote, indexOfCloseQuote + closeQuote
                    .length()) == -1)) {
                    dataTypeName = dataTypeName.substring(openQuote.length(), indexOfCloseQuote) +
                            dataTypeName.substring(indexOfCloseQuote + closeQuote.length(), dataTypeName.length());
                    break;
                }
            }
        }

        // record additional information that is still attached to the data type name
        String additionalInfo = null;
        if (dataTypeName.toLowerCase(Locale.US).startsWith("bit varying")
            || dataTypeName.toLowerCase(Locale.US).startsWith("character varying")) {
            // not going to do anything. Special case for postgres in our tests,
            // need to better support handling these types of differences
        } else {
            // Heuristic: from what we now have left of the data type name, everything after the first space
            // is counted as additional information.
            String[] splitTypeName = dataTypeName.trim().split("\\s+", 2);
            dataTypeName = splitTypeName[0];
            if (splitTypeName.length > 1) {
                additionalInfo = splitTypeName[1];
            }
        }

        // try to find matching classes for the data type name in our registry
        Collection<Class<? extends LiquibaseDataType>> classes = registry.get(dataTypeName.toLowerCase(Locale.US));

        LiquibaseDataType liquibaseDataType = null;
        if (classes == null) {
            // Map (date/time) INTERVAL types to the UnknownType
            if (dataTypeName.toUpperCase(Locale.US).startsWith("INTERVAL")) {
                liquibaseDataType = new UnknownType(dataTypeDefinition);
            } else {
                liquibaseDataType = new UnknownType(dataTypeName);
            }
        } else {
            // Iterate through the list (which is already sorted by priority) until we find a class
            // for this dataTypeName that supports the given database.
            Iterator<Class<? extends LiquibaseDataType>> iterator = classes.iterator();
            do {
                try {
                    liquibaseDataType = iterator.next().newInstance();
                } catch (Exception e) {
                    throw new UnexpectedLiquibaseException(e);
                }
            } while ((database != null) && !liquibaseDataType.supports(database) && iterator.hasNext());
        }
        if ((database != null) && !liquibaseDataType.supports(database)) {
            throw new UnexpectedLiquibaseException("Could not find type for " + liquibaseDataType.toString() +
                    " for DBMS "+database.getShortName());
        }
        if (liquibaseDataType == null) {
            liquibaseDataType = new UnknownType(dataTypeName);
        }
        liquibaseDataType.setAdditionalInformation(additionalInfo);

        // Does the type string have the form "some_data_type(additional,info,separated,by,commas)"?
        // If so, process these as additional data type parameters.
        if (dataTypeDefinition.matches(".+\\s*\\(.*")) {
            // Cut out the part between the first ()
            String paramStrings = dataTypeDefinition.replaceFirst(".*?\\(", "").replaceFirst("\\).*", "");
            String[] params = paramStrings.split(",");

            for (String param : params) {
                param = StringUtil.trimToNull(param);
                if (param != null) {
                    if ((liquibaseDataType instanceof CharType) && !(database instanceof OracleDatabase)) {
                        // TODO this might lead to wrong snapshot results in Oracle Database, because it assumes
                        // NLS_LENGTH_SEMANTICS=BYTE. If NLS_LENGTH_SEMANTICS=CHAR, we need to trim " CHAR" instead.
    
                        // not sure what else supports it:
                        param = param.replaceFirst(" BYTE", ""); //only use byte types on oracle,
                        
                    }
                    liquibaseDataType.addParameter(param);
                }
            }
        }

        // Did the original definition have embedded information in curly braces, e.g.
        // "int{autoIncrement:true}"? If so, we will extract and process it now.
        if (dataTypeDefinition.matches(".*\\{.*")) {
            String paramStrings = dataTypeDefinition.replaceFirst(".*?\\{", "")
                .replaceFirst("\\}.*", "");
            String[] params = paramStrings.split(",");
            for (String param : params) {
                param = StringUtil.trimToNull(param);
                if (param != null) {
                    String[] paramAndValue = param.split(":", 2);
                    // TODO: A run-time exception will occur here if the user writes a property name into the
                    // data type which does not exist - but what else could we do in this case, except aborting?
                    ObjectUtil.setProperty(liquibaseDataType, paramAndValue[0], paramAndValue[1]);
                }
            }
        }

        if (autoIncrement && (liquibaseDataType instanceof IntType)) {
            ((IntType) liquibaseDataType).setAutoIncrement(true);
        }
        if (autoIncrement && (liquibaseDataType instanceof BigIntType)) {
            ((BigIntType) liquibaseDataType).setAutoIncrement(true);
        }

        liquibaseDataType.finishInitialization(dataTypeDefinition);

        return liquibaseDataType;

    }


    public LiquibaseDataType fromObject(Object object, Database database) {
        if (object instanceof ColumnConfig.ValueNumeric) {
            object = ((ColumnConfig.ValueNumeric) object).getDelegate();
        }
        return fromDescription(object.getClass().getName(), database);
    }

    public LiquibaseDataType from(DataType type, Database database) {
        if (type == null) {
            return null;
        }
        return fromDescription(type.toString(), database);
    }

    public LiquibaseDataType from(DatabaseDataType type, Database database) {
        if (type == null) {
            return null;
        }
        return fromDescription(type.toString(), database);
    }

    public String getTrueBooleanValue(Database database) {
        return fromDescription("boolean", database).objectToSql(true, database);
    }

    public String getFalseBooleanValue(Database database) {
        return fromDescription("boolean", database).objectToSql(false, database);
    }
}
