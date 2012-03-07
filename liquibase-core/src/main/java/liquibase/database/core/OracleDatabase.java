package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.structure.Schema;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.statement.DatabaseFunction;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Encapsulates Oracle database support.
 */
public class OracleDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "oracle";

	public OracleDatabase() {
		// Setting list of Oracle's native functions
		databaseFunctions.add(new DatabaseFunction("SYSDATE"));
		databaseFunctions.add(new DatabaseFunction("SYSTIMESTAMP"));
        databaseFunctions.add(new DatabaseFunction("CURRENT_TIMESTAMP"));
	}

	public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    protected String correctObjectName(String objectName) {
        return objectName.toUpperCase();
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        try {
        	Method wrappedConn = conn.getClass().getMethod("getWrappedConnection");
        	wrappedConn.setAccessible(true);
        	Connection sqlConn = (Connection)wrappedConn.invoke(conn);
            Method method = sqlConn.getClass().getMethod("setRemarksReporting", Boolean.TYPE);
            method.setAccessible(true);
            method.invoke(sqlConn, true);
        } catch (Exception e) {
            LogFactory.getLogger().info("Could not set remarks reporting on OracleDatabase: "+e.getMessage());
            ; //cannot set it. That is OK
        }
        super.setConnection(conn);
    }

    public String getTypeName() {
        return "oracle";
    }

    @Override
    public String generatePrimaryKeyName(String tableName) {
        if (tableName.length() > 27) {
            return "PK_" + tableName.toUpperCase().substring(0, 27);
        } else {
            return "PK_" + tableName.toUpperCase();
        }
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    @Override
    public String escapeDatabaseObject(String objectName) {
        // escape the object name if it contains any non-word characters
        if (objectName != null &&
                (Pattern.compile("\\W").matcher(objectName).find() || isReservedWord(objectName))) {
            return "\"" + objectName.trim().toUpperCase() + "\"";
        } else {
            return objectName;
        }
    }

    @Override
    public boolean isReservedWord(String objectName) {
        return reservedWords.contains(objectName.toUpperCase());
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:oracle")) {
            return "oracle.jdbc.OracleDriver";
        }
        return null;
    }

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }
        return "SYSTIMESTAMP";
    }

    @Override
    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        String escapedIndexName = indexName;
        if (schemaName != null)
        {
            escapedIndexName = schemaName + "." + escapedIndexName;
        }
        return escapedIndexName;
    }

    /**
     * Return an Oracle date literal with the same value as a string formatted using ISO 8601.
     * <p/>
     * Convert an ISO8601 date string to one of the following results:
     * to_date('1995-05-23', 'YYYY-MM-DD')
     * to_date('1995-05-23 09:23:59', 'YYYY-MM-DD HH24:MI:SS')
     * <p/>
     * Implementation restriction:
     * Currently, only the following subsets of ISO8601 are supported:
     * YYYY-MM-DD
     * YYYY-MM-DDThh:mm:ss
     */
    @Override
    public String getDateLiteral(String isoDate) {
        String normalLiteral = super.getDateLiteral(isoDate);

        if (isDateOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("to_date(");
            val.append(normalLiteral);
            val.append(", 'YYYY-MM-DD')");
            return val.toString();
        } else if (isTimeOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("to_date(");
            val.append(normalLiteral);
            val.append(", 'HH24:MI:SS')");
            return val.toString();
        } else if (isDateTime(isoDate)) {
            normalLiteral = normalLiteral.substring(0, normalLiteral.lastIndexOf('.'))+"'";

            StringBuffer val = new StringBuffer(26);
            val.append("to_date(");
            val.append(normalLiteral);
            val.append(", 'YYYY-MM-DD HH24:MI:SS')");
            return val.toString();
        } else {
            return "UNSUPPORTED:" + isoDate;
        }
    }

    @Override
    public boolean isSystemTable(Schema schema, String tableName) {
        if (super.isSystemTable(schema, tableName)) {
            return true;
        } else if (tableName.startsWith("BIN$")) { //oracle deleted table
            return true;
        } else if (tableName.startsWith("AQ$")) { //oracle AQ tables
            return true;
        } else if (tableName.startsWith("DR$")) { //oracle index tables
            return true;
        } else if (tableName.startsWith("SYS_IOT_OVER")) { //oracle system table
            return true;
        }
        return false;
    }
    
    @Override
    public boolean shouldQuoteValue(String value) {
        return super.shouldQuoteValue(value)
            && !value.startsWith("to_date(")
            && !value.equalsIgnoreCase(getCurrentDateTimeFunction());
    }

    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }


