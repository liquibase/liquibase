package liquibase.datatype;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.datatype.core.BigIntType;
import liquibase.datatype.core.IntType;
import liquibase.datatype.core.UnknownType;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.core.DataType;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataTypeFactory {

    private static DataTypeFactory instance;

    private Map<String, SortedSet<Class<? extends LiquibaseDataType>>> registry = new ConcurrentHashMap<String, SortedSet<Class<? extends LiquibaseDataType>>>();

    protected DataTypeFactory() {
        Class<? extends LiquibaseDataType>[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(LiquibaseDataType.class);

            for (Class<? extends LiquibaseDataType> clazz : classes) {
                //noinspection unchecked
                register(clazz);
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public static synchronized DataTypeFactory getInstance() {
        if (instance == null) {
            instance = new DataTypeFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new DataTypeFactory();
    }


    public void register(Class<? extends LiquibaseDataType> dataTypeClass) {
        try {
            LiquibaseDataType example = dataTypeClass.newInstance();
            List<String> names = new ArrayList<String>();
            names.add(example.getName());
            names.addAll(Arrays.asList(example.getAliases()));

            for (String name : names) {
                name = name.toLowerCase();
                if (registry.get(name) == null) {
                    registry.put(name, new TreeSet<Class<? extends LiquibaseDataType>>(new Comparator<Class<? extends LiquibaseDataType>>() {
                        @Override
                        public int compare(Class<? extends LiquibaseDataType> o1, Class<? extends LiquibaseDataType> o2) {
                            try {
                                return -1 * new Integer(o1.newInstance().getPriority()).compareTo(o2.newInstance().getPriority());
                            } catch (Exception e) {
                                throw new UnexpectedLiquibaseException(e);
                            }
                        }
                    }));
                }
                registry.get(name).add(dataTypeClass);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void unregister(String name) {
        registry.remove(name.toLowerCase());
    }

    public Map<String, SortedSet<Class<? extends LiquibaseDataType>>> getRegistry() {
        return registry;
    }

//    public LiquibaseDataType fromDescription(String dataTypeDefinition) {
//        return fromDescription(dataTypeDefinition, null);
//    }

    public LiquibaseDataType fromDescription(String dataTypeDefinition, Database database) {
        String dataTypeName = dataTypeDefinition;
        if (dataTypeName.matches(".+\\(.*\\).*")) {
            dataTypeName = dataTypeDefinition.replaceFirst("\\s*\\(.*\\)", "");
        }
        if (dataTypeName.matches(".+\\{.*")) {
            dataTypeName = dataTypeName.replaceFirst("\\s*\\{.*", "");
        }
        boolean primaryKey = false;
        if (dataTypeName.endsWith(" identity")) {
            dataTypeName = dataTypeName.replaceFirst(" identity$", "");
            primaryKey = true;
        }

        String additionalInfo = null;
        if (dataTypeName.toLowerCase().startsWith("bit varying") || dataTypeName.toLowerCase().startsWith("character varying")) {
            //not going to do anything. Special case for postgres in our tests, need to better support handling these types of differences
        } else {
            String[] splitTypeName = dataTypeName.split("\\s+", 2);
            dataTypeName = splitTypeName[0];
            if (splitTypeName.length > 1) {
                additionalInfo = splitTypeName[1];
            }
        }

        SortedSet<Class<? extends LiquibaseDataType>> classes = registry.get(dataTypeName.toLowerCase());

        LiquibaseDataType liquibaseDataType = null;
        if (classes == null) {
            if (dataTypeName.toUpperCase().startsWith("INTERVAL")) {
                liquibaseDataType = new UnknownType(dataTypeDefinition);
            } else {
                liquibaseDataType = new UnknownType(dataTypeName);
            }
        } else {

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
            throw new UnexpectedLiquibaseException("Could not find type for "+liquibaseDataType.toString()+" for databaes "+database.getShortName());
        }
        if (liquibaseDataType == null) {
            liquibaseDataType = new UnknownType(dataTypeName);

        }
        liquibaseDataType.setAdditionalInformation(additionalInfo);

        if (dataTypeDefinition.matches(".+\\s*\\(.*")) {
            String paramStrings = dataTypeDefinition.replaceFirst(".*?\\(", "").replaceFirst("\\).*", "");
            String[] params = paramStrings.split(",");
            for (String param : params) {
                param = StringUtils.trimToNull(param);
                if (param != null) {
                    liquibaseDataType.addParameter(param);
                }
            }
        }

        /*
        The block below seems logically incomplete.
        It will always end up putting the second word after the entire type name
        e.g. character varying will become CHARACTER VARYING varying

        //try to something like "int(11) unsigned" or int unsigned but not "varchar(11 bytes)"
        String lookingForAdditionalInfo = dataTypeDefinition;
        lookingForAdditionalInfo = lookingForAdditionalInfo.replaceFirst("\\(.*\\)", "");
        if (lookingForAdditionalInfo.contains(" ")) {
            liquibaseDataType.setAdditionalInformation(lookingForAdditionalInfo.split(" ", 2)[1]);
        }*/

        if (dataTypeDefinition.matches(".*\\{.*")) {
            String paramStrings = dataTypeDefinition.replaceFirst(".*?\\{", "").replaceFirst("\\}.*", "");
            String[] params = paramStrings.split(",");
            for (String param : params) {
                param = StringUtils.trimToNull(param);
                if (param != null) {
                    String[] paramAndValue = param.split(":", 2);
                    try {
                        ObjectUtil.setProperty(liquibaseDataType, paramAndValue[0], paramAndValue[1]);
                    } catch (Exception e) {
                        throw new RuntimeException("Unknown property "+paramAndValue[0]+" for "+liquibaseDataType.getClass().getName()+" "+liquibaseDataType.toString());
                    }
                }
            }
        }

        if (primaryKey && liquibaseDataType instanceof IntType) {
            ((IntType) liquibaseDataType).setAutoIncrement(true);
        }
        if (primaryKey && liquibaseDataType instanceof BigIntType) {
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
        return fromDescription(type.toString(), database);
    }

    public LiquibaseDataType from(DatabaseDataType type, Database database) {
        return fromDescription(type.toString(), database);
    }

    public String getTrueBooleanValue(Database database) {
        return fromDescription("boolean", database).objectToSql(true, database);
    }

    public String getFalseBooleanValue(Database database) {
        return fromDescription("boolean", database).objectToSql(false, database);
    }
}
