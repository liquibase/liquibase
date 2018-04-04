package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

@DataTypeInfo(name = "blob", aliases = {"longblob", "longvarbinary", "java.sql.Types.BLOB", "java.sql.Types.LONGBLOB", "java.sql.Types.LONGVARBINARY", "java.sql.Types.VARBINARY", "java.sql.Types.BINARY", "varbinary", "binary", "image", "tinyblob", "mediumblob"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class BlobType extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());

        if ((database instanceof H2Database) || (database instanceof HsqlDatabase)) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("varbinary") || originalDefinition.startsWith("java.sql.Types.VARBINARY")) {
                return new DatabaseDataType("VARBINARY", getParameters());
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("longvarbinary") || originalDefinition.startsWith("java.sql.Types.LONGVARBINARY")) {
                return new DatabaseDataType("LONGVARBINARY", getParameters());
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("binary")) {
                return new DatabaseDataType("BINARY", getParameters());
            } else {
                return new DatabaseDataType("BLOB");
            }
        }

        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            if ("varbinary".equals(originalDefinition.toLowerCase(Locale.US))
                    || "[varbinary]".equals(originalDefinition)
                    || originalDefinition.matches("(?i)varbinary\\s*\\(.+")
                    || originalDefinition.matches("\\[varbinary\\]\\s*\\(.+")) {

                return new DatabaseDataType(database.escapeDataTypeName("varbinary"), maybeMaxParam(parameters, database));
            } else if ("binary".equals(originalDefinition.toLowerCase(Locale.US))
                    || "[binary]".equals(originalDefinition)
                    || originalDefinition.matches("(?i)binary\\s*\\(.+")
                    || originalDefinition.matches("\\[binary\\]\\s*\\(.+")) {

                if (parameters.length < 1) {
                    parameters = new Object[]{1};
                } else if (parameters.length > 1) {
                    parameters = Arrays.copyOfRange(parameters, 0, 1);
                }
                return new DatabaseDataType(database.escapeDataTypeName("binary"), parameters);
            }
            if ("image".equals(originalDefinition.toLowerCase(Locale.US))
                    || "[image]".equals(originalDefinition)
                    || originalDefinition.matches("(?i)image\\s*\\(.+")
                    || originalDefinition.matches("\\[image\\]\\s*\\(.+")) {

                return new DatabaseDataType(database.escapeDataTypeName("image"));
            }
            if (parameters.length == 0) {
                return new DatabaseDataType(database.escapeDataTypeName("varbinary"), "MAX");
            } else {
                return new DatabaseDataType(database.escapeDataTypeName("varbinary"), maybeMaxParam(parameters, database));
            }
        }

        if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("blob") || "java.sql.Types.BLOB".equals(originalDefinition)) {
                return new DatabaseDataType("BLOB");
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("varbinary") || "java.sql.Types.VARBINARY".equals
                (originalDefinition)) {
                return new DatabaseDataType("VARBINARY", getParameters());
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("tinyblob")) {
                return new DatabaseDataType("TINYBLOB");
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("mediumblob")) {
                return new DatabaseDataType("MEDIUMBLOB");
            } else if (originalDefinition.toLowerCase(Locale.US).startsWith("binary")) {
                return new DatabaseDataType("BINARY", getParameters());
            } else {
                return new DatabaseDataType("LONGBLOB");
            }
        }

        if (database instanceof PostgresDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("blob") || "java.sql.Types.BLOB".equals(originalDefinition)) {
                // There are two ways of handling byte arrays ("BLOBs") in pgsql. For consistency with Hibernate ORM
                // (see upstream bug https://liquibase.jira.com/browse/CORE-1863) we choose the oid variant.
                // For a more thorough discussion of the two alternatives, see:
                // https://stackoverflow.com/questions/3677380/proper-hibernate-annotation-for-byte
                return new DatabaseDataType("OID");
            }

            return new DatabaseDataType("BYTEA");
        }

        if (database instanceof SybaseASADatabase) {
            return new DatabaseDataType("LONG BINARY");
        }

        if (database instanceof SybaseDatabase) {
            return new DatabaseDataType("IMAGE");
        }

        if (database instanceof OracleDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("bfile")) {
                return new DatabaseDataType("BFILE");
            }

            if (originalDefinition.toLowerCase(Locale.US).startsWith("raw") || originalDefinition.toLowerCase(Locale.US).startsWith("binary") || originalDefinition.toLowerCase(Locale.US).startsWith("varbinary")) {
                return new DatabaseDataType("RAW", getParameters());
            }

            return new DatabaseDataType("BLOB");
        }

        if (database instanceof FirebirdDatabase) {
            return new DatabaseDataType("BLOB");
        }

        return super.toDatabaseDataType(database);
    }

    private Object[] maybeMaxParam(Object[] parameters, Database database) {
        if (parameters.length < 1) {
            parameters = new Object[]{1};
        } else if (parameters.length > 1) {
            parameters = Arrays.copyOfRange(parameters, 0, 1);
        }

        boolean max = true;
        if (parameters.length > 0) {
            String param1 = parameters[0].toString();
            max = !param1.matches("\\d+") || (new BigInteger(param1).compareTo(BigInteger.valueOf(8000L)) > 0);
        }
        if (max) {
            return new Object[]{"MAX"};
        }
        if (parameters.length > 1) {
            parameters = Arrays.copyOfRange(parameters, 0, 1);
        }
        return parameters;

    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return "'" + value + "'";
        } else {
            return value.toString();
        }
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.BLOB;
    }
}