//    public Set<UniqueConstraint> findUniqueConstraints(String schema) throws DatabaseException {
//        Set<UniqueConstraint> returnSet = new HashSet<UniqueConstraint>();
//
//        List<Map> maps = new Executor(this).queryForList(new RawSqlStatement("SELECT UC.CONSTRAINT_NAME, UCC.TABLE_NAME, UCC.COLUMN_NAME FROM USER_CONSTRAINTS UC, USER_CONS_COLUMNS UCC WHERE UC.CONSTRAINT_NAME=UCC.CONSTRAINT_NAME AND CONSTRAINT_TYPE='U' ORDER BY UC.CONSTRAINT_NAME"));
//
//        UniqueConstraint constraint = null;
//        for (Map map : maps) {
//            if (constraint == null || !constraint.getName().equals(constraint.getName())) {
//                returnSet.add(constraint);
//                Table table = new Table((String) map.get("TABLE_NAME"));
//                constraint = new UniqueConstraint(map.get("CONSTRAINT_NAME").toString(), table);
//            }
//        }
//        if (constraint != null) {
//            returnSet.add(constraint);
//        }
//
//        return returnSet;
//    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    private Set<String> reservedWords = new HashSet(Arrays.asList(
//            "ACCESS",
//            "ACCOUNT",
//            "ACTIVATE",
//            "ADD",
//            "ADMIN",
//            "ADVISE",
//            "AFTER",
//            "ALL",
//            "ALL_ROWS",
//            "ALLOCATE",
//            "ALTER",
//            "ANALYZE",
//            "AND",
//            "ANY",
//            "ARCHIVE",
//            "ARCHIVELOG",
//            "ARRAY",
//            "AS",
//            "ASC",
//            "AT",
//            "AUDIT",
//            "AUTHENTICATED",
//            "AUTHORIZATION",
//            "AUTOEXTEND",
//            "AUTOMATIC",
//            "BACKUP",
//            "BECOME",
//            "BEFORE",
//            "BEGIN",
//            "BETWEEN",
//            "BFILE",
//            "BITMAP",
//            "BLOB",
//            "BLOCK",
//            "BODY",
//            "BY",
//            "CACHE",
//            "CACHE_INSTANCES",
//            "CANCEL",
//            "CASCADE",
//            "CAST",
//            "CFILE",
//            "CHAINED",
//            "CHANGE",
//            "CHAR",
//            "CHAR_CS",
//            "CHARACTER",
//            "CHECK",
//            "CHECKPOINT",
//            "CHOOSE",
//            "CHUNK",
//            "CLEAR",
//            "CLOB",
//            "CLONE",
//            "CLOSE",
//            "CLOSE_CACHED_OPEN_CURSORS",
//            "CLUSTER",
//            "COALESCE",
//            "COLUMN",
//            "COLUMNS",
//            "COMMENT",
//            "COMMIT",
//            "COMMITTED",
//            "COMPATIBILITY",
//            "COMPILE",
//            "COMPLETE",
//            "COMPOSITE_LIMIT",
//            "COMPRESS",
//            "COMPUTE",
//            "CONNECT",
//            "CONNECT_TIME",
//            "CONSTRAINT",
//            "CONSTRAINTS",
//            "CONTENTS",
//            "CONTINUE",
//            "CONTROLFILE",
//            "CONVERT",
//            "COST",
//            "CPU_PER_CALL",
//            "CPU_PER_SESSION",
//            "CREATE",
//            "CURRENT",
//            "CURRENT_SCHEMA",
//            "CURREN_USER",
//            "CURSOR",
//            "CYCLE",
//            "DANGLING",
//            "DATABASE",
//            "DATAFILE",
//            "DATAFILES",
//            "DATAOBJNO",
//            "DATE",
//            "DBA",
//            "DBHIGH",
//            "DBLOW",
//            "DBMAC",
//            "DEALLOCATE",
//            "DEBUG",
//            "DEC",
//            "DECIMAL",
//            "DECLARE",
//            "DEFAULT",
//            "DEFERRABLE",
//            "DEFERRED",
//            "DEGREE",
//            "DELETE",
//            "DEREF",
//            "DESC",
//            "DIRECTORY",
//            "DISABLE",
//            "DISCONNECT",
//            "DISMOUNT",
//            "DISTINCT",
//            "DISTRIBUTED",
//            "DML",
//            "DOUBLE",
//            "DROP",
//            "DUMP",
//            "EACH",
//            "ELSE",
//            "ENABLE",
//            "END",
//            "ENFORCE",
//            "ENTRY",
//            "ESCAPE",
//            "EXCEPT",
//            "EXCEPTIONS",
//            "EXCHANGE",
//            "EXCLUDING",
//            "EXCLUSIVE",
//            "EXECUTE",
//            "EXISTS",
//            "EXPIRE",
//            "EXPLAIN",
//            "EXTENT",
//            "EXTENTS",
//            "EXTERNALLY",
//            "FAILED_LOGIN_ATTEMPTS",
//            "FALSE",
//            "FAST",
//            "FILE",
//            "FIRST_ROWS",
//            "FLAGGER",
//            "FLOAT",
//            "FLOB",
//            "FLUSH",
//            "FOR",
//            "FORCE",
//            "FOREIGN",
//            "FREELIST",
//            "FREELISTS",
//            "FROM",
//            "FULL",
//            "FUNCTION",
//            "GLOBAL",
//            "GLOBALLY",
//            "GLOBAL_NAME",
//            "GRANT",
//            "GROUP",
//            "GROUPS",
//            "HASH",
//            "HASHKEYS",
//            "HAVING",
//            "HEADER",
//            "HEAP",
//            "IDENTIFIED",
//            "IDGENERATORS",
//            "IDLE_TIME",
//            "IF",
//            "IMMEDIATE",
//            "IN",
//            "INCLUDING",
//            "INCREMENT",
//            "INDEX",
//            "INDEXED",
//            "INDEXES",
//            "INDICATOR",
//            "IND_PARTITION",
//            "INITIAL",
//            "INITIALLY",
//            "INITRANS",
//            "INSERT",
//            "INSTANCE",
//            "INSTANCES",
//            "INSTEAD",
//            "INT",
//            "INTEGER",
//            "INTERMEDIATE",
//            "INTERSECT",
//            "INTO",
//            "IS",
//            "ISOLATION",
//            "ISOLATION_LEVEL",
//            "KEEP",
//            "KEY",
//            "KILL",
//            "LABEL",
//            "LAYER",
//            "LESS",
//            "LEVEL",
//            "LIBRARY",
//            "LIKE",
//            "LIMIT",
//            "LINK",
//            "LIST",
//            "LOB",
//            "LOCAL",
//            "LOCK",
//            "LOCKED",
//            "LOG",
//            "LOGFILE",
//            "LOGGING",
//            "LOGICAL_READS_PER_CALL",
//            "LOGICAL_READS_PER_SESSION",
//            "LONG",
//            "MANAGE",
//            "MASTER",
//            "MAX",
//            "MAXARCHLOGS",
//            "MAXDATAFILES",
//            "MAXEXTENTS",
//            "MAXINSTANCES",
//            "MAXLOGFILES",
//            "MAXLOGHISTORY",
//            "MAXLOGMEMBERS",
//            "MAXSIZE",
//            "MAXTRANS",
//            "MAXVALUE",
//            "MIN",
//            "MEMBER",
//            "MINIMUM",
//            "MINEXTENTS",
//            "MINUS",
//            "MINVALUE",
//            "MLSLABEL",
//            "MLS_LABEL_FORMAT",
//            "MODE",
//            "MODIFY",
//            "MOUNT",
//            "MOVE",
//            "MTS_DISPATCHERS",
//            "MULTISET",
//            "NATIONAL",
//            "NCHAR",
//            "NCHAR_CS",
//            "NCLOB",
//            "NEEDED",
//            "NESTED",
//            "NETWORK",
//            "NEW",
//            "NEXT",
//            "NOARCHIVELOG",
//            "NOAUDIT",
//            "NOCACHE",
//            "NOCOMPRESS",
//            "NOCYCLE",
//            "NOFORCE",
//            "NOLOGGING",
//            "NOMAXVALUE",
//            "NOMINVALUE",
//            "NONE",
//            "NOORDER",
//            "NOOVERRIDE",
//            "NOPARALLEL",
//            "NOPARALLEL",
//            "NOREVERSE",
//            "NORMAL",
//            "NOSORT",
//            "NOT",
//            "NOTHING",
//            "NOWAIT",
//            "NULL",
            "NUMBER",
