package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotIdService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Snapshot generator for a SEQUENCE object in a JDBC-accessible database
 */
public class SequenceSnapshotGenerator extends JdbcSnapshotGenerator {

    public SequenceSnapshotGenerator() {
        super(Sequence.class, new Class[]{Schema.class});
    }

    private static final StringBuilder COMMON_PG_SEQUENCE_QUERY = new StringBuilder("JOIN pg_namespace ns on c.relnamespace = ns.oid ")
            .append("LEFT JOIN pg_depend d ON c.oid = d.objid WHERE c.relkind = 'S' AND ns.nspname = 'SCHEMA_NAME' ")
            .append("AND (c.oid not in (select ds.objid FROM pg_depend ds where ds.refobjsubid > 0) OR ( d.deptype = 'a' AND EXISTS ( ")
            .append("select 1 from pg_attribute a JOIN pg_class t ON t.oid = d.refobjid AND a.attrelid=t.oid AND a.attnum=d.refobjsubid ")
            .append("LEFT JOIN pg_catalog.pg_attrdef ad ON ad.adrelid = a.attrelid AND ad.adnum = a.attnum ")
            .append("WHERE a.atthasdef = false or not (pg_get_expr(ad.adbin, ad.adrelid) ilike '%' || c.relname || '%'))))");

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!(foundObject instanceof Schema) || !snapshot.getDatabase().supports(Sequence.class)) {
            return;
        }
        Schema schema = (Schema) foundObject;
        Database database = snapshot.getDatabase();

        //noinspection unchecked
        List<Map<String, ?>> sequences = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForList(getSelectSequenceStatement(schema, database));

        if (sequences != null) {
            for (Map<String, ?> sequence : sequences) {
                schema.addDatabaseObject(mapToSequence(sequence, (Schema) foundObject, database));
            }
        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        if (example.getSnapshotId() != null) {
            return example;
        }
        Database database = snapshot.getDatabase();
        List<Map<String, ?>> sequences;
        if (database instanceof Db2zDatabase) {
            sequences = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                    .getExecutor("jdbc", database)
                    .queryForList(getSelectSequenceStatement(example.getSchema(), database));
            return getSequences(example, database, sequences);
        } else {
            if (example.getAttribute("liquibase-complete", false)) { //need to go through "snapshotting" the object even if it was previously populated in addTo. Use the "liquibase-complete" attribute to track that it doesn't need to be fully snapshotted
                example.setSnapshotId(SnapshotIdService.getInstance().generateId());
                example.setAttribute("liquibase-complete", null);
                return example;
            }

            if (!database.supports(Sequence.class)) {
                return null;
            }
            sequences = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                    .getExecutor("jdbc", database)
                    .queryForList(getSelectSequenceStatement(example.getSchema(), database));
            return getSequences(example, database, sequences);
        }
    }

    private DatabaseObject getSequences(DatabaseObject example, Database database, List<Map<String, ?>> sequences) {
        for (Map<String, ?> sequenceRow : sequences) {
            String name = cleanNameFromDatabase((String) sequenceRow.get("SEQUENCE_NAME"), database);
            if (((database.isCaseSensitive() && name.equals(example.getName())) || (!database.isCaseSensitive() &&
                name.equalsIgnoreCase(example.getName())))) {
                return mapToSequence(sequenceRow, example.getSchema(), database);
            }
        }
        return null;
    }

    private Sequence mapToSequence(Map<String, ?> sequenceRow, Schema schema, Database database) {
        String name = cleanNameFromDatabase((String) sequenceRow.get("SEQUENCE_NAME"), database);
        Sequence seq = new Sequence();
        seq.setName(name);
        seq.setSchema(schema);
        seq.setStartValue(toBigInteger(sequenceRow.get("START_VALUE"), database));
        seq.setMinValue(toBigInteger(sequenceRow.get("MIN_VALUE"), database));
        seq.setMaxValue(toBigInteger(sequenceRow.get("MAX_VALUE"), database));
        seq.setCacheSize(toBigInteger(sequenceRow.get("CACHE_SIZE"), database));
        seq.setIncrementBy(toBigInteger(sequenceRow.get("INCREMENT_BY"), database));
        seq.setWillCycle(toBoolean(sequenceRow.get("WILL_CYCLE"), database));
        seq.setOrdered(toBoolean(sequenceRow.get("IS_ORDERED"), database));
        if (! (database instanceof CockroachDatabase)) {
            seq.setDataType((String) sequenceRow.get("SEQ_TYPE"));
        }
        seq.setAttribute("liquibase-complete", true);

        return seq;
    }

    protected Boolean toBoolean(Object value, Database database) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        String valueAsString = value.toString();
        valueAsString = valueAsString.replace("'", "");
        if ("true".equalsIgnoreCase(valueAsString)
                || "'true'".equalsIgnoreCase(valueAsString)
                || "y".equalsIgnoreCase(valueAsString)
                || "1".equalsIgnoreCase(valueAsString)
                || "t".equalsIgnoreCase(valueAsString)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    protected BigInteger toBigInteger(Object value, Database database) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }

        return new BigInteger(value.toString());
    }

    protected SqlStatement getSelectSequenceStatement (Schema schema, Database database) {
        if (database instanceof DB2Database) {
            if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
                return new RawParameterizedSqlStatement("SELECT SEQNAME AS SEQUENCE_NAME FROM QSYS2.SYSSEQUENCES WHERE SEQSCHEMA = ?", schema.getCatalogName());
            }
            return new RawParameterizedSqlStatement("SELECT SEQNAME AS SEQUENCE_NAME FROM SYSCAT.SEQUENCES WHERE SEQTYPE='S' AND SEQSCHEMA = ?", schema.getCatalogName());
        } else if (database instanceof Db2zDatabase) {
            StringBuilder sql = new StringBuilder("SELECT NAME AS SEQUENCE_NAME, START AS START_VALUE, MINVALUE AS MIN_VALUE, MAXVALUE AS MAX_VALUE, ")
                    .append("CACHE AS CACHE_SIZE, INCREMENT AS INCREMENT_BY, CYCLE AS WILL_CYCLE, ORDER AS IS_ORDERED FROM SYSIBM.SYSSEQUENCES WHERE SEQTYPE = 'S' AND SCHEMA = ?");
            return new RawParameterizedSqlStatement ( sql.toString(), schema.getCatalogName());
        } else if (database instanceof DerbyDatabase) {
            StringBuilder sql = new StringBuilder("SELECT seq.SEQUENCENAME AS SEQUENCE_NAME FROM SYS.SYSSEQUENCES seq, SYS.SYSSCHEMAS sch WHERE sch.SCHEMANAME = ? AND sch.SCHEMAID = seq.SCHEMAID");
            return new RawParameterizedSqlStatement( sql.toString(), new CatalogAndSchema(null, schema.getName()).customize(database).getSchemaName());
        } else if (database instanceof FirebirdDatabase) {
            return new RawParameterizedSqlStatement("SELECT TRIM(RDB$GENERATOR_NAME) AS SEQUENCE_NAME FROM RDB$GENERATORS WHERE RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0");
        } else if (database instanceof H2Database) {
            try {
                if (database.getDatabaseMajorVersion() <= 1) {
                    return new RawParameterizedSqlStatement("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = ? AND IS_GENERATED=FALSE", schema.getName());
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Cannot determine h2 version in order to generate sequence snapshot query");
            }
            return new RawParameterizedSqlStatement("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = ?", schema.getName());
        } else if (database instanceof HsqlDatabase) {
            return new RawParameterizedSqlStatement("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_SCHEMA = ?", schema.getName());
        } else if (database instanceof InformixDatabase) {
            return new RawParameterizedSqlStatement("SELECT tabname AS SEQUENCE_NAME FROM systables t, syssequences s WHERE s.tabid = t.tabid AND t.owner = ?", schema.getName());
        } else if (database instanceof OracleDatabase) {
            /*
             * Return an SQL statement that only returns the non-default values so the output changeLog is cleaner
             * and less polluted with unnecessary values.
             * The the following pages for the defaults (consistent for all supported releases ATM):
             * 12cR2: http://docs.oracle.com/database/122/SQLRF/CREATE-SEQUENCE.htm
             * 12cR1: http://docs.oracle.com/database/121/SQLRF/statements_6017.htm
             * 11gR2: http://docs.oracle.com/cd/E11882_01/server.112/e41084/statements_6015.htm
             */
            String catalogName = schema.getCatalogName();
            if (catalogName == null || catalogName.isEmpty()) {
                catalogName = database.getDefaultCatalogName();
            }
            StringBuilder sql = new StringBuilder("SELECT sequence_name, \n")
                    .append("CASE WHEN increment_by > 0 \n")
                    .append("     THEN CASE WHEN min_value=1 THEN NULL ELSE min_value END\n")
                    .append("     ELSE CASE WHEN min_value=(-999999999999999999999999999) THEN NULL else min_value END\n")
                    .append("END AS min_value, \n")
                    .append("CASE WHEN increment_by > 0 \n")
                    .append("     THEN CASE WHEN max_value=999999999999999999999999999 THEN NULL ELSE max_value END\n")
                    .append("     ELSE CASE WHEN max_value=last_number THEN NULL else max_value END \n")
                    .append("END  AS max_value, \n")
                    .append("CASE WHEN increment_by = 1 THEN NULL ELSE increment_by END AS increment_by, \n")
                    .append("CASE WHEN cycle_flag = 'N' THEN NULL ELSE cycle_flag END AS will_cycle, \n")
                    .append("CASE WHEN order_flag = 'N' THEN NULL ELSE order_flag END AS is_ordered, \n")
                    .append("LAST_NUMBER as START_VALUE, \n")
                    .append("CASE WHEN cache_size = 20 THEN NULL ELSE cache_size END AS cache_size \n")
                    .append(String.format("FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = '%s'", catalogName));
            return new RawParameterizedSqlStatement(sql.toString());
        } else if (database instanceof PostgresDatabase) {
            int version = 9;
            try {
                version = database.getDatabaseMajorVersion();
            } catch (Exception ignore) {
                Scope.getCurrentScope().getLog(getClass()).warning("Failed to retrieve database version: " + ignore);
            }
            String schemaName = schema.getName();
            if(schemaName == null) {
                schemaName = database.getDefaultSchemaName();
            }
            String pgSequenceQuery = COMMON_PG_SEQUENCE_QUERY.toString().replace("SCHEMA_NAME", schemaName);
            if (version < 10) { // 'pg_sequence' view does not exists yet
                return new RawParameterizedSqlStatement(String.format("SELECT c.relname AS \"SEQUENCE_NAME\" FROM pg_class c %s", pgSequenceQuery));
            } else {
                StringBuilder sql = new StringBuilder("SELECT c.relname AS \"SEQUENCE_NAME\", ")
                        .append("  s.seqmin AS \"MIN_VALUE\", s.seqmax AS \"MAX_VALUE\", s.seqincrement AS \"INCREMENT_BY\", ")
                        .append("  s.seqcycle AS \"WILL_CYCLE\", s.seqstart AS \"START_VALUE\", s.seqcache AS \"CACHE_SIZE\", ")
                        .append("  pg_catalog.format_type(s.seqtypid, NULL) AS \"SEQ_TYPE\" ")
                        .append("FROM pg_class c ")
                        .append("JOIN pg_sequence s on c.oid = s.seqrelid ")
                        .append(pgSequenceQuery);
                return new RawParameterizedSqlStatement(sql.toString());
            }
        } else if (database instanceof MSSQLDatabase) {
            StringBuilder sql = new StringBuilder("SELECT SEQUENCE_NAME, cast(START_VALUE AS BIGINT) AS START_VALUE, cast(MINIMUM_VALUE AS BIGINT) AS MIN_VALUE, ")
                    .append("cast(MAXIMUM_VALUE AS BIGINT) AS MAX_VALUE, CAST(INCREMENT AS BIGINT) AS INCREMENT_BY, CYCLE_OPTION AS WILL_CYCLE ")
                    .append("FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = ?");
            return new RawParameterizedSqlStatement( sql.toString(), schema.getName());
        } else if (database instanceof MariaDBDatabase) {
            StringJoiner j = new StringJoiner(" \n UNION\n");
            try {
                StringBuilder sql = new StringBuilder("select table_name AS SEQUENCE_NAME from information_schema.TABLES ")
                        .append("where TABLE_SCHEMA = ? and TABLE_TYPE = 'SEQUENCE' order by table_name;");
                List<Map<String, ?>> res = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                        .getExecutor("jdbc", database)
                        .queryForList(new RawParameterizedSqlStatement(sql.toString(), schema.getName()));
                if (res.size() == 0) {
                    return new RawParameterizedSqlStatement("SELECT 'name' AS SEQUENCE_NAME from dual WHERE 1=0");
                }
                for (Map<String, ?> e : res) {
                    String seqName = (String) e.get("SEQUENCE_NAME");
                    j.add(String.format("SELECT '%s' AS SEQUENCE_NAME, " +
                            "START_VALUE AS START_VALUE, " +
                            "MINIMUM_VALUE AS MIN_VALUE, " +
                            "MAXIMUM_VALUE AS MAX_VALUE, " +
                            "INCREMENT AS INCREMENT_BY, " +
                            "CYCLE_OPTION AS WILL_CYCLE " +
                            "FROM %s ", seqName, seqName));
                }
            } catch (DatabaseException e) {
                throw new UnexpectedLiquibaseException("Could not get list of schemas ", e);
            }
            return new RawParameterizedSqlStatement(j.toString());
        } else if (database instanceof SybaseASADatabase) {
            StringBuilder sql = new StringBuilder("SELECT SEQUENCE_NAME, START_WITH AS START_VALUE, MIN_VALUE, ")
                    .append("MAX_VALUE, INCREMENT_BY, CYCLE AS WILL_CYCLE FROM SYS.SYSSEQUENCE s JOIN SYS.SYSUSER u ON s.OWNER = u.USER_ID ")
                    .append("WHERE u.USER_NAME = ?");
            return new RawParameterizedSqlStatement( sql.toString(), schema.getName());
        } else if (database.getClass().getName().contains("MaxDB")) { //have to check classname as this is currently an extension
            return new RawParameterizedSqlStatement("SELECT SEQUENCE_NAME, MIN_VALUE, MAX_VALUE, INCREMENT_BY, CYCLE_FLAG AS WILL_CYCLE " +
                    "FROM sequences WHERE SCHEMANAME = ?", schema.getName());
        } else {
            throw new UnexpectedLiquibaseException("Don't know how to query for sequences on " + database);
        }
    }
}