//            "NUMERIC",
//            "NVARCHAR2",
//            "OBJECT",
//            "OBJNO",
//            "OBJNO_REUSE",
//            "OF",
//            "OFF",
//            "OFFLINE",
//            "OID",
//            "OIDINDEX",
//            "OLD",
//            "ON",
//            "ONLINE",
//            "ONLY",
//            "OPCODE",
//            "OPEN",
//            "OPTIMAL",
//            "OPTIMIZER_GOAL",
//            "OPTION",
//            "OR",
//            "ORDER",
//            "ORGANIZATION",
//            "OSLABEL",
//            "OVERFLOW",
//            "OWN",
//            "PACKAGE",
//            "PARALLEL",
//            "PARTITION",
//            "PASSWORD",
//            "PASSWORD_GRACE_TIME",
//            "PASSWORD_LIFE_TIME",
//            "PASSWORD_LOCK_TIME",
//            "PASSWORD_REUSE_MAX",
//            "PASSWORD_REUSE_TIME",
//            "PASSWORD_VERIFY_FUNCTION",
//            "PCTFREE",
//            "PCTINCREASE",
//            "PCTTHRESHOLD",
//            "PCTUSED",
//            "PCTVERSION",
//            "PERCENT",
//            "PERMANENT",
//            "PLAN",
//            "PLSQL_DEBUG",
//            "POST_TRANSACTION",
//            "PRECISION",
//            "PRESERVE",
//            "PRIMARY",
//            "PRIOR",
//            "PRIVATE",
//            "PRIVATE_SGA",
//            "PRIVILEGE",
//            "PRIVILEGES",
//            "PROCEDURE",
//            "PROFILE",
//            "PUBLIC",
//            "PURGE",
//            "QUEUE",
//            "QUOTA",
//            "RANGE",
//            "RAW",
//            "RBA",
//            "READ",
//            "READUP",
//            "REAL",
//            "REBUILD",
//            "RECOVER",
//            "RECOVERABLE",
//            "RECOVERY",
//            "REF",
//            "REFERENCES",
//            "REFERENCING",
//            "REFRESH",
//            "RENAME",
//            "REPLACE",
//            "RESET",
//            "RESETLOGS",
//            "RESIZE",
//            "RESOURCE",
//            "RESTRICTED",
//            "RETURN",
//            "RETURNING",
//            "REUSE",
//            "REVERSE",
//            "REVOKE",
//            "ROLE",
//            "ROLES",
//            "ROLLBACK",
//            "ROW",
//            "ROWID",
//            "ROWNUM",
//            "ROWS",
//            "RULE",
//            "SAMPLE",
//            "SAVEPOINT",
//            "SB4",
//            "SCAN_INSTANCES",
//            "SCHEMA",
//            "SCN",
//            "SCOPE",
//            "SD_ALL",
//            "SD_INHIBIT",
//            "SD_SHOW",
//            "SEGMENT",
//            "SEG_BLOCK",
//            "SEG_FILE",
//            "SELECT",
//            "SEQUENCE",
//            "SERIALIZABLE",
            "SESSION",
//            "SESSION_CACHED_CURSORS",
//            "SESSIONS_PER_USER",
//            "SET",
//            "SHARE",
//            "SHARED",
//            "SHARED_POOL",
//            "SHRINK",
//            "SIZE",
//            "SKIP",
//            "SKIP_UNUSABLE_INDEXES",
//            "SMALLINT",
//            "SNAPSHOT",
//            "SOME",
//            "SORT",
//            "SPECIFICATION",
//            "SPLIT",
//            "SQL_TRACE",
//            "STANDBY",
//            "START",
//            "STATEMENT_ID",
//            "STATISTICS",
//            "STOP",
//            "STORAGE",
//            "STORE",
//            "STRUCTURE",
//            "SUCCESSFUL",
//            "SWITCH",
//            "SYS_OP_ENFORCE_NOT_NULL",
//            "SYS_OP_NTCIMG",
//            "SYNONYM",
//            "SYSDATE",
//            "SYSDBA",
//            "SYSOPER",
//            "SYSTEM",
//            "TABLE",
//            "TABLES",
//            "TABNO",
//            "TEMPORARY",
//            "THAN",
//            "THE",
//            "THEN",
//            "THREAD",
//            "TIMESTAMP",
//            "TIME",
//            "TO",
//            "TOPLEVEL",
//            "TRACE",
//            "TRACING",
//            "TRANSACTION",
//            "TRANSITIONAL",
//            "TRIGGER",
//            "TRIGGERS",
//            "TRUE",
//            "TRUNCATE",
//            "TX",
//            "TYPE",
//            "UB2",
//            "UBA",
//            "UID",
//            "UNARCHIVED",
//            "UNDO",
//            "UNION",
//            "UNIQUE",
//            "UNLIMITED",
//            "UNLOCK",
//            "UNRECOVERABLE",
//            "UNTIL",
//            "UNUSABLE",
//            "UNUSED",
//            "UPDATABLE",
//            "UPDATE",
//            "USAGE",
//            "USE",
            "USER"
//            "USING",
//            "VALIDATE",
//            "VALIDATION",
//            "VALUE",
//            "VALUES",
//            "VARCHAR",
//            "VARCHAR2",
//            "VARYING",
//            "VIEW",
//            "WHEN",
//            "WHENEVER",
//            "WHERE",
//            "WITH",
//            "WITHOUT",
//            "WORK",
//            "WRITE",
//            "WRITEDOWN",
//            "WRITEUP",
//            "XID",
//            "YEAR"
    ));
}
